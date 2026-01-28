package com.ssafy.hearo.domain.queue.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 인터셉터
 * 현재: X-User-ID 헤더 사용 (mock)
 * 미래: JWT 토큰 검증으로 확장 가능
 */
@Component
@Slf4j
public class QueueHandshakeInterceptor implements HandshakeInterceptor {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_ID_ATTRIBUTE = "userId";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String userId = servletRequest.getServletRequest().getHeader(USER_ID_HEADER);

            // SockJS의 경우 쿼리 파라미터도 확인
            if (userId == null) {
                userId = servletRequest.getServletRequest().getParameter("userId");
            }

            if (userId != null && !userId.isBlank()) {
                attributes.put(USER_ID_ATTRIBUTE, userId);
                log.debug("WebSocket 핸드셰이크 성공: userId={}", userId);
                return true;
            }

            // TODO: JWT 구현 후 아래 코드 활성화
            // String token = extractJwtToken(servletRequest);
            // if (isValidToken(token)) {
            //     attributes.put(USER_ID_ATTRIBUTE, extractUserId(token));
            //     return true;
            // }

            log.warn("WebSocket 핸드셰이크: 사용자 식별 정보 없음");
        }

        // Mock 모드: 익명 연결 허용 (JWT 구현 후 false로 변경)
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }
}
