package com.ssafy.hearo.domain.registration.repository;

import com.ssafy.hearo.domain.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    @Query("""
           select r
           from Registration r
           join fetch r.customer
           join fetch r.product
           where r.id = :registrationId
           """)
    Optional<Registration> findDetailById(Integer registrationId);

    Optional<Registration> findTopByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    /**
     * 고객의 최신 접수 정보를 Product와 함께 조회 (Lazy 로딩 방지)
     */
    @Query("""
           select r
           from Registration r
           join fetch r.product
           where r.customer.id = :customerId
           order by r.createdAt desc
           limit 1
           """)
    Optional<Registration> findLatestByCustomerIdWithProduct(Integer customerId);
}