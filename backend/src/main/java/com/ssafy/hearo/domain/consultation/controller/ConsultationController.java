package com.ssafy.hearo.domain.consultation.controller;


import com.ssafy.hearo.domain.consultation.dto.ConsultationSummaryResponse;
import com.ssafy.hearo.domain.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final Integer mockId = 123;
    private final ConsultationService consultationService;

    @GetMapping("/latest")
    public ResponseEntity<List<ConsultationSummaryResponse>> getLatestConsultations() {
        Integer customerId = mockId;
        return ResponseEntity.ok(consultationService.getLatest3ByCustomerId(customerId));
    }
}

