package com.ssafy.hearo.domain.consultation.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "consultation_ratings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_consultation_ratings_consultation", columnNames = "consultation_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationRating extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 관계의 주인 (FK를 가짐)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal processRating;   // 상담 과정

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal solutionRating;  // 해결 방법

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal kindnessRating;  // 친절도

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Builder
    public ConsultationRating(Consultation consultation, BigDecimal processRating,
                              BigDecimal solutionRating, BigDecimal kindnessRating, String feedback) {
        validateAllRatings(processRating, solutionRating, kindnessRating);
        this.consultation = consultation;
        this.processRating = processRating;
        this.solutionRating = solutionRating;
        this.kindnessRating = kindnessRating;
        this.feedback = feedback;
    }

    // ========== 비즈니스 메서드 (수정 로직) ==========

    public void update(BigDecimal processRating, BigDecimal solutionRating,
                       BigDecimal kindnessRating, String feedback) {
        validateAllRatings(processRating, solutionRating, kindnessRating);
        this.processRating = processRating;
        this.solutionRating = solutionRating;
        this.kindnessRating = kindnessRating;
        this.feedback = feedback;
    }

    // ========== 검증 로직 ==========

    private void validateAllRatings(BigDecimal... ratings) {
        for (BigDecimal rating : ratings) {
            if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(new BigDecimal("5.0")) > 0) {
                throw new IllegalArgumentException("모든 평점은 1.0에서 5.0 사이여야 합니다.");
            }
        }
    }

    // 연관관계 편의 메서드를 위한 Setter (같은 패키지나 서비스에서 호출 가능하도록)
    protected void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }
}