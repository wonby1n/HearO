package com.ssafy.hearo.domain.ai.service;

/**
 * 상담 STT 요약/메타 생성 서비스
 */
public interface ConsultationSummaryService {

    /**
     * 상담 STT 전문(full transcript)을 기반으로
     * title/subtitle/aiSummary 를 생성한다.
     */
    GeneratedConsultationContent generateContent(String transcript);

    /**
     * (기존 호환) 요약 문자열만 필요할 때 사용
     */
    default String generateSummary(String transcript) {
        return generateContent(transcript).aiSummary();
    }
}
