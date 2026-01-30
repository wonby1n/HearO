package com.ssafy.hearo.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.hearo.global.common.response.BaseResponse;
import com.ssafy.hearo.global.security.jwt.JwtAuthenticationFilter;
import com.ssafy.hearo.global.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints (login, refresh, logout)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll()
                        // Queue endpoints (customers) - registration, status, cancel
                        .requestMatchers("/api/v1/queue/register").permitAll()
                        .requestMatchers("/api/v1/queue/status").permitAll()
                        .requestMatchers("/api/v1/queue/cancel").permitAll()
                        .requestMatchers("/api/v1/queue/stats").permitAll()
                        // Calls endpoint
                        .requestMatchers("/api/v1/calls/**").permitAll()
                        // Product endpoints (for customers - public access)
                        .requestMatchers("/api/v1/products/**").permitAll()
                        // Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // User endpoints require authentication
                        .requestMatchers("/api/v1/users/**").authenticated()
                        // Counselor endpoints require authentication
                        .requestMatchers("/api/v1/counselor/**").authenticated()
                        .requestMatchers("/images/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // Handle authentication/authorization errors
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            BaseResponse<Void> errorResponse = BaseResponse.fail(
                                    "인증이 필요합니다.", HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            BaseResponse<Void> errorResponse = BaseResponse.fail(
                                    "접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value());
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                )
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Development: Allow Vite / Electron dev server
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        // Enable credentials for cookie-based refresh token
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

