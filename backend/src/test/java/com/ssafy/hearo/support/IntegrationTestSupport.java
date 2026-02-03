package com.ssafy.hearo.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 통합 테스트 공통 설정
 * 기존 실행 중인 컨테이너 (PostgreSQL:8432, Redis:8379) 사용
 */
@SpringBootTest
public abstract class IntegrationTestSupport {

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        // 기존 Redis 컨테이너 (localhost:8379)
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "8379");
        registry.add("spring.data.redis.password", () -> "*");
        // 기존 PostgreSQL 컨테이너 (localhost:8432)
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:8432/hearo_test");
        registry.add("spring.datasource.username", () -> "hearo_user");
        registry.add("spring.datasource.password", () -> "*");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // 스케줄링 비활성화
        registry.add("spring.task.scheduling.pool.size", () -> "0");
    }
}
