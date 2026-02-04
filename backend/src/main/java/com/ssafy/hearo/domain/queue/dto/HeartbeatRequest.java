package com.ssafy.hearo.domain.queue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HeartbeatRequest {

    @NotBlank(message = "queueTicket은 필수입니다")
    private String queueTicket;

    public HeartbeatRequest(String queueTicket) {
        this.queueTicket = queueTicket;
    }
}
