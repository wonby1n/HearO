package com.ssafy.hearo.domain.queue.controller;

import com.ssafy.hearo.domain.queue.dto.HeartbeatRequest;
import com.ssafy.hearo.domain.queue.dto.HeartbeatResponse;
import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import com.ssafy.hearo.domain.queue.service.QueueLeaseService;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.global.common.response.BaseResponse;
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
    private final QueueLeaseService queueLeaseService;
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
     * lease도 함께 삭제
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancel(HttpServletRequest httpRequest) {
        String customerId = userIdExtractor.extract(httpRequest);

        // lease 삭제 (큐 제거 전에 먼저 삭제)
        queueLeaseService.deleteLeaseByCustomerId(customerId);

        boolean removed = queueService.remove(customerId);
        if (removed) {
            log.info("고객 {} 대기열 이탈 (lease 포함 삭제)", customerId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * heartbeat 수신 - lease TTL 갱신
     * 고객 클라이언트는 주기적으로 (예: 20초마다) 호출해야 함
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<BaseResponse<HeartbeatResponse>> heartbeat(
            @Valid @RequestBody HeartbeatRequest request) {

        boolean renewed = queueLeaseService.renewLease(request.getQueueTicket());

        if (renewed) {
            long remainingTtl = queueLeaseService.getRemainingTtl(request.getQueueTicket());
            log.debug("Heartbeat 수신: ticket={}, 남은TTL={}초", request.getQueueTicket(), remainingTtl);
            return ResponseEntity.ok(BaseResponse.success(HeartbeatResponse.success(remainingTtl)));
        }

        log.warn("Heartbeat 실패: 유효하지 않은 ticket={}", request.getQueueTicket());
        return ResponseEntity.ok(BaseResponse.success(HeartbeatResponse.expired()));
    }

    /**
     * 대기열 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<QueueService.QueueSizes> getStats() {
        return ResponseEntity.ok(queueService.getQueueSizes());
    }
}