package com.ssafy.hearo.global.security.jwt;

import com.ssafy.hearo.domain.user.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Ensure secret key is at least 256 bits for HS256
        String secret = jwtProperties.secret();
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access token
     */
    public String generateAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiryMs());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.refreshTokenExpiryMs());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Malformed JWT token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT token compact of handler are invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract all claims from token
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract role from token
     */
    public UserRole extractRole(String token) {
        Claims claims = extractClaims(token);
        return UserRole.valueOf(claims.get("role", String.class));
    }

    /**
     * Extract JTI (JWT ID) from token
     */
    public String extractJti(String token) {
        Claims claims = extractClaims(token);
        return claims.getId();
    }

    /**
     * Get remaining expiration time in milliseconds
     */
    public long getRemainingExpiration(String token) {
        Claims claims = extractClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = extractClaims(token);
        String type = claims.get("type", String.class);
        return "refresh".equals(type);
    }

    public long getAccessTokenExpiryMs() {
        return jwtProperties.accessTokenExpiryMs();
    }

    public long getRefreshTokenExpiryMs() {
        return jwtProperties.refreshTokenExpiryMs();
    }
}
