package com.ssafy.hearo.domain.consultation.repository;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {

    List<Consultation> findTop3ByCustomer_IdOrderByCreatedAtDesc(Integer customerId);
}
