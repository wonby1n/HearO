package com.ssafy.hearo.domain.consultation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConsultationStartRequest {
    private Integer customerId;
    private Integer registrationId;
}
