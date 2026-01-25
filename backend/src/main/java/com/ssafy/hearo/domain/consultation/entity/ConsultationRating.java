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
            @UniqueConstraint(name = "uk_consultation_ratings_consultation", 
                             columnNames = "consultation_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationRating extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Builder
    public ConsultationRating(Consultation consultation, BigDecimal rating, String feedback) {
        this.consultation = consultation;
        this.rating = rating;
        this.feedback = feedback;
    }

    // ========== 연관관계 편의 메서드 ==========

    protected void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    // ========== 비즈니스 메서드 ==========

    public void updateRating(BigDecimal rating, String feedback) {
        validateRating(rating);
        this.rating = rating;
        this.feedback = feedback;
    }

    private void validateRating(BigDecimal rating) {
        if (rating != null) {
            if (rating.compareTo(BigDecimal.ONE) < 0 || 
                rating.compareTo(new BigDecimal("5.0")) > 0) {
                throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
            }
        }
    }
}
