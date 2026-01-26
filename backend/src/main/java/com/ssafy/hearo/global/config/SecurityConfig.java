package com.ssafy.hearo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (API 서버이므로 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // ✨ 2. 폼 로그인 비활성화 (이게 /login 으로 튕기는 걸 막는 핵심!)
                .formLogin(AbstractHttpConfigurer::disable)

                // 3. HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 4. 세션 사용 안 함 (추후 JWT 토큰 쓸 거니까 STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. URL 권한 설정: 일단 모든 요청 다 허용! (나중에 로그인 기능 만들 때 잠그면 됨)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}