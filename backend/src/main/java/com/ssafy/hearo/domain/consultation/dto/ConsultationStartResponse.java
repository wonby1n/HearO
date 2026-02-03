package com.ssafy.hearo.domain.consultation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationStartResponse {
    private Integer consultationId;

    public static ConsultationStartResponse of(Integer consultationId) {
        return ConsultationStartResponse.builder()
                .consultationId(consultationId)
                .build();
    }
}
