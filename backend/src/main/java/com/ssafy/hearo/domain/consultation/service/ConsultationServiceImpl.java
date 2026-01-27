package com.ssafy.hearo.domain.consultation.service;

import com.ssafy.hearo.domain.consultation.dto.ConsultationSummaryResponse;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationServiceImpl implements ConsultationService{
    private final ConsultationRepository consultationRepository;

    public List<ConsultationSummaryResponse> getLatest3ByCustomerId(Integer customerId) {
        return consultationRepository.findTop3ByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ConsultationSummaryResponse::from)
                .toList();
    }
}
