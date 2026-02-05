package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.Consultation;

import java.time.LocalDateTime;

public record ConsultationSummaryResponse(
        Integer consultationId,
        Integer customerId,
        Integer registrationId,
        String symptom,
        String errorCode,
        String title,
        String subtitle,
        String terminationReason,
        Integer durationSeconds,
        Integer profanityCount,
        LocalDateTime createdAt
) {
    public static ConsultationSummaryResponse from(Consultation c) {
        return new ConsultationSummaryResponse(
                c.getId(),
                c.getCustomer().getId(),
                c.getRegistration().getId(),
                c.getRegistration().getSymptom(),
                c.getRegistration().getErrorCode(),
                c.getTitle(),
                c.getSubtitle(),
                c.getTerminationReason().name(),
                c.getDurationSeconds(),
                c.getProfanityCount(),
                c.getCreatedAt()
        );
    }
}
