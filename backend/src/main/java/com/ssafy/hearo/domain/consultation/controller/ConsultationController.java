package com.ssafy.hearo.domain.consultation.controller;

import com.ssafy.hearo.domain.consultation.dto.ConsultationRatingDto;
import com.ssafy.hearo.domain.consultation.dto.ConsultationSummaryResponse;
import com.ssafy.hearo.domain.consultation.service.ConsultationRatingService;
import com.ssafy.hearo.domain.consultation.service.ConsultationService;
import com.ssafy.hearo.global.common.response.BaseResponse; // ★ import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationRatingService ratingService;

    // 임시 Mock ID
    private final Integer mockId = 123;

    // ========== 기존 상담 로직 ==========

    @GetMapping("/latest")
    public ResponseEntity<BaseResponse<List<ConsultationSummaryResponse>>> getLatestConsultations() {
        Integer customerId = mockId;
        List<ConsultationSummaryResponse> result = consultationService.getLatest3ByCustomerId(customerId);

        // BaseResponse.success(result)로 감싸기
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // ========== ★ 추가된 후기(Rating) 로직 ★ ==========

    // 1. 후기 등록
    @PostMapping("/{consultationId}/rating")
    public ResponseEntity<BaseResponse<Long>> createRating(
            @PathVariable Integer consultationId,
            @RequestBody ConsultationRatingDto.Request request) {

        Long ratingId = ratingService.createRating(consultationId, request);

        // 데이터(ratingId)가 있는 성공 응답
        return ResponseEntity.ok(BaseResponse.success(ratingId));
    }

    // 2. 후기 조회
    @GetMapping("/{consultationId}/rating")
    public ResponseEntity<BaseResponse<ConsultationRatingDto.Response>> getRating(
            @PathVariable Integer consultationId) {

        ConsultationRatingDto.Response response = ratingService.getRating(consultationId);

        // 데이터(DTO)가 있는 성공 응답
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    // 3. 후기 수정
    @PutMapping("/{consultationId}/rating")
    public ResponseEntity<BaseResponse<Void>> updateRating(
            @PathVariable Integer consultationId,
            @RequestBody ConsultationRatingDto.Request request) {

        ratingService.updateRatingByConsultationId(consultationId, request);

        // 데이터가 없는 성공 응답 (BaseResponse.success() 사용)
        return ResponseEntity.ok(BaseResponse.success());
    }

    // 4. 후기 삭제
    @DeleteMapping("/{consultationId}/rating")
    public ResponseEntity<BaseResponse<Void>> deleteRating(@PathVariable Integer consultationId) {

        ratingService.deleteRatingByConsultationId(consultationId);

        // 데이터가 없는 성공 응답
        return ResponseEntity.ok(BaseResponse.success());
    }
}