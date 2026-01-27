package com.ssafy.hearo.domain.matching.service;

import com.ssafy.hearo.domain.matching.dto.MatchingResult;
import com.ssafy.hearo.domain.queue.service.QueueEventPublisher;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.queue.service.QueueService.PopResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private final CounselorAvailabilityService counselorAvailabilityService;
    private final QueueEventPublisher queueEventPublisher;
    private final ApplicationEventPublisher eventPublisher;

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
        // 가용 상담원 조회
        Set<Long> availableCounselors = counselorAvailabilityService.getAvailableCounselorIds();

        if (availableCounselors.isEmpty()) {
            log.debug("매칭 스킵: 가용 상담원 없음");
            return;
        }

        // 대기열 상태 확인
        QueueService.QueueSizes sizes = queueService.getQueueSizes();
        if (sizes.totalSize() == 0) {
            log.debug("매칭 스킵: 대기 고객 없음");
            return;
        }

        log.info("매칭 시작 - 가용 상담원: {}명, 대기 고객: {}명 (Blacklist: {}, Normal: {})",
                availableCounselors.size(), sizes.totalSize(),
                sizes.blacklistQueueSize(), sizes.normalQueueSize());

        // 가용 상담원 수만큼 매칭 시도
        int matchedCount = 0;
        int maxMatches = availableCounselors.size();

        while (matchedCount < maxMatches) {
            // 현재 가용 상담원 목록 다시 조회 (매칭 중 변경될 수 있음)
            Set<Long> currentAvailable = counselorAvailabilityService.getAvailableCounselorIds();
            if (currentAvailable.isEmpty()) {
                break;
            }

            // 매칭 가능한 고객 추출
            PopResult result = queueService.popMatchable(currentAvailable);

            if (!result.hasMatch()) {
                log.info("매칭 종료: 더 이상 매칭 가능한 고객 없음. " +
                         "Blacklist 스킵: {}, Normal→Blacklist 이동: {}",
                        result.skippedCount(), result.movedToBlacklistCount());
                break;
            }

            // 매칭 가능한 상담원 중 하나 선택 (첫 번째)
            Long selectedCounselor = result.matchableCounselorIds().iterator().next();

            // 상담 세션 생성
            String roomName = generateRoomName(result.customerId(), selectedCounselor);
            MatchingResult matchingResult = MatchingResult.success(
                    result.customerId(), selectedCounselor, roomName);

            // 상담원을 비가용 상태로 변경
            counselorAvailabilityService.setUnavailable(selectedCounselor);

            // 매칭 이벤트 발행 (상담 세션 생성용)
            eventPublisher.publishEvent(new MatchingCompletedEvent(
                    result.customerId(), selectedCounselor, roomName));

            log.info("매칭 완료: 고객={}, 상담원={}, 방={}",
                    result.customerId(), selectedCounselor, roomName);

            matchedCount++;
        }

        if (matchedCount > 0) {
            log.info("매칭 사이클 완료: {}건 매칭 성공", matchedCount);
        }
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
     * 매칭 완료 이벤트
     * 상담 세션 생성 등 후속 처리를 위해 발행
     */
    public record MatchingCompletedEvent(
            String customerId,
            Long counselorId,
            String roomName
    ) {}
}
