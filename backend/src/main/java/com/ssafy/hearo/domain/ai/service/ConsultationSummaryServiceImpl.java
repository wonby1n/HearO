package com.ssafy.hearo.domain.ai.service;

import com.ssafy.hearo.domain.ai.client.GmsClient;
import com.ssafy.hearo.domain.ai.dto.ChatCompletionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationSummaryServiceImpl implements ConsultationSummaryService {

    private final GmsClient gmsClient;

    private static final String CHAT_MODEL = "gpt-4.1-mini";

    public String generateSummary(String transcript) {
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
                """.formatted(transcript);

        ChatCompletionsDto.Request request =
                new ChatCompletionsDto.Request(
                        CHAT_MODEL,
                        List.of(
                                new ChatCompletionsDto.Message("system", systemPrompt),
                                new ChatCompletionsDto.Message("user", userPrompt)
                        ),
                        0.2,
                        600
                );

        return gmsClient.summarize(request).trim();
    }
}
