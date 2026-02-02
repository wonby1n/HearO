package com.ssafy.hearo.domain.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class VoiceRecordingDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String fileUrl;
        private Long fileSize;
        private Integer durationSeconds;
    }
}
