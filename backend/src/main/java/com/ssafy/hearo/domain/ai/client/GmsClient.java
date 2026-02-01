package com.ssafy.hearo.domain.ai.client;

import com.ssafy.hearo.domain.ai.dto.ChatCompletionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GmsClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${gms.url}")
    private String baseUrl;

    @Value("${gms.api-key:}")
    private String apiKey;

    public String summarize(ChatCompletionsDto.Request request) {
        ChatCompletionsDto.Response response = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
                .post()
                .uri("/chat/completions")
                .headers(h -> {
                    if (apiKey != null && !apiKey.isBlank()) {
                        h.setBearerAuth(apiKey);
                    }
                })
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionsDto.Response.class)
                .block();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new IllegalStateException("GMS 요약 응답이 비어있습니다.");
        }

        return response.getChoices().get(0).getMessage().getContent();
    }
}
