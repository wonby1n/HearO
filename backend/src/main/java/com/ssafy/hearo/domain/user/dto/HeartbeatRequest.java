package com.ssafy.hearo.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record HeartbeatRequest(
        @NotNull(message = "isHeartbeatActive는 필수입니다")
        Boolean isHeartbeatActive
) {
}
