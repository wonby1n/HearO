package com.ssafy.hearo.domain.user.controller;

import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import com.ssafy.hearo.global.security.jwt.JwtTokenProvider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

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

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setup() {
        // Clear Redis heartbeat keys
        redisTemplate.keys("heartbeat:counselor:*").forEach(redisTemplate::delete);

        // Create test user and get access token
        testUser = transactionTemplate.execute(status -> {
            User user = User.builder()
                    .email("heartbeat@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("하트비트테스트")
                    .role(UserRole.USER)
                    .build();
            return userRepository.save(user);
        });

        accessToken = jwtTokenProvider.generateAccessToken(
                testUser.getId(), testUser.getEmail(), testUser.getRole());
    }

    @AfterEach
    void cleanup() {
        transactionTemplate.execute(status -> {
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            return null;
        });
    }

    // ==================== Heartbeat Tests ====================

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/users/me/heartbeat: 하트비트 활성화 시 Redis에 저장")
    void setHeartbeat_WhenActive_ShouldStoreInRedis() throws Exception {
        // given
        String requestBody = """
            {
                "isHeartbeatActive": true
            }
            """;

        // when
        mockMvc.perform(post("/api/v1/users/me/heartbeat")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHeartbeatActive").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty());

        // then - Verify Redis key exists
        String heartbeatKey = "heartbeat:counselor:" + testUser.getId();
        Boolean exists = redisTemplate.hasKey(heartbeatKey);
        assertThat(exists).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/users/me/heartbeat: 하트비트 비활성화 시 Redis에서 삭제")
    void setHeartbeat_WhenInactive_ShouldRemoveFromRedis() throws Exception {
        // given - First activate heartbeat
        String heartbeatKey = "heartbeat:counselor:" + testUser.getId();
        redisTemplate.opsForValue().set(heartbeatKey, "active");

        String requestBody = """
            {
                "isHeartbeatActive": false
            }
            """;

        // when
        mockMvc.perform(post("/api/v1/users/me/heartbeat")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHeartbeatActive").value(false));

        // then - Verify Redis key removed
        Boolean exists = redisTemplate.hasKey(heartbeatKey);
        assertThat(exists).isFalse();
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/users/me/heartbeat: 인증 없이 요청 시 401 반환")
    void setHeartbeat_WithoutAuth_ShouldReturn401() throws Exception {
        // given
        String requestBody = """
            {
                "isHeartbeatActive": true
            }
            """;

        // when/then
        mockMvc.perform(post("/api/v1/users/me/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/users/me/heartbeat: 잘못된 토큰으로 요청 시 401 반환")
    void setHeartbeat_WithInvalidToken_ShouldReturn401() throws Exception {
        // given
        String requestBody = """
            {
                "isHeartbeatActive": true
            }
            """;

        // when/then
        mockMvc.perform(post("/api/v1/users/me/heartbeat")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/users/me/heartbeat: isHeartbeatActive 필드 누락 시 400 반환")
    void setHeartbeat_WithMissingField_ShouldReturn400() throws Exception {
        // given
        String requestBody = "{}";

        // when/then
        mockMvc.perform(post("/api/v1/users/me/heartbeat")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
