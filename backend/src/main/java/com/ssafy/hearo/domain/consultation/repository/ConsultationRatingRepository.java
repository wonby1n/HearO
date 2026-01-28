package com.ssafy.hearo.domain.consultation.repository;

import com.ssafy.hearo.domain.consultation.entity.ConsultationRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ConsultationRatingRepository extends JpaRepository<ConsultationRating, Long> {

    // 내 상담 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM ConsultationRating r WHERE r.consultation.user.id = :userId")
    BigDecimal findAverageRatingByUserId(@Param("userId") Long userId);
}