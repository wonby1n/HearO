package com.ssafy.hearo.domain.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AskResponse {

    private String answer;
    private List<RetrievedDoc> contexts = new ArrayList<>();

    public AskResponse() {}

    public AskResponse(String answer, List<RetrievedDoc> contexts) {
        this.answer = answer;
        this.contexts = contexts;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<RetrievedDoc> getContexts() {
        return contexts;
    }

    public void setContexts(List<RetrievedDoc> contexts) {
        this.contexts = contexts;
    }

    public static class RetrievedDoc {
        private String id;
        private String text;
        private double score;
        private String source;

        public RetrievedDoc() {}

        public RetrievedDoc(String id, String text, double score, String source) {
            this.id = id;
            this.text = text;
            this.score = score;
            this.source = source;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}