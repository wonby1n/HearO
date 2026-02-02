package com.ssafy.hearo.domain.registration.dto;

import com.ssafy.hearo.domain.registration.entity.Registration;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record RegistrationDetailResponse(
        Long id,
        String symptom,
        String errorCode,
        LocalDateTime createdAt,

        // Customer 정보 포함 (추가)
        Integer customerId,
        String customerName,
        String customerPhone,

        // Product 정보 포함
        String productName,
        String modelCode,
        String productCategory,
        String productImageUrl,

        // 기기 상세 날짜
        LocalDate manufacturedAt,
        LocalDate warrantyEndsAt
) {
    public static RegistrationDetailResponse from(Registration r) {
        return RegistrationDetailResponse.builder()
                .id(r.getId().longValue())
                .symptom(r.getSymptom())
                .errorCode(r.getErrorCode())
                .createdAt(r.getCreatedAt())

                // 연관된 Customer 엔티티에서 정보 추출 (추가)
                .customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getName())
                .customerPhone(r.getCustomer().getPhone())

                // 연관된 Product 엔티티에서 정보 추출
                .productName(r.getProduct().getName())
                .modelCode(r.getProduct().getCode())
                .productCategory(r.getProduct().getCategory())
                .productImageUrl(r.getProduct().getImageUrl())

                .manufacturedAt(r.getManufacturedAt())
                .warrantyEndsAt(r.getWarrantyEndsAt())
                .build();
    }
}