package com.ssafy.hearo.domain.queue.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class QueueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    }

    @BeforeEach
    void clearQueues() {
        redisTemplate.delete("queue:normal");
        redisTemplate.delete("queue:blacklist");
        redisTemplate.delete("counselors:available");
    }

    // ==================== 고객용 Queue API 테스트 ====================

    @Test
    @DisplayName("POST /api/v1/queue/register: 등록 요청이 대기열에 추가된다")
    void register_ShouldEnqueueCustomer() throws Exception {
        // given
        String requestBody = """
            {
                "symptom": "화면이 깜빡입니다",
                "errorCode": "E001",
                "modelCode": "TV-2024",
                "productCategory": "TV",
                "boughtAt": "2025-06-15T10:30:00"
            }
            """;

        // when/then
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "customer-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("customer-123"))
                .andExpect(jsonPath("$.waitingRank").value(1))
                .andExpect(jsonPath("$.queueType").value("NORMAL"))
                .andExpect(jsonPath("$.websocketTopic").value("/topic/queue-rank/customer-123"));
    }

    @Test
    @DisplayName("GET /api/v1/queue/status: 현재 대기 순위를 반환한다")
    void getStatus_ShouldReturnCurrentRank() throws Exception {
        // given - 먼저 등록
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "status-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"symptom":"test","productCategory":"TV","boughtAt":"2025-01-01T00:00:00"}
                            """))
                .andExpect(status().isOk());

        // when/then
        mockMvc.perform(get("/api/v1/queue/status")
                        .header("X-User-ID", "status-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("status-test"))
                .andExpect(jsonPath("$.waitingRank").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/queue/status: 대기열에 없으면 404 반환")
    void getStatus_NotInQueue_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/queue/status")
                        .header("X-User-ID", "not-registered"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/queue/cancel: 대기열에서 고객을 제거한다")
    void cancel_ShouldRemoveFromQueue() throws Exception {
        // given - 먼저 등록
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "cancel-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"symptom":"test","productCategory":"TV","boughtAt":"2025-01-01T00:00:00"}
                            """))
                .andExpect(status().isOk());

        // when
        mockMvc.perform(delete("/api/v1/queue/cancel")
                        .header("X-User-ID", "cancel-test"))
                .andExpect(status().isNoContent());

        // then - status 조회 시 404
        mockMvc.perform(get("/api/v1/queue/status")
                        .header("X-User-ID", "cancel-test"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/queue/stats: 대기열 통계를 반환한다")
    void getStats_ShouldReturnQueueSizes() throws Exception {
        // given - 2명 등록
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "stats-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"symptom":"test","productCategory":"TV","boughtAt":"2025-01-01T00:00:00"}
                            """));
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "stats-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"symptom":"test","productCategory":"TV","boughtAt":"2025-01-01T00:00:00"}
                            """));

        // when/then
        mockMvc.perform(get("/api/v1/queue/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalQueueSize").value(2))
                .andExpect(jsonPath("$.blacklistQueueSize").value(0));
    }

    // ==================== 상담원용 Counselor API 테스트 ====================

    @Test
    @DisplayName("POST /api/v1/counselor/{id}/available: 상담원을 가용 상태로 설정한다")
    void setAvailable_ShouldAddCounselorToAvailableSet() throws Exception {
        // when
        mockMvc.perform(post("/api/v1/counselor/1/available"))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/counselor/{id}/unavailable: 상담원을 비가용 상태로 설정한다")
    void setUnavailable_ShouldRemoveCounselorFromAvailableSet() throws Exception {
        // given - 먼저 가용 상태로 설정
        mockMvc.perform(post("/api/v1/counselor/1/available"));

        // when
        mockMvc.perform(post("/api/v1/counselor/1/unavailable"))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/counselor/{id}/consultation-complete: 상담 완료 후 가용 상태로 전환")
    void consultationComplete_ShouldSetCounselorAvailable() throws Exception {
        // when
        mockMvc.perform(post("/api/v1/counselor/5/consultation-complete"))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/counselor/system-status: 시스템 전체 상태를 반환한다")
    void getSystemStatus_ShouldReturnFullStatus() throws Exception {
        // given - 고객 2명 등록, 상담원 1명 가용
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "sys-customer-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"symptom":"test","productCategory":"TV","boughtAt":"2025-01-01T00:00:00"}
                            """));
        mockMvc.perform(post("/api/v1/queue/register")
                        .header("X-User-ID", "sys-customer-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"symptom":"test","productCategory":"TV","boughtAt":"2025-01-01T00:00:00"}
                            """));
        mockMvc.perform(post("/api/v1/counselor/10/available"));

        // when/then
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalQueueSize").value(2))
                .andExpect(jsonPath("$.blacklistQueueSize").value(0))
                .andExpect(jsonPath("$.totalWaitingCustomers").value(2))
                .andExpect(jsonPath("$.availableCounselorCount").value(1))
                .andExpect(jsonPath("$.availableCounselorIds[0]").value(10));
    }
}
