package com.ssafy.hearo.domain.consultation.service;

import com.ssafy.hearo.domain.consultation.dto.ConsultationRatingDto;
import com.ssafy.hearo.domain.consultation.entity.Consultation;
import com.ssafy.hearo.domain.consultation.entity.ConsultationRating;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRatingRepository;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationRatingService {

    private final ConsultationRatingRepository ratingRepository;
    private final ConsultationRepository consultationRepository;

    /**
     * 후기 등록
     */
    @Transactional
    public Long createRating(Integer consultationId, ConsultationRatingDto.Request request) {
        // 1. 상담 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담이 존재하지 않습니다."));

        // 2. 중복 등록 체크
        if (ratingRepository.findByConsultationId(consultationId).isPresent()) {
            throw new IllegalStateException("이미 후기가 등록된 상담입니다.");
        }

        // 3. 엔티티 생성
        ConsultationRating rating = ConsultationRating.builder()
                .consultation(consultation)
                .processRating(request.getProcessRating())
                .solutionRating(request.getSolutionRating())
                .kindnessRating(request.getKindnessRating())
                .feedback(request.getFeedback())
                .build();

        // 4. 연관관계 편의 메서드 (양방향 연결)
        consultation.setRating(rating);

        // 5. 저장
        ratingRepository.save(rating);

        return rating.getId();
    }

    /**
     * 후기 조회 (상담 ID 기준)
     */
    public ConsultationRatingDto.Response getRating(Integer consultationId) {
        ConsultationRating rating = ratingRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담의 후기가 없습니다."));
        return ConsultationRatingDto.Response.from(rating);
    }

    /**
     * 후기 수정 (상담 ID 기준) -> ★ 변경됨 ★
     */
    @Transactional
    public void updateRatingByConsultationId(Integer consultationId, ConsultationRatingDto.Request request) {
        // ratingId 대신 consultationId로 바로 찾기!
        ConsultationRating rating = ratingRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담의 후기가 존재하지 않습니다."));

        rating.update(
                request.getProcessRating(),
                request.getSolutionRating(),
                request.getKindnessRating(),
                request.getFeedback()
        );
    }

    /**
     * 후기 삭제 (상담 ID 기준) -> ★ 변경됨 ★
     */
    @Transactional
    public void deleteRatingByConsultationId(Integer consultationId) {
        // ratingId 대신 consultationId로 바로 찾기!
        ConsultationRating rating = ratingRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담의 후기가 존재하지 않습니다."));

        // 연관관계 끊기
        // (Consultation 엔티티에서 rating 필드를 null로 만들어주지 않으면,
        // 영속성 컨텍스트에 남아있는 부모 객체가 삭제된 자식을 계속 참조할 수 있음)
        if (rating.getConsultation() != null) {
            // Consultation에 setRating 메서드가 있다면 null 처리 해주면 좋음
            // rating.getConsultation().setRating(null);
        }

        ratingRepository.delete(rating);
    }
}