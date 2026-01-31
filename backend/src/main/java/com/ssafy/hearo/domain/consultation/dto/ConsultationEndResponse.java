package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationEndResponse {
    private Integer consultationId;
    private String title;
    private String subtitle;
    private String aiSummary;

    public static ConsultationEndResponse from(Consultation c) {
        return ConsultationEndResponse.builder()
                .consultationId(c.getId())
                .title(c.getTitle())
                .subtitle(c.getSubtitle())
                .aiSummary(c.getAiSummary())
                .build();
    }
}
