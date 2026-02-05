package com.ssafy.hearo.domain.ai.controller;

import com.ssafy.hearo.domain.ai.dto.ConsultationSummaryRequest;
import com.ssafy.hearo.domain.ai.dto.ConsultationSummaryResponse;
import com.ssafy.hearo.domain.ai.service.ConsultationSummaryService;
import com.ssafy.hearo.domain.ai.service.GeneratedConsultationContent;
import com.ssafy.hearo.domain.consultation.entity.Consultation;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ConsultationAiController {

    private final ConsultationSummaryService summaryService;
    private final ConsultationRepository consultationRepository;

    @PostMapping("/summary")
    @Transactional
    public ResponseEntity<ConsultationSummaryResponse> generateSummary(
            @RequestBody ConsultationSummaryRequest request
    ) {
        if (request.getConsultationId() == null) {
            throw new IllegalArgumentException("consultationId는 필수입니다.");
        }

        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상담이 존재하지 않습니다."));

        // 1) transcript 결정: 프론트가 보내면 그걸 우선, 아니면 DB에 저장된 fullTranscript 사용
        String transcript = request.getFullTranscript();
        if (transcript == null || transcript.isBlank()) {
            transcript = consultation.getFullTranscript();
        }

        if (transcript == null || transcript.isBlank()) {
            throw new IllegalArgumentException("fullTranscript가 비어 있습니다. (요약 불가)");
        }

        // (선택) 프론트가 fullTranscript를 보내준 경우 DB에도 최신 반영
        if (request.getFullTranscript() != null && !request.getFullTranscript().isBlank()) {
            consultation.updateFullTranscript(request.getFullTranscript());
        }

        // 2) AI 요약 생성
        GeneratedConsultationContent content = summaryService.generateContent(transcript);

        // 3) DB 업데이트 (title/subtitle/aiSummary)
        consultation.updateTitleSubtitle(content.title(), content.subtitle());
        consultation.updateAiSummary(content.aiSummary());

        // JPA dirty checking으로 flush됨 (save 호출 없어도 되지만, 명시해도 OK)
        // consultationRepository.save(consultation);

        return ResponseEntity.ok(
                new ConsultationSummaryResponse(
                        content.title(),
                        content.subtitle(),
                        content.aiSummary()
                )
        );
    }
}
