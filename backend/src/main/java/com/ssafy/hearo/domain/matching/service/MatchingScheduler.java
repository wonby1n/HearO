package com.ssafy.hearo.domain.matching.service;

import com.ssafy.hearo.domain.matching.dto.MatchingResult;
import com.ssafy.hearo.domain.queue.service.QueueEventPublisher;
import com.ssafy.hearo.domain.queue.service.QueueLeaseService;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.queue.service.QueueService.PopResult;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 자율 매칭 엔진
 * 5초마다 실행되어 대기 고객과 가용 상담원을 자동으로 매칭
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingScheduler {

    private final QueueService queueService;
    private final QueueLeaseService queueLeaseService;
    private final CounselorAvailabilityService counselorAvailabilityService;
    private final CounselorScoreService counselorScoreService;
    private final QueueEventPublisher queueEventPublisher;
    private final ApplicationEventPublisher eventPublisher;
    private final RegistrationRepository registrationRepository;

    /**
     * 5초마다 실행되는 자동 매칭 프로세스
     *
     * 로직:
     * 1. 가용 상담원 목록 조회
     * 2. 대기 고객 중 매칭 가능한 고객 탐색
     *    - Blacklist Queue 우선 (우선순위 높음)
     *    - Normal Queue 차순위
     * 3. 매칭 불가 고객은 자동으로 Blacklist Queue로 이동
     * 4. 매칭 성공 시 상담 세션 생성 이벤트 발행
     */
    @Scheduled(fixedRate = 5000) // 5초마다 실행
    public void executeMatching() {
        // 매칭 가능한 상담원 조회 (가용 AND 하트비트 활성)
        Set<Long> availableCounselors = counselorAvailabilityService.getMatchableCounselorIds();

        if (availableCounselors.isEmpty()) {
            return;
        }

        // 대기열 상태 확인
        QueueService.QueueSizes sizes = queueService.getQueueSizes();
        if (sizes.totalSize() == 0) {
            return;
        }

        log.info("[매칭] ========== 매칭 사이클 시작 ==========");
        log.info("[매칭] 가용 상담원: {} (ID: {})", availableCounselors.size(), availableCounselors);
        log.info("[매칭] 대기 고객: {}명 (Normal: {}, Blacklist: {})",
                sizes.totalSize(), sizes.normalQueueSize(), sizes.blacklistQueueSize());

        // 가용 상담원 수만큼 매칭 시도
        int matchedCount = 0;
        int maxMatches = availableCounselors.size();

        while (matchedCount < maxMatches) {
            // 현재 매칭 가능한 상담원 목록 다시 조회 (매칭 중 변경될 수 있음)
            Set<Long> currentAvailable = counselorAvailabilityService.getMatchableCounselorIds();
            if (currentAvailable.isEmpty()) {
                log.info("[매칭] 루프 중단: 가용 상담원이 더 이상 없음");
                break;
            }

            log.info("[매칭] {}번째 매칭 시도 - 현재 가용 상담원: {}", matchedCount + 1, currentAvailable);

            // 매칭 가능한 고객 추출
            PopResult result = queueService.popMatchable(currentAvailable);

            if (!result.hasMatch()) {
                log.info("[매칭] 매칭 가능한 고객 없음 (Blacklist 스킵: {}, Normal→Blacklist 이동: {})",
                        result.skippedCount(), result.movedToBlacklistCount());
                break;
            }

            // 매칭 가능한 상담원 중 최적의 상담원 선택 (가중치 기반)
            log.info("[매칭] 고객 {} 에 대해 상담원 선택 중... 후보: {}",
                    result.customerId(), result.matchableCounselorIds());

            Long selectedCounselor = counselorScoreService.selectBestCounselor(
                    result.customerId(), result.matchableCounselorIds());

            // 상담 세션 생성
            String roomName = generateRoomName(result.customerId(), selectedCounselor);
            MatchingResult matchingResult = MatchingResult.success(
                    result.customerId(), selectedCounselor, roomName);

            // 상담원을 비가용 상태로 변경
            log.info("[매칭] 상담원 {} → 비가용 상태로 변경 (고객 {} 매칭됨)", selectedCounselor, result.customerId());
            counselorAvailabilityService.setUnavailable(selectedCounselor);

            // 매칭 성공 시 lease 삭제 (더 이상 heartbeat 불필요)
            queueLeaseService.deleteLeaseByCustomerId(result.customerId());
            log.info("[매칭] 고객 {} lease 삭제 완료", result.customerId());

            // 매칭 이벤트 발행 (상담 세션 생성용)
            eventPublisher.publishEvent(new MatchingCompletedEvent(
                    result.customerId(), selectedCounselor, roomName));

            // WebSocket으로 고객/상담원에게 매칭 알림 전송
            sendMatchingNotifications(result.customerId(), selectedCounselor, roomName);

            log.info("[매칭] ★ 매칭 성공: 고객={} ↔ 상담원={}, 방={}",
                    result.customerId(), selectedCounselor, roomName);

            matchedCount++;
        }

        // 매칭 후 남은 가용 상담원 확인
        Set<Long> remainingAvailable = counselorAvailabilityService.getMatchableCounselorIds();
        log.info("[매칭] ========== 매칭 사이클 종료: {}건 성공, 남은 가용 상담원: {} ==========",
                matchedCount, remainingAvailable);
    }

    /**
     * 상담 방 이름 생성
     */
    private String generateRoomName(String customerId, Long counselorId) {
        return String.format("room-%s-%d-%s",
                customerId, counselorId,
                UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * WebSocket으로 고객/상담원에게 매칭 알림 전송
     */
    private void sendMatchingNotifications(String customerId, Long counselorId, String roomName) {
        // customerId에서 DB PK 추출 (예: "customer_123" -> 123)
        Integer customerDbId = extractCustomerDbId(customerId);

        // 고객용 identity 생성
        String customerIdentity = "customer_" + customerDbId;
        // 상담원용 identity 생성
        String counselorIdentity = "counselor_" + counselorId;

        // 고객에게 매칭 알림 전송
        queueEventPublisher.sendMatchingToCustomer(customerId, customerIdentity, roomName);

        // 상담원에게 매칭 알림 전송 (registrationId, customerId 포함)
        Long registrationId = findLatestRegistrationId(customerDbId);
        queueEventPublisher.sendMatchingToCounselor(counselorId, registrationId, customerDbId,
                counselorIdentity, roomName);
    }

    /**
     * customerId 문자열에서 DB PK 추출
     * 예: "customer_123" -> 123, "123" -> 123
     */
    private Integer extractCustomerDbId(String customerId) {
        if (customerId == null) {
            return null;
        }
        // "customer_" 접두사가 있으면 제거
        String idPart = customerId.startsWith("customer_")
                ? customerId.substring("customer_".length())
                : customerId;
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            log.warn("customerId에서 숫자 추출 실패: {}", customerId);
            return null;
        }
    }

    /**
     * 고객의 최신 접수 ID 조회
     */
    private Long findLatestRegistrationId(Integer customerDbId) {
        if (customerDbId == null) {
            return null;
        }
        Optional<Registration> registration = registrationRepository
                .findTopByCustomerIdOrderByCreatedAtDesc(customerDbId);
        return registration.map(r -> r.getId().longValue()).orElse(null);
    }

    /**
     * 매칭 완료 이벤트
     * 상담 세션 생성 등 후속 처리를 위해 발행
     */
    public record MatchingCompletedEvent(
            String customerId,
            Long counselorId,
            String roomName
    ) {}
}
