package com.ssafy.hearo.domain.user.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeartbeatServiceTest {

    @Autowired
    private HeartbeatService heartbeatService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void setup() {
        // Clear all heartbeat keys
        Set<String> keys = redisTemplate.keys("heartbeat:counselor:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @Order(1)
    @DisplayName("setHeartbeat: true일 때 Redis에 TTL과 함께 저장")
    void setHeartbeat_WhenTrue_ShouldStoreWithTTL() {
        // given
        Long counselorId = 1L;

        // when
        heartbeatService.setHeartbeat(counselorId, true);

        // then
        String key = "heartbeat:counselor:" + counselorId;
        Boolean exists = redisTemplate.hasKey(key);
        assertThat(exists).isTrue();

        // Verify TTL is set (should be around 30 seconds)
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttl).isGreaterThan(0L);
        assertThat(ttl).isLessThanOrEqualTo(30L);
    }

    @Test
    @Order(2)
    @DisplayName("setHeartbeat: false일 때 Redis에서 삭제")
    void setHeartbeat_WhenFalse_ShouldRemoveFromRedis() {
        // given
        Long counselorId = 2L;
        String key = "heartbeat:counselor:" + counselorId;
        redisTemplate.opsForValue().set(key, "active");

        // when
        heartbeatService.setHeartbeat(counselorId, false);

        // then
        Boolean exists = redisTemplate.hasKey(key);
        assertThat(exists).isFalse();
    }

    @Test
    @Order(3)
    @DisplayName("isHeartbeatActive: 키 존재 시 true 반환")
    void isHeartbeatActive_WhenKeyExists_ShouldReturnTrue() {
        // given
        Long counselorId = 3L;
        heartbeatService.setHeartbeat(counselorId, true);

        // when
        boolean isActive = heartbeatService.isHeartbeatActive(counselorId);

        // then
        assertThat(isActive).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("isHeartbeatActive: 키 없으면 false 반환")
    void isHeartbeatActive_WhenKeyNotExists_ShouldReturnFalse() {
        // given
        Long counselorId = 999L; // Never set

        // when
        boolean isActive = heartbeatService.isHeartbeatActive(counselorId);

        // then
        assertThat(isActive).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("getActiveHeartbeatCounselorIds: 활성 하트비트 상담원 ID 목록 반환")
    void getActiveHeartbeatCounselorIds_ShouldReturnActiveIds() {
        // given
        heartbeatService.setHeartbeat(10L, true);
        heartbeatService.setHeartbeat(20L, true);
        heartbeatService.setHeartbeat(30L, true);

        // when
        Set<Long> activeIds = heartbeatService.getActiveHeartbeatCounselorIds();

        // then
        assertThat(activeIds).containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    @Test
    @Order(6)
    @DisplayName("getActiveHeartbeatCounselorIds: 활성 상담원 없으면 빈 Set 반환")
    void getActiveHeartbeatCounselorIds_WhenNoActive_ShouldReturnEmptySet() {
        // given - No heartbeats set

        // when
        Set<Long> activeIds = heartbeatService.getActiveHeartbeatCounselorIds();

        // then
        assertThat(activeIds).isEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("setHeartbeat: 연속 호출 시 TTL 갱신")
    void setHeartbeat_ConsecutiveCalls_ShouldRefreshTTL() throws InterruptedException {
        // given
        Long counselorId = 5L;
        heartbeatService.setHeartbeat(counselorId, true);

        // Wait a bit
        Thread.sleep(1000);

        String key = "heartbeat:counselor:" + counselorId;
        Long ttlBefore = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        // when - Refresh heartbeat
        heartbeatService.setHeartbeat(counselorId, true);

        // then - TTL should be refreshed back to ~30 seconds
        Long ttlAfter = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttlAfter).isGreaterThanOrEqualTo(ttlBefore);
    }
}
