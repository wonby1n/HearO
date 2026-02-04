package com.ssafy.hearo.domain.queue.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartbeatResponse {

    private boolean alive;
    private long remainingTtlSeconds;
    private String message;

    public static HeartbeatResponse success(long remainingTtl) {
        return HeartbeatResponse.builder()
                .alive(true)
                .remainingTtlSeconds(remainingTtl)
                .message("Lease renewed")
                .build();
    }

    public static HeartbeatResponse expired() {
        return HeartbeatResponse.builder()
                .alive(false)
                .remainingTtlSeconds(0)
                .message("Lease expired or invalid ticket")
                .build();
    }
}
