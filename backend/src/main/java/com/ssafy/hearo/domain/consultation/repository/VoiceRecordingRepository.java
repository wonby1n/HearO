package com.ssafy.hearo.domain.consultation.repository;

import com.ssafy.hearo.domain.consultation.entity.VoiceRecording;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoiceRecordingRepository extends JpaRepository<VoiceRecording, Long> {
    Optional<VoiceRecording> findByConsultationId(Integer consultationId);
}
