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
     * Generate access token for User (Counselor)
     */
    public String generateAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiryMs());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .claim("tokenType", "USER")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generate access token for Customer
     */
    public String generateCustomerAccessToken(Integer customerId, String name, String phone) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiryMs());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(customerId))
                .claim("name", name)
                .claim("phone", phone)
                .claim("tokenType", "CUSTOMER")
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

    /**
     * Check if token is a Customer token
     */
    public boolean isCustomerToken(String token) {
        Claims claims = extractClaims(token);
        String tokenType = claims.get("tokenType", String.class);
        return "CUSTOMER".equals(tokenType);
    }

    /**
     * Check if token is a User (Counselor) token
     */
    public boolean isUserToken(String token) {
        Claims claims = extractClaims(token);
        String tokenType = claims.get("tokenType", String.class);
        return "USER".equals(tokenType) || tokenType == null; // backward compatibility
    }

    /**
     * Extract Customer ID from token (for Customer tokens)
     */
    public Integer extractCustomerId(String token) {
        Claims claims = extractClaims(token);
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extract name from Customer token
     */
    public String extractName(String token) {
        Claims claims = extractClaims(token);
        return claims.get("name", String.class);
    }

    /**
     * Extract phone from Customer token
     */
    public String extractPhone(String token) {
        Claims claims = extractClaims(token);
        return claims.get("phone", String.class);
    }
}
