package com.ssafy.hearo.domain.matching.controller;

import com.ssafy.hearo.domain.matching.service.CounselorAvailabilityService;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import com.ssafy.hearo.global.security.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private User testUser;

    @BeforeEach
    void setup() {
        clearRedis();
        // 테스트 유저 생성
        testUser = userRepository.findById(1L).orElseGet(() -> {
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .name("테스트상담원")
                    .role(UserRole.USER)
                    .build();
            return userRepository.save(user);
        });
    }

    @AfterEach
    void cleanup() {
        clearRedis();
    }

    void clearRedis() {
        redisTemplate.delete("queue:normal");
        redisTemplate.delete("queue:blacklist");
        redisTemplate.delete("counselors:available");
        // Clear heartbeat keys
        Set<String> heartbeatKeys = redisTemplate.keys("heartbeat:counselor:*");
        if (heartbeatKeys != null && !heartbeatKeys.isEmpty()) {
            redisTemplate.delete(heartbeatKeys);
        }
    }

    private CustomUserDetails createUserDetails(Long userId) {
        User user = User.builder()
                .email("counselor" + userId + "@example.com")
                .password("password")
                .name("상담원" + userId)
                .role(UserRole.USER)
                .build();
        // ID를 직접 설정하기 위해 reflection 사용하거나 저장된 유저 사용
        User savedUser = userRepository.findById(userId).orElseGet(() -> userRepository.save(user));
        return new CustomUserDetails(savedUser);
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/counselor/me/status - 가용 상태인 상담원의 상태를 조회한다")
    void getMyStatus_WhenAvailable_ShouldReturnAvailableStatus() throws Exception {
        // given
        availabilityService.setAvailable(testUser.getId());

        // when/then
        mockMvc.perform(get("/api/v1/counselor/me/status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.counselorId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.isAvailable", is(true)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/counselor/me/status - 비가용 상태인 상담원의 상태를 조회한다")
    void getMyStatus_WhenUnavailable_ShouldReturnUnavailableStatus() throws Exception {
        // given - 아무 설정 안 함 (기본 비가용)

        // when/then
        mockMvc.perform(get("/api/v1/counselor/me/status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.counselorId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.isAvailable", is(false)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/counselor/available - 가용 상담원 목록을 반환한다")
    void getAvailableCounselors_ShouldReturnAvailableList() throws Exception {
        // given - 서비스를 통해 직접 가용 상태 설정
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        // when/then
        mockMvc.perform(get("/api/v1/counselor/available")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder(1, 2, 3)));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/counselor/available - 가용 상담원이 없으면 빈 배열을 반환한다")
    void getAvailableCounselors_WhenEmpty_ShouldReturnEmptyArray() throws Exception {
        // when/then
        mockMvc.perform(get("/api/v1/counselor/available")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(5)
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
        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.normalQueueSize", is(2)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(1)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(3)))
                .andExpect(jsonPath("$.availableCounselorCount", is(2)))
                .andExpect(jsonPath("$.availableCounselorIds", containsInAnyOrder(1, 2)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/counselor/system-status - 모든 값이 0인 경우도 정상 동작한다")
    void getSystemStatus_WhenEmpty_ShouldReturnZeros() throws Exception {
        // when/then
        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.normalQueueSize", is(0)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(0)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(0)))
                .andExpect(jsonPath("$.availableCounselorCount", is(0)))
                .andExpect(jsonPath("$.availableCounselorIds", hasSize(0)));
    }

    @Test
    @Order(7)
    @DisplayName("상담원 가용성 변경 후 system-status에 반영 확인")
    void availabilityChange_ThenCheckSystemStatus_ShouldReflectChange() throws Exception {
        // given - 초기 상태 확인
        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$.availableCounselorCount", is(0)));

        // when - 서비스를 통해 가용 상태 설정
        availabilityService.setAvailable(5L);

        // then
        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$.availableCounselorCount", is(1)))
                .andExpect(jsonPath("$.availableCounselorIds", contains(5)));
    }

    @Test
    @Order(8)
    @DisplayName("여러 상담원의 상태를 순차적으로 변경하는 시나리오")
    void multipleCounselorStateChanges_ShouldWorkCorrectly() throws Exception {
        // 1. 상담원 1, 2, 3 가용 상태로 설정 (서비스 직접 호출)
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        mockMvc.perform(get("/api/v1/counselor/available")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$", hasSize(3)));

        // 2. 상담원 2 상담 시작 (비가용)
        availabilityService.setUnavailable(2L);

        mockMvc.perform(get("/api/v1/counselor/available")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", containsInAnyOrder(1, 3)));

        // 3. 상담원 2 상담 종료 (다시 가용)
        availabilityService.setAvailable(2L);

        mockMvc.perform(get("/api/v1/counselor/available")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder(1, 2, 3)));
    }

    @Test
    @Order(9)
    @DisplayName("대기열 크기가 시스템 상태에 정확히 반영된다")
    void queueSizeChanges_ShouldReflectInSystemStatus() throws Exception {
        // given - 고객 추가
        queueService.enqueue("c1");
        queueService.enqueue("c2");

        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$.normalQueueSize", is(2)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(2)));

        // when - 한 명을 Blacklist Queue로 이동
        queueService.moveToBlacklistQueue("c1");

        // then
        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$.normalQueueSize", is(1)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(1)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(2)));

        // when - 한 명 제거
        queueService.remove("c2");

        // then
        mockMvc.perform(get("/api/v1/counselor/system-status")
                        .with(user(new CustomUserDetails(testUser))))
                .andExpect(jsonPath("$.normalQueueSize", is(0)))
                .andExpect(jsonPath("$.blacklistQueueSize", is(1)))
                .andExpect(jsonPath("$.totalWaitingCustomers", is(1)));
    }

    @Test
    @Order(10)
    @DisplayName("인증 없이 상담원 엔드포인트 접근 시 401 반환")
    void counselorEndpoint_WithoutAuth_ShouldReturn401() throws Exception {
        // when/then
        mockMvc.perform(get("/api/v1/counselor/me/status"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/counselor/available"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/counselor/system-status"))
                .andExpect(status().isUnauthorized());
    }
}
