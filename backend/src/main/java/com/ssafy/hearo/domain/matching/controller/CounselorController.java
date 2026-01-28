package com.ssafy.hearo.domain.matching.controller;

import com.ssafy.hearo.domain.matching.service.CounselorAvailabilityService;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 상담원 관련 API
 * 시스템 상태 조회 (가용성 전환은 heartbeat에서 처리)
 */
@RestController
@RequestMapping("/api/v1/counselor")
@RequiredArgsConstructor
@Slf4j
public class CounselorController {

    private final CounselorAvailabilityService availabilityService;
    private final QueueService queueService;

    /**
     * 현재 로그인한 상담원의 가용 상태 조회
     */
    @GetMapping("/me/status")
    public ResponseEntity<Map<String, Object>> getMyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long counselorId = userDetails.getId();
        boolean isAvailable = availabilityService.isAvailable(counselorId);

        return ResponseEntity.ok(Map.of(
                "counselorId", counselorId,
                "isAvailable", isAvailable
        ));
    }

    /**
     * 현재 가용 상담원 목록 조회
     */
    @GetMapping("/available")
    public ResponseEntity<Set<Long>> getAvailableCounselors() {
        return ResponseEntity.ok(availabilityService.getAvailableCounselorIds());
    }

    /**
     * 시스템 상태 조회 (대기열 + 상담원 현황)
     */
    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        QueueService.QueueSizes queueSizes = queueService.getQueueSizes();
        Set<Long> availableCounselors = availabilityService.getAvailableCounselorIds();

        return ResponseEntity.ok(Map.of(
                "normalQueueSize", queueSizes.normalQueueSize(),
                "blacklistQueueSize", queueSizes.blacklistQueueSize(),
                "totalWaitingCustomers", queueSizes.totalSize(),
                "availableCounselorCount", availableCounselors.size(),
                "availableCounselorIds", availableCounselors
        ));
    }
}
