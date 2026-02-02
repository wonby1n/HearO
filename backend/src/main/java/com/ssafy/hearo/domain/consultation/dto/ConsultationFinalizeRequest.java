package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.TerminationReason;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ConsultationFinalizeRequest {
    private String fullTranscript;
    private String userMemo;

    private Integer profanityCount;
    private BigDecimal avgAggressionScore;
    private BigDecimal maxAggressionScore;

    private TerminationReason terminationReason;
    private Integer durationSeconds;
}
