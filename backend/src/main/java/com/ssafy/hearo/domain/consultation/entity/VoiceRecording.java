package com.ssafy.hearo.domain.consultation.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voice_recordings",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_voice_recordings_consultation", columnNames = "consultation_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceRecording extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Builder
    public VoiceRecording(Consultation consultation, String fileUrl, 
                          Long fileSize, Integer durationSeconds) {
        this.consultation = consultation;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.durationSeconds = durationSeconds;
    }

    // ========== 연관관계 편의 메서드 ==========

    protected void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    // ========== 비즈니스 메서드 ==========

    public void updateFileInfo(String fileUrl, Long fileSize, Integer durationSeconds) {
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.durationSeconds = durationSeconds;
    }
}
