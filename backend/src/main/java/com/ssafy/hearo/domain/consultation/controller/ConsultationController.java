package com.ssafy.hearo.domain.consultation.controller;

import com.ssafy.hearo.domain.consultation.dto.*;
import com.ssafy.hearo.domain.consultation.service.ConsultationRatingService;
import com.ssafy.hearo.domain.consultation.service.ConsultationService;
import com.ssafy.hearo.global.common.response.BaseResponse; // ★ import 추가
import com.ssafy.hearo.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationRatingService ratingService;

    @PostMapping
    public ResponseEntity<BaseResponse<ConsultationStartResponse>> start(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ConsultationStartRequest request
    ) {
        if (userDetails == null) throw new IllegalArgumentException("인증 정보가 없습니다.");
        ConsultationStartResponse response = consultationService.startConsultation(userDetails.getId(), request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/latest")
    public ResponseEntity<BaseResponse<List<ConsultationSummaryResponse>>> getLatestConsultations(
            @RequestParam Integer customerId
    ) {
        List<ConsultationSummaryResponse> result = consultationService.getLatest3ByCustomerId(customerId);
        return ResponseEntity.ok(BaseResponse.success(result));
    }


    // ========== 상담 종료 후 추가정보 업데이트(PATCH) ==========

    @PatchMapping("/{consultationId}/end")
    public ResponseEntity<BaseResponse<ConsultationEndResponse>> finalizeAfterMatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer consultationId,
            @RequestBody ConsultationFinalizeRequest request
    ) {
        if (userDetails == null) throw new IllegalArgumentException("인증 정보가 없습니다.");
        ConsultationEndResponse response = consultationService.finalizeConsultation(consultationId, userDetails.getId(), request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PatchMapping("/{consultationId}")
    public ResponseEntity<BaseResponse<Void>> patchAfterEnd(
            @PathVariable Integer consultationId,
            @RequestBody ConsultationPatchRequest request
    ) {
        consultationService.patchConsultation(consultationId, request);
        return ResponseEntity.ok(BaseResponse.success());
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

    // ========== 내 상담 기록 조회 ==========
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<Page<ConsultationListResponse>>> getMyConsultations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (userDetails == null) throw new IllegalArgumentException("인증 정보가 없습니다.");
        Page<ConsultationListResponse> result = consultationService.getMyConsultations(userDetails.getId(), pageable);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // ========== 특정 고객의 상담 기록 조회 ==========
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<BaseResponse<Page<ConsultationListResponse>>> getCustomerConsultations(
            @PathVariable Integer customerId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ConsultationListResponse> result = consultationService.getCustomerConsultations(customerId, pageable);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @PatchMapping("/{consultationId}/memo")
    public ResponseEntity<ConsultationMemoPatchResponse> patchMemo(
            @PathVariable Integer consultationId,
            @RequestBody ConsultationMemoPatchRequest request
    ) {
        return ResponseEntity.ok(
                consultationService.updateMemo(consultationId, request.getUserMemo())
        );
    }

}
