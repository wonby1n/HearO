package com.ssafy.hearo.domain.consultation.dto;

import com.ssafy.hearo.domain.consultation.entity.VoiceRecording;
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String fileUrl;
        private Long fileSize;
        private Integer durationSeconds;

        public static Response from(VoiceRecording entity) {
            if (entity == null) return null;
            return Response.builder()
                    .fileUrl(entity.getFileUrl())
                    .fileSize(entity.getFileSize())
                    .durationSeconds(entity.getDurationSeconds())
                    .build();
        }
    }
}
