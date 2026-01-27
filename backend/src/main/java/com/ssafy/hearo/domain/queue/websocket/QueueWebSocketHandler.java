package com.ssafy.hearo.domain.queue.websocket;

import com.ssafy.hearo.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
     */
    @MessageMapping("/queue/rank")
    public void handleRankRequest(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            String userId = (String) sessionAttrs.get("userId");
            if (userId != null) {
                queueService.getWaitingRank(userId).ifPresent(rank -> {
                    log.debug("순위 조회 요청: userId={}, rank={}", userId, rank);
                });
            }
        }
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
