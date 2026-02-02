package com.ssafy.hearo.domain.consultation.service;

import com.ssafy.hearo.domain.consultation.dto.ConsultationEndRequest;
import com.ssafy.hearo.domain.consultation.dto.ConsultationEndResponse;
import com.ssafy.hearo.domain.consultation.dto.ConsultationPatchRequest;
import com.ssafy.hearo.domain.consultation.dto.ConsultationSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConsultationService {
    public List<ConsultationSummaryResponse> getLatest3ByCustomerId(Integer customerId);

    ConsultationEndResponse endAndSave(Long userId, ConsultationEndRequest request);

    void patchConsultation(Integer consultationId, ConsultationPatchRequest request);
}
