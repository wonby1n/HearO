package com.ssafy.hearo.domain.consultation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConsultationPatchRequest {
    //Voice와 Rating은 따로 분리해야함.
    private VoiceRecordingDto.Request voiceRecording;
    private ConsultationRatingDto.Request rating;
}
