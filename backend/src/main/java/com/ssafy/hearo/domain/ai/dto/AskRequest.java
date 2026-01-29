package com.ssafy.hearo.domain.ai.dto;

public class AskRequest {
    private String question;

    public AskRequest() {}

    public AskRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
