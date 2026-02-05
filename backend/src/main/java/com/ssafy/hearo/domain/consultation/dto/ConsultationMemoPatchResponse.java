package com.ssafy.hearo.domain.consultation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationMemoPatchResponse {
    private Integer consultationId;
    private String userMemo;
}
