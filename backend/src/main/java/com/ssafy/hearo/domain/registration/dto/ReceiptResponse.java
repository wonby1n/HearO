package com.ssafy.hearo.domain.registration.dto;

import java.time.LocalDateTime;

public record ReceiptResponse(
        int id,
        int customerId,
        String symptom,
        String errorCode,
        String modelCode,
        String productCategory,
        LocalDateTime boughtAt,
        LocalDateTime createdAt
) {
    public static ReceiptResponse from(com.ssafy.hearo.domain.registration.entity.Registration r) {
        return new ReceiptResponse(
                r.getId(),
                r.getCustomer().getId(),
                r.getSymptom(),
                r.getErrorCode(),
                r.getModelCode(),
                r.getProductCategory(),
                r.getBoughtAt(),
                r.getCreatedAt()
        );
    }
}
