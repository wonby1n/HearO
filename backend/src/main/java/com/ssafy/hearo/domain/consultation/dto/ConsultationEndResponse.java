package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationEndResponse {
    private Integer consultationId;

    public static ConsultationEndResponse from(Consultation c) {
        return ConsultationEndResponse.builder()
                .consultationId(c.getId())
                .build();
    }
}
