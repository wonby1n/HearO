package com.ssafy.hearo.domain.matching.controller;

import com.ssafy.hearo.domain.matching.service.CounselorAvailabilityService;
import com.ssafy.hearo.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 상담원 관련 API
 * 상담원 가용성 관리 및 시스템 상태 조회
 */
@RestController
@RequestMapping("/api/v1/counselor")
@RequiredArgsConstructor
@Slf4j
public class CounselorController {

    private final CounselorAvailabilityService availabilityService;
    private final QueueService queueService;

    /**
     * 상담원 가용 상태 설정 (로그인/상담 대기 시작)
     * TODO: JWT 구현 후 SecurityContext에서 상담원 ID 추출
     */
    @PostMapping("/{counselorId}/available")
    public ResponseEntity<Void> setAvailable(@PathVariable Long counselorId) {
        availabilityService.setAvailable(counselorId);
        log.info("상담원 {} 가용 상태로 전환", counselorId);
        return ResponseEntity.ok().build();
    }

    /**
     * 상담원 비가용 상태 설정 (로그아웃/휴식)
     */
    @PostMapping("/{counselorId}/unavailable")
    public ResponseEntity<Void> setUnavailable(@PathVariable Long counselorId) {
        availabilityService.setUnavailable(counselorId);
        log.info("상담원 {} 비가용 상태로 전환", counselorId);
        return ResponseEntity.ok().build();
    }

    /**
     * 상담 종료 후 다시 가용 상태로 전환
     */
    @PostMapping("/{counselorId}/consultation-complete")
    public ResponseEntity<Void> consultationComplete(@PathVariable Long counselorId) {
        availabilityService.setAvailable(counselorId);
        log.info("상담원 {} 상담 종료, 가용 상태로 전환", counselorId);
        return ResponseEntity.ok().build();
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
