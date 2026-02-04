package com.ssafy.hearo.domain.consultation.service;

import com.ssafy.hearo.domain.consultation.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConsultationService {

    List<ConsultationSummaryResponse> getLatest3ByCustomerId(Integer customerId);

    ConsultationStartResponse startConsultation(Long userId, ConsultationStartRequest request);

    ConsultationEndResponse finalizeConsultation(Integer consultationId, Long userId, ConsultationFinalizeRequest request);

    void patchConsultation(Integer consultationId, ConsultationPatchRequest request);

    Page<ConsultationListResponse> getMyConsultations(Long userId, Pageable pageable);

    Page<ConsultationListResponse> getCustomerConsultations(Integer customerId, Pageable pageable);

    ConsultationMemoPatchResponse updateMemo(Integer consultationId, String memo);
}
