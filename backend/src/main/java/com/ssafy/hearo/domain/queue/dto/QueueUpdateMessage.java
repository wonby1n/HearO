package com.ssafy.hearo.domain.queue.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 대기열에 변화가 생겼을 때
 * 웹소켓을 통해 브로드 캐스팅 할 때 사용하는 메시지 객체
 * 지금 대기열 전체 상황이 이렇다
 */

@Getter
@Builder
public class QueueUpdateMessage {

    private Long normalQueueSize;
    private Long blacklistQueueSize;
    private Long totalWaiting;
    private Long timestamp;

    public static QueueUpdateMessage of(long normalSize, long blacklistSize, long timestamp) {
        return QueueUpdateMessage.builder()
                .normalQueueSize(normalSize)
                .blacklistQueueSize(blacklistSize)
                .totalWaiting(normalSize + blacklistSize)
                .timestamp(timestamp)
                .build();
    }
}
