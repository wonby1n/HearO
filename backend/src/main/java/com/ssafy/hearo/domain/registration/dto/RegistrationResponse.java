package com.ssafy.hearo.domain.registration.dto;

import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationResponse {

    private Long registrationId; // DB에 저장된 접수 번호
    private Integer customerId; // 고객 ID
    private Long waitingRank; // 등록 직후의 초기 대기 순위
    private String queueType; // 어떤 대기열에 들어갔는지 알려줌 ** 필요 없음
    private Long estimatedWaitMinutes; // 어떤 대기열에 들어갔는지 알려줌 ** 필요 없음
    private String websocketTopic; //** 사용자가 실시간 순위를 받아보기 위해 구독해야할 웹소켓 주소를 알려줌

    public static RegistrationResponse of(Long registrationId, QueueStatusResponse queueStatus) {
        return RegistrationResponse.builder()
                .registrationId(registrationId)
                .customerId(Integer.parseInt(queueStatus.getCustomerId()))
                .waitingRank(queueStatus.getWaitingRank())
                .queueType(queueStatus.getQueueType())
                .estimatedWaitMinutes(queueStatus.getEstimatedWaitMinutes())
                .websocketTopic("/topic/queue-rank/" + queueStatus.getCustomerId()) // 사용자별로 고유한 알림 채널 주소를 생성하여 담아줌
                .build();
    }
}
