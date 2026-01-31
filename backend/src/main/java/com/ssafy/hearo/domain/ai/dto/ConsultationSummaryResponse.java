package com.ssafy.hearo.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


public record ConsultationSummaryResponse(
        String title,
        String subtitle,
        String aiSummary
) {}
