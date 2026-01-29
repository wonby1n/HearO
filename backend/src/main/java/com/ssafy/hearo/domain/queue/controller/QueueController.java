package com.ssafy.hearo.domain.queue.controller;

import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.global.util.MockUserIdExtractor;
import jakarta.servlet.http.HttpServletRequest;
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

    // register 메서드는 RegistrationController로 이사감! 👋

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
     * 대기열 이탈 (접수 취소 아님, 단순 줄 서기 취소)
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancel(HttpServletRequest httpRequest) {
        String customerId = userIdExtractor.extract(httpRequest);

        boolean removed = queueService.remove(customerId);
        if (removed) {
            log.info("고객 {} 대기열 이탈", customerId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 대기열 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<QueueService.QueueSizes> getStats() {
        return ResponseEntity.ok(queueService.getQueueSizes());
    }
}