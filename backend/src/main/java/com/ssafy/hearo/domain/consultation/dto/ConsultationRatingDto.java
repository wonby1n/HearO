package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.ConsultationRating;
import lombok.*;

import java.math.BigDecimal;

public class ConsultationRatingDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private BigDecimal processRating;
        private BigDecimal solutionRating;
        private BigDecimal kindnessRating;
        private String feedback;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long ratingId;
        private Integer consultationId;
        private BigDecimal processRating;
        private BigDecimal solutionRating;
        private BigDecimal kindnessRating;
        private String feedback;

        public static Response from(ConsultationRating entity) {
            return Response.builder()
                    .ratingId(entity.getId())
                    .consultationId(entity.getConsultation().getId())
                    .processRating(entity.getProcessRating())
                    .solutionRating(entity.getSolutionRating())
                    .kindnessRating(entity.getKindnessRating())
                    .feedback(entity.getFeedback())
                    .build();
        }
    }
}
