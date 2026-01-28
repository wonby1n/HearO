package com.ssafy.hearo.domain.auth.controller;

import com.ssafy.hearo.domain.auth.dto.LoginRequest;
import com.ssafy.hearo.domain.auth.dto.LoginResponse;
import com.ssafy.hearo.domain.auth.dto.RefreshResponse;
import com.ssafy.hearo.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     * POST /api/v1/auth/login
     *
     * Request Body: { "email": "...", "password": "..." }
     * Response Body: { "accessToken": "...", "userId": ..., "username": "...", "userRole": "..." }
     * Cookie: refreshToken (HttpOnly, Secure, SameSite=None)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("Login request: email={}", request.email());
        LoginResponse loginResponse = authService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Refresh access token endpoint
     * POST /api/v1/auth/refresh
     *
     * Cookie: refreshToken (required)
     * Response Body: { "accessToken": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Refresh token request");
        RefreshResponse refreshResponse = authService.refresh(request, response);
        return ResponseEntity.ok(refreshResponse);
    }

    /**
     * Logout endpoint
     * POST /api/v1/auth/logout
     *
     * Cookie: refreshToken (optional - will be cleared)
     * Response: 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Logout request");
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}

