package com.ssafy.hearo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins(
                    "http://i14e106.p.ssafy.io", // 운영 서버
                    "http://localhost:5173",      // 프론트엔드 로컬 (Vite)
                    "http://localhost:3000"       // 프론트엔드 로컬 (기타)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 브라우저가 CORS 검사 결과를 캐싱할 시간
    }
}