package com.ssafy.hearo.domain.consultation.repository;

import com.ssafy.hearo.domain.consultation.entity.ConsultationRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ConsultationRatingRepository extends JpaRepository<ConsultationRating, Long> {

    // 1. 종합 평균 조회 ( (과정 + 해결 + 친절) / 3 의 전체 평균 )
    // 주의: 3.0 (실수)으로 나눠야 소수점이 유지됨
    @Query("SELECT AVG((r.processRating + r.solutionRating + r.kindnessRating) / 3.0) " +
            "FROM ConsultationRating r " +
            "WHERE r.consultation.user.id = :userId")
    BigDecimal findAverageRatingByUserId(@Param("userId") Long userId);

    // 3. 상담 ID로 후기 찾기
    Optional<ConsultationRating> findByConsultationId(Integer consultationId);
}