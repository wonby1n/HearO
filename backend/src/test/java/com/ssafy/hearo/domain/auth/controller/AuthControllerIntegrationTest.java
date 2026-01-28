package com.ssafy.hearo.domain.auth.controller;

import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.repository.UserRepository;
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
import org.springframework.test.web.servlet.MvcResult;
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
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @BeforeEach
    void setup() {
        // Clear Redis keys
        redisTemplate.delete(redisTemplate.keys("refresh-token:*"));
        redisTemplate.delete(redisTemplate.keys("blacklist:token:*"));
    }

    @AfterEach
    void cleanup() {
        transactionTemplate.execute(status -> {
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            return null;
        });
    }

    private User createTestUser(String email, String password, String name, UserRole role) {
        return transactionTemplate.execute(status -> {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .role(role)
                    .build();
            return userRepository.save(user);
        });
    }

    // ==================== Login Tests ====================

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/auth/login: 올바른 자격증명으로 로그인 성공")
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // given
        User user = createTestUser("test@example.com", "password123", "테스트유저", UserRole.USER);

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        // when/then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.username").value("테스트유저"))
                .andExpect(jsonPath("$.userRole").value("USER"))
                .andExpect(header().exists("Set-Cookie"))
                .andReturn();

        // Verify refresh token cookie
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("refreshToken");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/");
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/auth/login: 잘못된 비밀번호로 401 반환")
    void login_WithWrongPassword_ShouldReturn401() throws Exception {
        // given
        createTestUser("wrong@example.com", "correctPassword", "테스트유저", UserRole.USER);

        String requestBody = """
            {
                "email": "wrong@example.com",
                "password": "wrongPassword"
            }
            """;

        // when/then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/auth/login: 존재하지 않는 이메일로 401 반환")
    void login_WithNonExistentEmail_ShouldReturn401() throws Exception {
        // given
        String requestBody = """
            {
                "email": "nonexistent@example.com",
                "password": "anyPassword"
            }
            """;

        // when/then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/auth/login: 빈 이메일로 400 반환")
    void login_WithEmptyEmail_ShouldReturn400() throws Exception {
        // given
        String requestBody = """
            {
                "email": "",
                "password": "password123"
            }
            """;

        // when/then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ==================== Refresh Tests ====================

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/auth/refresh: 유효한 리프레시 토큰으로 새 액세스 토큰 발급")
    void refresh_WithValidRefreshToken_ShouldReturnNewAccessToken() throws Exception {
        // given - Login first to get refresh token cookie
        User user = createTestUser("refresh@example.com", "password123", "리프레시유저", UserRole.USER);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "refresh@example.com", "password": "password123"}
                            """))
                .andExpect(status().isOk())
                .andReturn();

        // Extract refresh token from Set-Cookie header
        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split(";")[0].split("=")[1];

        // when/then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/auth/refresh: 리프레시 토큰 없이 요청하면 401 반환")
    void refresh_WithoutRefreshToken_ShouldReturn401() throws Exception {
        // when/then
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Logout Tests ====================

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/auth/logout: 로그아웃 시 쿠키 삭제")
    void logout_ShouldClearCookie() throws Exception {
        // given - Login first
        createTestUser("logout@example.com", "password123", "로그아웃유저", UserRole.USER);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "logout@example.com", "password": "password123"}
                            """))
                .andExpect(status().isOk())
                .andReturn();

        // Extract refresh token from Set-Cookie header
        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split(";")[0].split("=")[1];

        // when
        MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                .andExpect(status().isNoContent())
                .andReturn();

        // then - Cookie should be cleared (maxAge=0)
        String logoutCookie = logoutResult.getResponse().getHeader("Set-Cookie");
        assertThat(logoutCookie).contains("refreshToken");
        assertThat(logoutCookie).contains("Max-Age=0");
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/auth/logout: 토큰 없이 로그아웃해도 성공")
    void logout_WithoutToken_ShouldSucceed() throws Exception {
        // when/then - Should succeed even without token
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }
}
