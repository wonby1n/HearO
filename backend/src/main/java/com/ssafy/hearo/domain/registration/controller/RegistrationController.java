package com.ssafy.hearo.domain.registration.controller;

import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import com.ssafy.hearo.domain.registration.dto.RegistrationRequest;
import com.ssafy.hearo.domain.queue.dto.RegistrationResponse;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.registration.dto.RegistrationDetailResponse;
import com.ssafy.hearo.domain.registration.service.RegistrationService;
import com.ssafy.hearo.global.util.MockUserIdExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations") // 경로 변경 (RESTful하게)
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
    public ResponseEntity<RegistrationResponse> createRegistration(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest) {

        String customerIdStr = userIdExtractor.extract(httpRequest);
        log.info("상담 접수 요청: customerId={}", customerIdStr);

        Long registrationId = 0L;

        try {
            Integer customerId = Integer.parseInt(customerIdStr);
            
            // 1. 접수 정보 저장 (RegistrationService 위임)
            registrationId = registrationService.createRegistration(customerId, request);
            
            log.info("접수 저장 완료: registrationId={}", registrationId);

        } catch (NumberFormatException e) {
            log.debug("비회원/테스트 유저 진입 (저장 스킵): {}", customerIdStr);
        } catch (Exception e) {
            log.error("접수 저장 중 예외 발생", e);
            // 필요하다면 여기서 throw e 해서 GlobalExceptionHandler가 잡게 하거나 처리
        }

        // 2. 대기열 등록 (QueueService 위임)
        // 접수가 완료되었으니 줄을 세운다.
        QueueStatusResponse queueStatus = queueService.enqueue(customerIdStr);

        return ResponseEntity.ok(RegistrationResponse.of(registrationId, queueStatus));
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<RegistrationDetailResponse> getRegistrationDetail(
            @PathVariable("registrationId") Integer registrationId) {

        RegistrationDetailResponse response = registrationService.getRegistrationDetail(registrationId);
        return ResponseEntity.ok(response);
    }
}