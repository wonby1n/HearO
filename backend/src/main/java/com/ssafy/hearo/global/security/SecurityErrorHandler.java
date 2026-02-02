package com.ssafy.hearo.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.hearo.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 인증/인가 에러 응답 핸들러
 * GlobalExceptionHandler와 동일한 BaseResponse 포맷으로 응답
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 시 호출 (401 Unauthorized)
     * - 토큰 없음, 토큰 만료, 토큰 무효 등
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Authentication failed for {}: {}", request.getRequestURI(), authException.getMessage());
        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }

    /**
     * 인가 실패 시 호출 (403 Forbidden)
     * - 인증은 되었으나 권한이 없는 경우
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied for {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
        writeErrorResponse(response, HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status.value());

        BaseResponse<Void> errorResponse = BaseResponse.fail(message, status.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
