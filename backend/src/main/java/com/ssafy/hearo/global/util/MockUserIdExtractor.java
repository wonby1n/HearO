package com.ssafy.hearo.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * HTTP 요청에서 사용자 ID를 추출하는 유틸리티
 * JWT 인증 우선, X-User-ID 헤더 fallback (하위 호환성)
 */
@Component
public class MockUserIdExtractor {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String DEFAULT_USER_ID = "anonymous";

    /**
     * HTTP 요청에서 사용자 ID 추출
     * 우선순위: 1) JWT SecurityContext, 2) X-User-ID 헤더, 3) anonymous
     */
    public String extract(HttpServletRequest request) {
        // 1. Try SecurityContext first (JWT authenticated user)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            // SecurityContext의 principal name은 userId
            return auth.getName();
        }

        // 2. Fallback to X-User-ID header (backward compatibility)
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId != null && !userId.isBlank()) {
            return userId;
        }

        // 3. Default
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
