package com.ssafy.hearo.domain.ai.controller;

import com.ssafy.hearo.domain.ai.dto.ConsultationSummaryRequest;
import com.ssafy.hearo.domain.ai.dto.ConsultationSummaryResponse;
import com.ssafy.hearo.domain.ai.service.ConsultationSummaryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ConsultationAiController {

    private final ConsultationSummaryServiceImpl summaryService;

    @PostMapping("/summary")
    public ResponseEntity<ConsultationSummaryResponse> generateSummary(
            @RequestBody ConsultationSummaryRequest request
    ) {
        var content = summaryService.generateContent(request.getFullTranscript());
        return ResponseEntity.ok(
                new ConsultationSummaryResponse(
                        content.title(),
                        content.subtitle(),
                        content.aiSummary()
                )
        );
    }
}
