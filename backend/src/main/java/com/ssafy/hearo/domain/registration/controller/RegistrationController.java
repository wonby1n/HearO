package com.ssafy.hearo.domain.registration.controller;

import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import com.ssafy.hearo.domain.registration.dto.RegistrationRequest;
import com.ssafy.hearo.domain.queue.dto.RegistrationResponse;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.registration.dto.RegistrationDetailResponse;
import com.ssafy.hearo.domain.registration.service.RegistrationService;
import com.ssafy.hearo.global.common.response.BaseResponse; // BaseResponse 임포트!
import com.ssafy.hearo.global.util.MockUserIdExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;
    private final QueueService queueService;
    private final MockUserIdExtractor userIdExtractor;

    /**
     * 상담 접수 등록 및 대기열 진입
     */
    @PostMapping
    public ResponseEntity<BaseResponse<RegistrationResponse>> createRegistration(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest) {

        String customerIdStr = userIdExtractor.extract(httpRequest);
        log.info("상담 접수 요청: customerId={}", customerIdStr);

        Long registrationId = 0L;

        try {
            Integer customerId = Integer.parseInt(customerIdStr);
            registrationId = registrationService.createRegistration(customerId, request);
            log.info("접수 저장 완료: registrationId={}", registrationId);
        } catch (NumberFormatException e) {
            log.debug("비회원/테스트 유저 진입 (저장 스킵): {}", customerIdStr);
        } catch (Exception e) {
            log.error("접수 저장 중 예외 발생", e);
        }

        QueueStatusResponse queueStatus = queueService.enqueue(customerIdStr);

        // [수정] BaseResponse.success()로 감싸기
        return ResponseEntity.ok(BaseResponse.success(RegistrationResponse.of(registrationId, queueStatus)));
    }

    /**
     * 접수 상세 조회
     */
    @GetMapping("/{registrationId}")
    public ResponseEntity<BaseResponse<RegistrationDetailResponse>> getRegistrationDetail(
            @PathVariable("registrationId") Integer registrationId) {

        RegistrationDetailResponse response = registrationService.getRegistrationDetail(registrationId);

        // [수정] BaseResponse.success()로 감싸기
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}