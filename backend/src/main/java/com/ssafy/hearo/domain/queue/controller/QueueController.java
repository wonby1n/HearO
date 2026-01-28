package com.ssafy.hearo.domain.queue.controller;

import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import com.ssafy.hearo.domain.queue.dto.RegistrationRequest;
import com.ssafy.hearo.domain.queue.dto.RegistrationResponse;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.global.util.MockUserIdExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
@Slf4j
public class QueueController {

    private final QueueService queueService;
    private final MockUserIdExtractor userIdExtractor;

    /**
     * 상담 등록 및 대기열 진입
     * X-User-ID 헤더 (mock) 또는 JWT 토큰 (미래)으로 사용자 식별
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest) {

        String customerId = userIdExtractor.extract(httpRequest);
        log.info("상담 등록 요청: customerId={}", customerId);

        // TODO: Registration 엔티티를 DB에 저장 (기존 Registration 도메인과 통합)
        Long registrationId = 1L; // Placeholder

        QueueStatusResponse queueStatus = queueService.enqueue(customerId);

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
