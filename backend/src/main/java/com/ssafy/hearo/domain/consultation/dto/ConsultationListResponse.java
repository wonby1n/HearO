package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import com.ssafy.hearo.domain.consultation.entity.TerminationReason;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ConsultationListResponse {
    private Integer consultationId;
    private String customerName;
    private String customerPhone;
    private String productCategory;
    private String title;
    private String subtitle;
    private String aiSummary;
    private String fullTranscript;
    private String symptom;
    private String errorCode;
    private TerminationReason terminationReason;
    private Integer durationSeconds;
    private Integer profanityCount;
    private BigDecimal avgAggressionScore;
    private LocalDateTime createdAt;

    // 추가된 필드
    private ConsultationRatingDto.Response rating;
    private VoiceRecordingDto.Response voiceRecording;

    public static ConsultationListResponse from(Consultation consultation) {
        return ConsultationListResponse.builder()
                .consultationId(consultation.getId())
                .customerName(consultation.getCustomer().getName())
                .customerPhone(consultation.getCustomer().getPhone())
                .productCategory(consultation.getRegistration().getProduct().getCategory())
                .title(consultation.getTitle())
                .subtitle(consultation.getSubtitle())
                .aiSummary(consultation.getAiSummary())
                .fullTranscript(consultation.getFullTranscript())
                .symptom(consultation.getRegistration().getSymptom())
                .errorCode(consultation.getRegistration().getErrorCode())
                .terminationReason(consultation.getTerminationReason())
                .durationSeconds(consultation.getDurationSeconds())
                .profanityCount(consultation.getProfanityCount())
                .avgAggressionScore(consultation.getAvgAggressionScore())
                .createdAt(consultation.getCreatedAt())
                .rating(consultation.getRating() != null ? ConsultationRatingDto.Response.from(consultation.getRating()) : null)
                .voiceRecording(consultation.getVoiceRecording() != null ? VoiceRecordingDto.Response.from(consultation.getVoiceRecording()) : null)
                .build();
    }
}
