package com.ssafy.hearo.domain.matching.controller;

import com.ssafy.hearo.domain.matching.service.CounselorAvailabilityService;
import com.ssafy.hearo.domain.queue.service.QueueService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CounselorControllerIntegrationTest {

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CounselorAvailabilityService availabilityService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @BeforeEach
    void setup() {
        clearRedis();
    }

    @AfterEach
    void cleanup() {
        clearRedis();
    }

    void clearRedis() {
        redisTemplate.delete("queue:normal");
        redisTemplate.delete("queue:blacklist");
        redisTemplate.delete("counselors:available");
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/counselor/{id}/available - 상담원을 가용 상태로 설정한다")
    void setAvailable_ShouldSetCounselorAvailable() throws Exception {
        // when
        mockMvc.perform(post("/api/v1/counselor/1/available"))
                .andExpect(status().isOk());

        // then
        org.assertj.core.api.Assertions.assertThat(availabilityService.isAvailable(1L)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/counselor/{id}/unavailable - 상담원을 비가용 상태로 설정한다")
    void setUnavailable_ShouldSetCounselorUnavailable() throws Exception {
        // given
        availabilityService.setAvailable(1L);

        // when
        mockMvc.perform(post("/api/v1/counselor/1/unavailable"))
                .andExpect(status().isOk());

        // then
        org.assertj.core.api.Assertions.assertThat(availabilityService.isAvailable(1L)).isFalse();
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/counselor/{id}/consultation-complete - 상담 종료 후 가용 상태로 전환한다")
    void consultationComplete_ShouldSetCounselorAvailable() throws Exception {
        // given - 상담 중 상태 (비가용)
        availabilityService.setUnavailable(1L);

        // when
        mockMvc.perform(post("/api/v1/counselor/1/consultation-complete"))
                .andExpect(status().isOk());

        // then
        org.assertj.core.api.Assertions.assertThat(availabilityService.isAvailable(1L)).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/counselor/available - 가용 상담원 목록을 반환한다")
    void getAvailableCounselors_ShouldReturnAvailableList() throws Exception {
        // given
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        // when/then
        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder(1, 2, 3)));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/counselor/available - 가용 상담원이 없으면 빈 배열을 반환한다")
    void getAvailableCounselors_WhenEmpty_ShouldReturnEmptyArray() throws Exception {
        // when/then
        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/counselor/system-status - 시스템 상태를 반환한다")
    void getSystemStatus_ShouldReturnSystemStatus() throws Exception {
        // given
        queueService.enqueue("customer-1");
        queueService.enqueue("customer-2");
        queueService.enqueue("customer-3");
        queueService.moveToBlacklistQueue("customer-3");

        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);

        // when/then
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.normalQueueSize", is(2)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(1)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(3)))
                .andExpect(jsonPath("$.availableCounselorCount", is(2)))
                .andExpect(jsonPath("$.availableCounselorIds", containsInAnyOrder(1, 2)));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/counselor/system-status - 모든 값이 0인 경우도 정상 동작한다")
    void getSystemStatus_WhenEmpty_ShouldReturnZeros() throws Exception {
        // when/then
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.normalQueueSize", is(0)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(0)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(0)))
                .andExpect(jsonPath("$.availableCounselorCount", is(0)))
                .andExpect(jsonPath("$.availableCounselorIds", hasSize(0)));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/counselor/{id}/available 후 system-status 변경 확인")
    void setAvailable_ThenCheckSystemStatus_ShouldReflectChange() throws Exception {
        // given
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(jsonPath("$.availableCounselorCount", is(0)));

        // when
        mockMvc.perform(post("/api/v1/counselor/5/available"))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(jsonPath("$.availableCounselorCount", is(1)))
                .andExpect(jsonPath("$.availableCounselorIds", contains(5)));
    }

    @Test
    @Order(9)
    @DisplayName("여러 상담원의 상태를 순차적으로 변경하는 시나리오")
    void multipleCounselorStateChanges_ShouldWorkCorrectly() throws Exception {
        // 1. 상담원 1, 2, 3 가용 상태로 설정
        mockMvc.perform(post("/api/v1/counselor/1/available")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/counselor/2/available")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/counselor/3/available")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(jsonPath("$", hasSize(3)));

        // 2. 상담원 2 상담 시작 (비가용)
        mockMvc.perform(post("/api/v1/counselor/2/unavailable")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", containsInAnyOrder(1, 3)));

        // 3. 상담원 2 상담 종료 (다시 가용)
        mockMvc.perform(post("/api/v1/counselor/2/consultation-complete")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder(1, 2, 3)));
    }

    @Test
    @Order(10)
    @DisplayName("대기열 크기가 시스템 상태에 정확히 반영된다")
    void queueSizeChanges_ShouldReflectInSystemStatus() throws Exception {
        // given - 고객 추가
        queueService.enqueue("c1");
        queueService.enqueue("c2");

        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(jsonPath("$.normalQueueSize", is(2)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(2)));

        // when - 한 명을 Blacklist Queue로 이동
        queueService.moveToBlacklistQueue("c1");

        // then
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(jsonPath("$.normalQueueSize", is(1)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(1)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(2)));

        // when - 한 명 제거
        queueService.remove("c2");

        // then
        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(jsonPath("$.normalQueueSize", is(0)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(1)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(1)));
    }
}
