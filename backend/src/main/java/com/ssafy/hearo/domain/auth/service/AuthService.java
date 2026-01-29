package com.ssafy.hearo.domain.auth.service;

import com.ssafy.hearo.domain.auth.dto.CustomerLoginRequest;
import com.ssafy.hearo.domain.auth.dto.CustomerLoginResponse;
import com.ssafy.hearo.domain.auth.dto.LoginRequest;
import com.ssafy.hearo.domain.auth.dto.LoginResponse;
import com.ssafy.hearo.domain.auth.dto.RefreshResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * Login with email and password (Counselor)
     * Returns access token in response body and sets refresh token in HttpOnly cookie
     */
    LoginResponse login(LoginRequest request, HttpServletResponse response);

    /**
     * Login with name and phone (Customer)
     * Returns access token in response body
     * Creates customer if not exists
     */
    CustomerLoginResponse customerLogin(CustomerLoginRequest request);

    /**
     * Refresh access token using refresh token from cookie
     */
    RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response);

    /**
     * Logout - invalidate tokens and clear cookie
     */
    void logout(HttpServletRequest request, HttpServletResponse response);
}
