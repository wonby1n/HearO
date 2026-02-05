package com.ssafy.hearo.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsultationSummaryRequest {
    private Integer consultationId;
    private String fullTranscript;
}
