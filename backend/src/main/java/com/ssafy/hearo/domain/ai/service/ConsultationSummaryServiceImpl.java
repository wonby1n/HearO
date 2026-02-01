package com.ssafy.hearo.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.hearo.domain.ai.client.GmsClient;
import com.ssafy.hearo.domain.ai.dto.ChatCompletionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationSummaryServiceImpl implements ConsultationSummaryService {

    private final GmsClient gmsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CHAT_MODEL = "gpt-4.1-mini";

    @Override
    public GeneratedConsultationContent generateContent(String transcript) {
        String systemPrompt = """
                너는 콜센터 상담 STT를 요약하는 AI다.
                기록에 없는 내용은 절대 추측하지 마.
                반드시 한국어로 작성해.
                
                아래 JSON만 출력해. JSON 외의 텍스트는 절대 출력하지 마.
                title, subtitle은 각 30자 이내.
                aiSummary는 아래 형식으로 작성해.
                - 이슈 요약:
                - 고객 요청:
                - 상담원 안내:
                - 후속 조치:
                
                출력 JSON 스키마:
                {"title":"...","subtitle":"...","aiSummary":"..."}
                """;

        String userPrompt = """
                다음은 상담 STT 전문이다.
                이 내용을 기반으로만 요약하라.

                [STT]
                %s
                """.formatted(transcript == null ? "" : transcript);

        ChatCompletionsDto.Request request = new ChatCompletionsDto.Request(
                CHAT_MODEL,
                List.of(
                        new ChatCompletionsDto.Message("system", systemPrompt),
                        new ChatCompletionsDto.Message("user", userPrompt)
                ),
                0.2,
                650
        );

        String raw = gmsClient.summarize(request).trim();

        // 1) JSON 파싱 시도
        try {
            JsonNode node = objectMapper.readTree(raw);
            String title = safe30(node.path("title").asText("상담 요약"));
            String subtitle = safe30(node.path("subtitle").asText(""));
            String summary = node.path("aiSummary").asText("").trim();
            if (summary.isEmpty()) {
                summary = fallbackBulletSummary(transcript);
            }
            return new GeneratedConsultationContent(title, subtitle, summary);
        } catch (JsonProcessingException ignore) {
            // 2) JSON이 아닐 경우: 기존 bullet 요약으로 fallback
            String summary = fallbackBulletSummary(transcript);
            return new GeneratedConsultationContent("상담 요약", "", summary);
        }
    }

    private String safe30(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        return trimmed.length() <= 30 ? trimmed : trimmed.substring(0, 30);
    }

    private String fallbackBulletSummary(String transcript) {
        String systemPrompt = """
                너는 콜센터 상담 STT를 요약하는 AI다.
                기록에 없는 내용은 절대 추측하지 마.
                한국어로 아래 형식을 유지해라.

                - 이슈 요약:
                - 고객 요청:
                - 상담원 안내:
                - 후속 조치:
                """;

        String userPrompt = """
                다음은 상담 STT 전문이다.
                이 내용을 기반으로만 요약하라.

                [STT]
                %s
                """.formatted(transcript == null ? "" : transcript);

        ChatCompletionsDto.Request req = new ChatCompletionsDto.Request(
                CHAT_MODEL,
                List.of(
                        new ChatCompletionsDto.Message("system", systemPrompt),
                        new ChatCompletionsDto.Message("user", userPrompt)
                ),
                0.2,
                600
        );

        return gmsClient.summarize(req).trim();
    }
}
