package com.ssafy.hearo.domain.consultation.repository;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {

    List<Consultation> findTop3ByCustomer_IdOrderByCreatedAtDesc(Integer customerId);

    // 특정 기간(이번 주) 내의 내 상담 기록 조회
    List<Consultation> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 내 총 상담 시간(초) 합계
    @Query("SELECT SUM(c.durationSeconds) FROM Consultation c WHERE c.user.id = :userId")
    Long sumDurationByUserId(@Param("userId") Long userId);

    // 내 상담 기록 페이징 조회
    Page<Consultation> findAllByUserId(Long userId, Pageable pageable);
}
