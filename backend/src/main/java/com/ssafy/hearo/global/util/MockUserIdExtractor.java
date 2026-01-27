package com.ssafy.hearo.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * HTTP 요청에서 사용자 ID를 추출하는 유틸리티
 * 현재는 X-User-ID 헤더 사용 (mock)
 * JWT 구현 후 SecurityContext로 전환 예정
 */
@Component
public class MockUserIdExtractor {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String DEFAULT_USER_ID = "anonymous";

    /**
     * HTTP 요청에서 사용자 ID 추출
     * 우선순위: 1) X-User-ID 헤더, 2) JWT token (미래), 3) anonymous
     */
    public String extract(HttpServletRequest request) {
        // Mock 모드: 헤더 사용
        String userId = request.getHeader(USER_ID_HEADER);

        if (userId != null && !userId.isBlank()) {
            return userId;
        }

        // TODO: JWT 구현 후 아래 코드로 전환
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // if (auth != null && auth.isAuthenticated()) {
        //     return auth.getName();
        // }

        return DEFAULT_USER_ID;
    }

    /**
     * WebSocket 세션 속성에서 사용자 ID 추출
     */
    public String extractFromSession(Map<String, Object> sessionAttributes) {
        if (sessionAttributes != null) {
            Object userId = sessionAttributes.get("userId");
            if (userId instanceof String && !((String) userId).isBlank()) {
                return (String) userId;
            }
        }
        return DEFAULT_USER_ID;
    }
}
