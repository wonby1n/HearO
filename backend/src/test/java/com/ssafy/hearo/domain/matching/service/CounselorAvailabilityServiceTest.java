package com.ssafy.hearo.domain.matching.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CounselorAvailabilityServiceTest {

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
    private CounselorAvailabilityService availabilityService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final String AVAILABLE_COUNSELORS_KEY = "counselors:available";

    @BeforeEach
    void setup() {
        availabilityService.clearAll();
    }

    @AfterEach
    void cleanup() {
        availabilityService.clearAll();
    }

    @Test
    @Order(1)
    @DisplayName("setAvailable: 상담원을 가용 상태로 설정한다")
    void setAvailable_ShouldAddCounselorToAvailableSet() {
        // given
        Long counselorId = 1L;

        // when
        availabilityService.setAvailable(counselorId);

        // then
        assertThat(availabilityService.isAvailable(counselorId)).isTrue();
        assertThat(availabilityService.getAvailableCount()).isEqualTo(1);
    }

    @Test
    @Order(2)
    @DisplayName("setUnavailable: 상담원을 비가용 상태로 설정한다")
    void setUnavailable_ShouldRemoveCounselorFromAvailableSet() {
        // given
        Long counselorId = 1L;
        availabilityService.setAvailable(counselorId);

        // when
        availabilityService.setUnavailable(counselorId);

        // then
        assertThat(availabilityService.isAvailable(counselorId)).isFalse();
        assertThat(availabilityService.getAvailableCount()).isZero();
    }

    @Test
    @Order(3)
    @DisplayName("getAvailableCounselorIds: 모든 가용 상담원 ID를 반환한다")
    void getAvailableCounselorIds_ShouldReturnAllAvailableIds() {
        // given
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        // when
        Set<Long> availableIds = availabilityService.getAvailableCounselorIds();

        // then
        assertThat(availableIds).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @Order(4)
    @DisplayName("getAvailableCounselorIds: 가용 상담원이 없으면 빈 Set을 반환한다")
    void getAvailableCounselorIds_WhenEmpty_ShouldReturnEmptySet() {
        // given - no counselors available

        // when
        Set<Long> availableIds = availabilityService.getAvailableCounselorIds();

        // then
        assertThat(availableIds).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("isAvailable: 가용 상담원이면 true를 반환한다")
    void isAvailable_WhenCounselorIsAvailable_ShouldReturnTrue() {
        // given
        Long counselorId = 5L;
        availabilityService.setAvailable(counselorId);

        // when
        boolean available = availabilityService.isAvailable(counselorId);

        // then
        assertThat(available).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("isAvailable: 비가용 상담원이면 false를 반환한다")
    void isAvailable_WhenCounselorIsNotAvailable_ShouldReturnFalse() {
        // given
        Long counselorId = 5L;
        // 등록하지 않음

        // when
        boolean available = availabilityService.isAvailable(counselorId);

        // then
        assertThat(available).isFalse();
    }

    @Test
    @Order(7)
    @DisplayName("getAvailableCount: 가용 상담원 수를 정확히 반환한다")
    void getAvailableCount_ShouldReturnCorrectCount() {
        // given
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        // when
        long count = availabilityService.getAvailableCount();

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @Order(8)
    @DisplayName("clearAll: 모든 상담원 가용성을 초기화한다")
    void clearAll_ShouldRemoveAllCounselors() {
        // given
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        // when
        availabilityService.clearAll();

        // then
        assertThat(availabilityService.getAvailableCount()).isZero();
        assertThat(availabilityService.getAvailableCounselorIds()).isEmpty();
    }

    @Test
    @Order(9)
    @DisplayName("setAvailable: 이미 가용 상태인 상담원을 다시 등록해도 중복되지 않는다")
    void setAvailable_WhenAlreadyAvailable_ShouldNotDuplicate() {
        // given
        Long counselorId = 1L;
        availabilityService.setAvailable(counselorId);

        // when
        availabilityService.setAvailable(counselorId);
        availabilityService.setAvailable(counselorId);

        // then
        assertThat(availabilityService.getAvailableCount()).isEqualTo(1);
    }

    @Test
    @Order(10)
    @DisplayName("setUnavailable: 존재하지 않는 상담원을 비가용으로 설정해도 에러가 발생하지 않는다")
    void setUnavailable_WhenNotExists_ShouldNotThrowError() {
        // given
        Long counselorId = 999L;

        // when/then - 에러 없이 실행
        assertThatCode(() -> availabilityService.setUnavailable(counselorId))
                .doesNotThrowAnyException();
    }

    @Test
    @Order(11)
    @DisplayName("여러 상담원의 상태를 순차적으로 변경할 수 있다")
    void multipleOperations_ShouldWorkCorrectly() {
        // given
        availabilityService.setAvailable(1L);
        availabilityService.setAvailable(2L);
        availabilityService.setAvailable(3L);

        // when - 상담원 2만 비가용으로 전환
        availabilityService.setUnavailable(2L);

        // then
        assertThat(availabilityService.isAvailable(1L)).isTrue();
        assertThat(availabilityService.isAvailable(2L)).isFalse();
        assertThat(availabilityService.isAvailable(3L)).isTrue();
        assertThat(availabilityService.getAvailableCount()).isEqualTo(2);
    }
}
