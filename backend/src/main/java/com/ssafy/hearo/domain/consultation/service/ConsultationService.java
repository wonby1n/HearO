package com.ssafy.hearo.domain.consultation.service;

import com.ssafy.hearo.domain.consultation.dto.ConsultationSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConsultationService {
    public List<ConsultationSummaryResponse> getLatest3ByCustomerId(Integer customerId);
}
