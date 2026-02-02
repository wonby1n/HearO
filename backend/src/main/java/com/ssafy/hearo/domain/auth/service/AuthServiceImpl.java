package com.ssafy.hearo.domain.auth.service;

import com.ssafy.hearo.domain.auth.dto.CustomerLoginRequest;
import com.ssafy.hearo.domain.auth.dto.CustomerLoginResponse;
import com.ssafy.hearo.domain.auth.dto.LoginRequest;
import com.ssafy.hearo.domain.auth.dto.LoginResponse;
import com.ssafy.hearo.domain.auth.dto.RefreshResponse;
import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import com.ssafy.hearo.domain.user.service.UserStateService;
import com.ssafy.hearo.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserStateService userStateService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        // Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + request.email()));

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        String redisKey = REFRESH_TOKEN_PREFIX + user.getId();
        long refreshTokenExpiryMs = jwtTokenProvider.getRefreshTokenExpiryMs();
        redisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpiryMs, TimeUnit.MILLISECONDS);

        // Set refresh token in HttpOnly cookie
        setRefreshTokenCookie(response, refreshToken, refreshTokenExpiryMs);

        log.info("User logged in: userId={}, email={}", user.getId(), user.getEmail());

        return LoginResponse.of(accessToken, user);
    }

    @Override
    @Transactional
    public CustomerLoginResponse customerLogin(CustomerLoginRequest request) {
        // Find or create customer
        Customer customer = customerRepository.findByNameAndPhone(request.name(), request.phone())
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .name(request.name())
                            .phone(request.phone())
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // Generate access token for customer
        String accessToken = jwtTokenProvider.generateCustomerAccessToken(
                customer.getId(),
                customer.getName(),
                customer.getPhone()
        );

        log.info("Customer logged in: customerId={}, name={}", customer.getId(), customer.getName());

        return CustomerLoginResponse.of(accessToken, customer);
    }

    @Override
    public RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new BadCredentialsException("리프레시 토큰이 없습니다");
        }

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다");
        }

        // Check if it's actually a refresh token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("리프레시 토큰이 아닙니다");
        }

        // Extract user ID and validate against Redis
        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BadCredentialsException("리프레시 토큰이 만료되었거나 유효하지 않습니다");
        }

        // Get user for new token
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        log.info("Access token refreshed: userId={}", userId);

        return RefreshResponse.of(accessToken);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            try {
                Long userId = jwtTokenProvider.extractUserId(refreshToken);
                String jti = jwtTokenProvider.extractJti(refreshToken);

                // (추가) 로그아웃 시 REST 전환 + 히스토리 저장
                userStateService.switchToRestOnLogout(userId);

                // Delete refresh token from Redis
                String refreshKey = REFRESH_TOKEN_PREFIX + userId;
                redisTemplate.delete(refreshKey);

                // Add refresh token to blacklist
                long remainingMs = jwtTokenProvider.getRemainingExpiration(refreshToken);
                if (remainingMs > 0) {
                    String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
                    redisTemplate.opsForValue().set(blacklistKey, "blacklisted", remainingMs, TimeUnit.MILLISECONDS);
                }

                log.info("User logged out: userId={}", userId);
            } catch (Exception e) {
                log.warn("Error during logout: {}", e.getMessage());
            }
        }

        // Clear refresh token cookie
        clearRefreshTokenCookie(response);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, long expiryMs) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true) // TODO: Set to false for local testing without HTTPS
                .sameSite("None") // Enable for Electron or cross-domain environments
                .path("/")
                .maxAge(Duration.ofMillis(expiryMs))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true) // TODO: Set to false for local testing without HTTPS
                .sameSite("None")
                .path("/")
                .maxAge(0) // Expire immediately
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
