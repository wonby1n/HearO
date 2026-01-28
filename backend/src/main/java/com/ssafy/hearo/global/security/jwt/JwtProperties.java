package com.ssafy.hearo.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpiryMs,
        long refreshTokenExpiryMs
) {
    public JwtProperties {
        // Default values if not provided
        if (accessTokenExpiryMs <= 0) {
            accessTokenExpiryMs = 30 * 60 * 1000L; // 30 minutes
        }
        if (refreshTokenExpiryMs <= 0) {
            refreshTokenExpiryMs = 7 * 24 * 60 * 60 * 1000L; // 7 days
        }
    }
}
