package com.ssafy.hearo.domain.customer.repository;

import com.ssafy.hearo.domain.customer.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    /**
     * 특정 고객이 블랙리스트로 등록한 상담원 ID 목록 조회
     */
    @Query("SELECT b.user.id FROM Blacklist b WHERE b.customer.id = :customerId")
    Set<Long> findBlockedCounselorIdsByCustomerId(@Param("customerId") Long customerId);

    /**
     * 특정 고객과 특정 상담원 간의 블랙리스트 관계 존재 여부
     */
    boolean existsByCustomerIdAndUserId(Long customerId, Long userId);

    /**
     * 특정 고객이 주어진 상담원 목록 중 매칭 가능한 상담원이 있는지 확인
     * (블랙리스트에 없는 상담원이 하나라도 있으면 true)
     */
    @Query("""
        SELECT CASE WHEN COUNT(b) < :counselorCount THEN true ELSE false END
        FROM Blacklist b
        WHERE b.customer.id = :customerId AND b.user.id IN :counselorIds
        """)
    boolean hasMatchableCounselor(
            @Param("customerId") Long customerId,
            @Param("counselorIds") Set<Long> counselorIds,
            @Param("counselorCount") long counselorCount);
}
