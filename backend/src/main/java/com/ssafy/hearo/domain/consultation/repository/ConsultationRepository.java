package com.ssafy.hearo.domain.consultation.repository;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {

    List<Consultation> findTop3ByCustomer_IdOrderByCreatedAtDesc(Integer customerId);

    List<Consultation> findByCustomerIdOrderByCreatedAtDesc(Integer customerId, Pageable pageable);

    // 특정 기간(이번 주) 내의 내 상담 기록 조회
    List<Consultation> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 내 총 상담 시간(초) 합계
    @Query("SELECT SUM(c.durationSeconds) FROM Consultation c WHERE c.user.id = :userId")
    Long sumDurationByUserId(@Param("userId") Long userId);

    // ========== 매칭 가중치 계산용 쿼리 ==========

    /**
     * 특정 상담원-고객 간 과거 상담의 평균 평점 조회
     * (processRating + solutionRating + kindnessRating) / 3 의 평균
     */
    @Query("""
           SELECT AVG((r.processRating + r.solutionRating + r.kindnessRating) / 3.0)
           FROM Consultation c
           JOIN c.rating r
           WHERE c.user.id = :counselorId
           AND c.customer.id = :customerId
           """)
    BigDecimal findAvgRatingByCounselorAndCustomer(
            @Param("counselorId") Long counselorId,
            @Param("customerId") Integer customerId);

    /**
     * 특정 상담원-고객 간 과거 상담 횟수 조회
     */
    @Query("""
           SELECT COUNT(c)
           FROM Consultation c
           WHERE c.user.id = :counselorId
           AND c.customer.id = :customerId
           """)
    Long countByCounselorAndCustomer(
            @Param("counselorId") Long counselorId,
            @Param("customerId") Integer customerId);

    /**
     * 특정 상담원이 특정 카테고리 상품을 상담한 횟수 조회
     */
    @Query("""
           SELECT COUNT(c)
           FROM Consultation c
           JOIN c.registration reg
           JOIN reg.product p
           WHERE c.user.id = :counselorId
           AND p.category = :category
           """)
    Long countByCounselorAndCategory(
            @Param("counselorId") Long counselorId,
            @Param("category") String category);

    // 내 상담 기록 페이징 조회
    Page<Consultation> findAllByUserId(Long userId, Pageable pageable);

    // 특정 고객의 상담 기록 페이징 조회
    Page<Consultation> findAllByCustomerId(Integer customerId, Pageable pageable);

}
