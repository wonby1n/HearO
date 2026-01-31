package com.ssafy.hearo.domain.ai.service;

/**
 * LLM이 생성한 상담 메타/요약 결과
 */
public record GeneratedConsultationContent(
        String title,
        String subtitle,
        String aiSummary
) {
}
