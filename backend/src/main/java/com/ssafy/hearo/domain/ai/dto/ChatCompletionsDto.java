package com.ssafy.hearo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

public class ChatCompletionsDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private String model;
        private List<Message> messages;
        private Double temperature;

        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Response {
        private List<Choice> choices;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Choice {
        private Message message;
    }
}
