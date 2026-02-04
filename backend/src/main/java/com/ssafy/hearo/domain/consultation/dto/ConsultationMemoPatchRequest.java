package com.ssafy.hearo.domain.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ConsultationMemoPatchRequest {

    @NotBlank
    private String userMemo;
}
