package com.ssafy.hearo.domain.consultation.service;

import com.ssafy.hearo.domain.ai.service.ConsultationSummaryService;
import com.ssafy.hearo.domain.ai.service.GeneratedConsultationContent;
import com.ssafy.hearo.domain.consultation.dto.ConsultationEndRequest;
import com.ssafy.hearo.domain.consultation.dto.ConsultationEndResponse;
import com.ssafy.hearo.domain.consultation.dto.ConsultationPatchRequest;
import com.ssafy.hearo.domain.consultation.dto.ConsultationSummaryResponse;
import com.ssafy.hearo.domain.consultation.dto.VoiceRecordingDto;
import com.ssafy.hearo.domain.consultation.entity.Consultation;
import com.ssafy.hearo.domain.consultation.entity.TerminationReason;
import com.ssafy.hearo.domain.consultation.entity.VoiceRecording;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import com.ssafy.hearo.domain.consultation.repository.VoiceRecordingRepository;
import com.ssafy.hearo.domain.customer.entity.Blacklist;
import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.BlacklistRepository;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationServiceImpl implements ConsultationService{
    private final ConsultationRepository consultationRepository;

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RegistrationRepository registrationRepository;
    private final VoiceRecordingRepository voiceRecordingRepository;
    private final ConsultationRatingService ratingService;
    private final ConsultationSummaryService summaryService;
    private final BlacklistRepository blacklistRepository;

    public List<ConsultationSummaryResponse> getLatest3ByCustomerId(Integer customerId) {
        return consultationRepository.findTop3ByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ConsultationSummaryResponse::from)
                .toList();
    }

    /**
     * 상담 종료 시점에 프론트에서 전달한 데이터로 Consultation을 생성/저장하고,
     * fullTranscript 기반으로 title/subtitle/aiSummary를 생성해 저장한다.
     */
    @Override
    @Transactional
    public ConsultationEndResponse endAndSave(Long userId, ConsultationEndRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        if (request.getCustomerId() == null || request.getRegistrationId() == null) {
            throw new IllegalArgumentException("customerId, registrationId는 필수입니다.");
        }
        if (request.getTerminationReason() == null) {
            throw new IllegalArgumentException("terminationReason은 필수입니다.");
        }
        if (request.getFullTranscript() == null || request.getFullTranscript().isBlank()) {
            throw new IllegalArgumentException("fullTranscript는 필수입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("상담원(user)이 존재하지 않습니다."));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객(customer)이 존재하지 않습니다."));
        Registration registration = registrationRepository.findById(request.getRegistrationId())
                .orElseThrow(() -> new IllegalArgumentException("접수(registration)가 존재하지 않습니다."));

        // 1) 먼저 저장 (LLM 장애로 저장이 실패하지 않도록 기본값으로 생성)
        Consultation consultation = Consultation.builder()
                .user(user)
                .customer(customer)
                .registration(registration)
                .title("상담 요약")
                .subtitle("")
                .build();

        consultation.updateFullTranscript(request.getFullTranscript());
        consultation.updateUserMemo(request.getUserMemo());
        consultation.updateAnalysisData(
                request.getProfanityCount(),
                request.getAvgAggressionScore(),
                request.getMaxAggressionScore()
        );
        TerminationReason reason = request.getTerminationReason();
        consultation.endConsultation(reason, request.getDurationSeconds());

        consultationRepository.save(consultation);
        blacklistIfNeeded(user, customer, request.getTerminationReason());
        // 2) LLM 생성 결과로 업데이트
        try {
            GeneratedConsultationContent generated = summaryService.generateContent(request.getFullTranscript());
            consultation.updateTitleSubtitle(generated.title(), generated.subtitle());
            consultation.updateAiSummary(generated.aiSummary());
        } catch (Exception e) {
            // 저장은 성공해야 하므로 요약 실패는 로그만 남기고 진행
            log.warn("AI summary generation failed. consultationId={}", consultation.getId(), e);
        }

        return ConsultationEndResponse.from(consultation);
    }
    /**
     * 블랙리스트 등록
     */
    private void blacklistIfNeeded(User user, Customer customer, TerminationReason reason) {
        if (reason != TerminationReason.PROFANITY_LIMIT &&
                reason != TerminationReason.AGGRESSION_LIMIT) {
            return;
        }

        Integer customerId = customer.getId();
        blacklistRepository.findByUserIdAndCustomerId(user.getId(), customerId)
                .ifPresentOrElse(
                        existing -> existing.updateReason(reason.name()),
                        () -> blacklistRepository.save(
                                Blacklist.builder()
                                        .user(user)
                                        .customer(customer)
                                        .reason(reason.name())
                                        .build()
                        )
                );
    }


    /**
     * 상담 종료 후 추가 정보(녹음파일/평점 등) 업데이트
     */
    @Override
    @Transactional
    public void patchConsultation(Integer consultationId, ConsultationPatchRequest request) {
        if (consultationId == null) {
            throw new IllegalArgumentException("consultationId는 필수입니다.");
        }
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담이 존재하지 않습니다."));

        // 1) voice recording upsert
        VoiceRecordingDto.Request vr = request != null ? request.getVoiceRecording() : null;
        if (vr != null) {
            VoiceRecording voiceRecording = voiceRecordingRepository.findByConsultationId(consultationId)
                    .orElseGet(() -> VoiceRecording.builder()
                            .consultation(consultation)
                            .fileUrl(null)
                            .fileSize(null)
                            .durationSeconds(null)
                            .build());

            voiceRecording.updateFileInfo(vr.getFileUrl(), vr.getFileSize(), vr.getDurationSeconds());
            consultation.setVoiceRecording(voiceRecording);
            voiceRecordingRepository.save(voiceRecording);
        }

        // 2) rating upsert (기존 ratingService 재사용)
        if (request != null && request.getRating() != null) {
            try {
                ratingService.createRating(consultationId, request.getRating());
            } catch (IllegalStateException dup) {
                // 이미 있으면 update
                ratingService.updateRatingByConsultationId(consultationId, request.getRating());
            }
        }
    }
}
