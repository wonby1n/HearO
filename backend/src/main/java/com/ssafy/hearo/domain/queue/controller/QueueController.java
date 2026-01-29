package com.ssafy.hearo.domain.queue.controller;

import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import com.ssafy.hearo.domain.queue.dto.RegistrationRequest;
import com.ssafy.hearo.domain.queue.dto.RegistrationResponse;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import com.ssafy.hearo.global.util.MockUserIdExtractor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
@Slf4j
public class QueueController {

    private final QueueService queueService;
    private final MockUserIdExtractor userIdExtractor;
    private final CustomerRepository customerRepository;
    private final RegistrationRepository registrationRepository;

    /**
     * 상담 등록 및 대기열 진입
     * JWT 토큰으로 고객 식별 (X-User-ID 헤더 fallback 지원)
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest) {

        String customerIdStr = userIdExtractor.extract(httpRequest);
        log.info("상담 등록 요청: customerId={}", customerIdStr);

        Long registrationId = 0L;

        // JWT 인증된 Customer인 경우 Registration 저장
        try {
            Integer customerId = Integer.parseInt(customerIdStr);
            Customer customer = customerRepository.findById(customerId).orElse(null);

            if (customer != null) {
                // Registration 저장
                Registration registration = Registration.builder()
                        .customer(customer)
                        .symptom(request.getSymptom())
                        .errorCode(request.getErrorCode())
                        .modelCode(request.getModelCode())
                        .productCategory(request.getProductCategory())
                        .boughtAt(request.getBoughtAt() != null ? java.time.LocalDateTime.parse(request.getBoughtAt()) : null)
                        .build();
                registration = registrationRepository.save(registration);
                registrationId = registration.getId().longValue();
                log.info("Registration 저장 완료: registrationId={}", registrationId);
            }
        } catch (NumberFormatException e) {
            // X-User-ID 헤더 fallback (테스트 호환성)
            log.debug("customerId가 숫자가 아님, Registration 저장 스킵: {}", customerIdStr);
        }

        // Queue에 등록
        QueueStatusResponse queueStatus = queueService.enqueue(customerIdStr);

        return ResponseEntity.ok(RegistrationResponse.of(registrationId, queueStatus));
    }

    /**
     * 현재 대기 순위 조회
     */
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getStatus(HttpServletRequest httpRequest) {
        String customerId = userIdExtractor.extract(httpRequest);

        return queueService.getWaitingRank(customerId)
                .map(rank -> {
                    var queueType = queueService.getQueueType(customerId)
                            .map(Enum::name)
                            .orElse(null);
                    return ResponseEntity.ok(QueueStatusResponse.of(customerId, rank, queueType));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 등록 취소 및 대기열 이탈
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancel(HttpServletRequest httpRequest) {
        String customerId = userIdExtractor.extract(httpRequest);

        boolean removed = queueService.remove(customerId);

        if (removed) {
            log.info("고객 {} 등록 취소", customerId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 대기열 통계 조회 (대시보드용)
     */
    @GetMapping("/stats")
    public ResponseEntity<QueueService.QueueSizes> getStats() {
        return ResponseEntity.ok(queueService.getQueueSizes());
    }
}
