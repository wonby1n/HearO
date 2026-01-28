package com.ssafy.hearo.domain.queue.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 대기열의 현재 상태를 사용자에게 전달하기 위한 응답용 데이터 객체
 */

@Getter
@Builder
public class QueueStatusResponse {

    private String customerId;
    private Long waitingRank; // 현재 사용자의 대기 순번
    private String queueType; // 사용자가 일반 큐에 있는지 , blacklist 큐에 있는지 구분
    private Long estimatedWaitMinutes; // 현재 코드에서는 한 명당 평균 상담 시간을 5분으로 가저앟고 대기 시간을 계산함

    public static QueueStatusResponse of(String customerId, Long rank, String queueType) {
        return QueueStatusResponse.builder()
                .customerId(customerId)
                .waitingRank(rank)
                .queueType(queueType)
                .estimatedWaitMinutes(rank != null ? rank * 5 : null)
                .build();
    }
}
