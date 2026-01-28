package com.ssafy.hearo.domain.consultation.entity;

import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "consultations", indexes = {
        @Index(name = "idx_consultations_user_id", columnList = "user_id"),
        @Index(name = "idx_consultations_customer_id", columnList = "customer_id"),
        @Index(name = "idx_consultations_registration_id", columnList = "registration_id"),
        @Index(name = "idx_consultations_created_at", columnList = "created_at"),
        @Index(name = "idx_consultations_termination_reason", columnList = "termination_reason")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consultation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    // ========== 기본 정보 ==========

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 30)
    private String subtitle;

    @Column(name = "full_transcript", columnDefinition = "TEXT")
    private String fullTranscript;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "user_memo", columnDefinition = "TEXT")
    private String userMemo;

    // ========== 분석 데이터 ==========

    @Column(name = "profanity_count", nullable = false)
    private Integer profanityCount = 0;

    @Column(name = "avg_aggression_score", precision = 3, scale = 2)
    private BigDecimal avgAggressionScore;

    @Column(name = "max_aggression_score", precision = 3, scale = 2)
    private BigDecimal maxAggressionScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason", nullable = false, length = 30)
    private TerminationReason terminationReason = TerminationReason.NORMAL;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // ========== 연관 엔티티 (1:1 관계) ==========

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoiceRecording voiceRecording;

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConsultationRating rating;

    // ========== 생성자 ==========

    @Builder
    public Consultation(User user, Customer customer, Registration registration,
                        String title, String subtitle) {
        this.user = user;
        this.customer = customer;
        this.registration = registration;
        this.title = title;
        this.subtitle = subtitle;
        this.profanityCount = 0;
        this.terminationReason = TerminationReason.NORMAL;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * STT 텍스트 추가 (실시간 업데이트용)
     */
    public void appendTranscript(String text) {
        if (this.fullTranscript == null) {
            this.fullTranscript = text;
        } else {
            this.fullTranscript += "\n" + text;
        }
    }

    /**
     * AI 요약 저장
     */
    public void updateAiSummary(String summary) {
        this.aiSummary = summary;
    }

    /**
     * 상담원 메모 저장
     */
    public void updateUserMemo(String memo) {
        this.userMemo = memo;
    }

    /**
     * 분석 데이터 업데이트 (상담 종료 시)
     */
    public void updateAnalysisData(Integer profanityCount, 
                                   BigDecimal avgAggressionScore,
                                   BigDecimal maxAggressionScore) {
        this.profanityCount = profanityCount != null ? profanityCount : 0;
        this.avgAggressionScore = avgAggressionScore;
        this.maxAggressionScore = maxAggressionScore;
    }

    /**
     * 상담 종료 처리
     */
    public void endConsultation(TerminationReason reason, Integer durationSeconds) {
        this.terminationReason = reason;
        this.durationSeconds = durationSeconds;
    }

    /**
     * 욕설 카운트 증가
     */
    public void incrementProfanityCount() {
        this.profanityCount++;
    }

    // ========== 연관관계 편의 메서드 ==========

    public void setVoiceRecording(VoiceRecording voiceRecording) {
        this.voiceRecording = voiceRecording;
        voiceRecording.setConsultation(this);
    }

    public void setRating(ConsultationRating rating) {
        this.rating = rating;
        rating.setConsultation(this);
    }
}
