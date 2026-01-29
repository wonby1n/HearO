package com.ssafy.hearo.domain.queue.service;

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
     * @param rank 현재 대기 순위
     */
    public void sendRankUpdate(String customerId, Long rank) {
        String userTopic = RANK_TOPIC_PREFIX + customerId;
        RankUpdateMessage message = new RankUpdateMessage(customerId, rank, System.currentTimeMillis());

        log.debug("순위 업데이트 전송: customerId={}, rank={}", customerId, rank);
        messagingTemplate.convertAndSend(userTopic, message);
    }

    public record RankUpdateMessage(String customerId, Long rank, Long timestamp) {}
}
