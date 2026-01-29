package com.ssafy.hearo.domain.queue.websocket;

import com.ssafy.hearo.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * STOMP 메시지 핸들러 - 대기열 관련 WebSocket 메시지 처리
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class QueueWebSocketHandler {

    private final QueueService queueService;

    /**
     * 클라이언트 순위 조회 요청 처리
     * 클라이언트 전송: /app/queue/rank
     * 응답: /user/queue/rank (요청한 사용자에게만 전송)
     */
    @MessageMapping("/queue/rank")
    @SendToUser("/queue/rank")
    public Map<String, Object> handleRankRequest(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs == null) {
            return Map.of(
                "success", false,
                "message", "세션 정보를 찾을 수 없습니다",
                "timestamp", System.currentTimeMillis()
            );
        }

        String customerId = (String) sessionAttrs.get("userId");
        if (customerId == null) {
            return Map.of(
                "success", false,
                "message", "사용자 정보를 찾을 수 없습니다",
                "timestamp", System.currentTimeMillis()
            );
        }

        return queueService.getWaitingRank(customerId)
            .map(rank -> {
                log.debug("순위 조회 요청: customerId={}, rank={}", customerId, rank);
                return Map.<String, Object>of(
                    "success", true,
                    "rank", rank,
                    "customerId", customerId,
                    "timestamp", System.currentTimeMillis()
                );
            })
            .orElseGet(() -> Map.of(
                "success", false,
                "message", "대기열에서 순위를 찾을 수 없습니다",
                "customerId", customerId,
                "timestamp", System.currentTimeMillis()
            ));
    }

    /**
     * 대기열 상태 브로드캐스트 요청
     * 클라이언트 전송: /app/queue/status
     * 응답: /topic/queue-updates
     */
    @MessageMapping("/queue/status")
    @SendTo("/topic/queue-updates")
    public Map<String, Object> handleStatusRequest() {
        QueueService.QueueSizes sizes = queueService.getQueueSizes();
        return Map.of(
            "normalQueueSize", sizes.normalQueueSize(),
            "blacklistQueueSize", sizes.blacklistQueueSize(),
            "totalWaiting", sizes.totalSize(),
            "timestamp", System.currentTimeMillis()
        );
    }
}
