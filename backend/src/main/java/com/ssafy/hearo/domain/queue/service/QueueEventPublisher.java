package com.ssafy.hearo.domain.queue.service;

import com.ssafy.hearo.domain.matching.dto.MatchingNotification;
import com.ssafy.hearo.domain.queue.dto.QueueUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueEventPublisher {

    private static final String QUEUE_TOPIC = "/topic/queue-updates";
    private static final String RANK_TOPIC_PREFIX = "/topic/queue-rank/";
    private static final String COUNSELOR_TOPIC_PREFIX = "/topic/counselor/";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 대기열 변경 브로드캐스트
     * @param normalSize Normal Queue 크기
     * @param blacklistSize Blacklist Queue 크기
     */
    @Async
    public void publishQueueUpdate(long normalSize, long blacklistSize) {
        QueueUpdateMessage message = QueueUpdateMessage.of(
            normalSize,
            blacklistSize,
            System.currentTimeMillis()
        );

        log.debug("대기열 업데이트 브로드캐스트: normal={}, blacklist={}", normalSize, blacklistSize);
        messagingTemplate.convertAndSend(QUEUE_TOPIC, message);
    }

    /**
     * 특정 고객에게 순위 업데이트 전송
     * @param customerId 고객 식별자
     * @param rank 현재 대기 순위 (null이면 대기열에서 제거됨)
     */
    @Async
    public void sendRankUpdate(String customerId, Long rank) {
        String userTopic = RANK_TOPIC_PREFIX + customerId;
        RankUpdateMessage message = new RankUpdateMessage(
                customerId,
                rank,
                rank != null ? "WAITING" : "REMOVED",
                System.currentTimeMillis()
        );

        log.debug("순위 업데이트 전송: customerId={}, rank={}, status={}", customerId, rank, message.status());
        messagingTemplate.convertAndSend(userTopic, message);
    }

    /**
     * 여러 고객에게 순위 업데이트 일괄 전송
     * @param rankUpdates customerId -> rank 맵
     */
    @Async
    public void sendBatchRankUpdates(java.util.Map<String, Long> rankUpdates) {
        if (rankUpdates == null || rankUpdates.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        log.debug("배치 순위 업데이트 전송: {}명", rankUpdates.size());

        for (java.util.Map.Entry<String, Long> entry : rankUpdates.entrySet()) {
            String customerId = entry.getKey();
            Long rank = entry.getValue();
            String userTopic = RANK_TOPIC_PREFIX + customerId;

            RankUpdateMessage message = new RankUpdateMessage(
                    customerId,
                    rank,
                    "WAITING",
                    timestamp
            );
            messagingTemplate.convertAndSend(userTopic, message);
        }
    }

    /**
     * 순위 업데이트 메시지
     * @param customerId 고객 ID
     * @param rank 대기 순위 (null이면 대기열에서 제거됨)
     * @param status WAITING(대기중), REMOVED(대기열에서 제거됨), MATCHED(매칭완료)
     * @param timestamp 타임스탬프
     */
    public record RankUpdateMessage(String customerId, Long rank, String status, Long timestamp) {}

    /**
     * 고객에게 매칭 완료 알림 전송
     * @param customerId 고객 식별자 (예: "customer_123")
     * @param identity LiveKit 접속용 identity
     * @param roomName LiveKit 방 이름
     */
    public void sendMatchingToCustomer(String customerId, String identity, String roomName) {
        String userTopic = RANK_TOPIC_PREFIX + customerId;
        MatchingNotification.CustomerNotification notification = MatchingNotification.CustomerNotification.builder()
                .customerId(customerId)
                .status("MATCHED")
                .identity(identity)
                .roomName(roomName)
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("[WebSocket] 고객 {} 에게 MATCHED 알림 전송 (방: {}, identity: {})",
                customerId, roomName, identity);
        messagingTemplate.convertAndSend(userTopic, notification);
    }

    /**
     * 상담원에게 매칭 완료 알림 전송
     * @param counselorId 상담원 ID
     * @param registrationId 접수 ID
     * @param customerId 고객 ID (DB PK)
     * @param identity LiveKit 접속용 identity
     * @param roomName LiveKit 방 이름
     */
    public void sendMatchingToCounselor(Long counselorId, Long registrationId, Integer customerId,
                                        String identity, String roomName) {
        String counselorTopic = COUNSELOR_TOPIC_PREFIX + counselorId;
        MatchingNotification.CounselorNotification notification = MatchingNotification.CounselorNotification.builder()
                .type("MATCH_ASSIGNED")
                .registrationId(registrationId)
                .customerId(customerId)
                .identity(identity)
                .roomName(roomName)
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("[WebSocket] 상담원 {} 에게 MATCH_ASSIGNED 알림 전송 (고객: {}, 접수: {}, 방: {})",
                counselorId, customerId, registrationId, roomName);
        messagingTemplate.convertAndSend(counselorTopic, notification);
    }
}
