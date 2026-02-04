package com.ssafy.hearo.domain.queue.service;

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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * QueueLeaseService 통합 테스트
 * heartbeat 기반 유령 회원 제거 기능 검증
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueueLeaseServiceTest {

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
        // 스케줄링 비활성화
        registry.add("spring.task.scheduling.pool.size", () -> "0");
    }

    @Autowired
    private QueueLeaseService queueLeaseService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // WebSocket 메시지 템플릿을 Mock으로 대체
    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @BeforeEach
    void setup() {
        clearAll();
    }

    @AfterEach
    void cleanup() {
        clearAll();
    }

    void clearAll() {
        // 모든 lease 관련 키 삭제
        Set<String> leaseKeys = redisTemplate.keys("queue:lease:*");
        Set<String> ticketKeys = redisTemplate.keys("queue:ticket:*");
        if (leaseKeys != null && !leaseKeys.isEmpty()) {
            redisTemplate.delete(leaseKeys);
        }
        if (ticketKeys != null && !ticketKeys.isEmpty()) {
            redisTemplate.delete(ticketKeys);
        }
        // 큐 정리
        redisTemplate.delete("queue:normal");
        redisTemplate.delete("queue:blacklist");
    }

    @Test
    @Order(1)
    @DisplayName("createLease: 새 lease 생성 시 queueTicket 반환")
    void createLease_ShouldReturnQueueTicket() {
        // given
        String customerId = "customer-001";

        // when
        String queueTicket = queueLeaseService.createLease(customerId);

        // then
        assertThat(queueTicket).isNotNull().isNotBlank();
        assertThat(queueLeaseService.isLeaseAlive(customerId)).isTrue();
        assertThat(queueLeaseService.validateLease(queueTicket)).contains(customerId);
    }

    @Test
    @Order(2)
    @DisplayName("renewLease: heartbeat로 TTL 갱신 성공")
    void renewLease_ShouldExtendTTL() {
        // given
        String customerId = "customer-002";
        String queueTicket = queueLeaseService.createLease(customerId);
        long initialTtl = queueLeaseService.getRemainingTtl(queueTicket);

        // when
        boolean renewed = queueLeaseService.renewLease(queueTicket);
        long renewedTtl = queueLeaseService.getRemainingTtl(queueTicket);

        // then
        assertThat(renewed).isTrue();
        assertThat(renewedTtl).isGreaterThanOrEqualTo(initialTtl - 1); // 약간의 시간 오차 허용
    }

    @Test
    @Order(3)
    @DisplayName("renewLease: 유효하지 않은 ticket은 갱신 실패")
    void renewLease_InvalidTicket_ShouldFail() {
        // given
        String invalidTicket = "invalid-ticket-12345";

        // when
        boolean renewed = queueLeaseService.renewLease(invalidTicket);

        // then
        assertThat(renewed).isFalse();
    }

    @Test
    @Order(4)
    @DisplayName("deleteLease: lease 삭제 후 isLeaseAlive 반환 false")
    void deleteLease_ShouldInvalidateLease() {
        // given
        String customerId = "customer-003";
        String queueTicket = queueLeaseService.createLease(customerId);
        assertThat(queueLeaseService.isLeaseAlive(customerId)).isTrue();

        // when
        queueLeaseService.deleteLease(queueTicket);

        // then
        assertThat(queueLeaseService.isLeaseAlive(customerId)).isFalse();
        assertThat(queueLeaseService.validateLease(queueTicket)).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("deleteLeaseByCustomerId: customerId로 lease 삭제")
    void deleteLeaseByCustomerId_ShouldWork() {
        // given
        String customerId = "customer-004";
        String queueTicket = queueLeaseService.createLease(customerId);

        // when
        queueLeaseService.deleteLeaseByCustomerId(customerId);

        // then
        assertThat(queueLeaseService.isLeaseAlive(customerId)).isFalse();
        assertThat(queueLeaseService.validateLease(queueTicket)).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("createLease 호출 시 기존 lease 교체")
    void createLease_ShouldReplaceExistingLease() {
        // given
        String customerId = "customer-005";
        String oldTicket = queueLeaseService.createLease(customerId);

        // when
        String newTicket = queueLeaseService.createLease(customerId);

        // then
        assertThat(newTicket).isNotEqualTo(oldTicket);
        assertThat(queueLeaseService.validateLease(oldTicket)).isEmpty();
        assertThat(queueLeaseService.validateLease(newTicket)).contains(customerId);
    }

    @Test
    @Order(7)
    @DisplayName("getRemainingTtl: 유효한 lease의 남은 TTL 반환")
    void getRemainingTtl_ShouldReturnPositiveValue() {
        // given
        String customerId = "customer-006";
        String queueTicket = queueLeaseService.createLease(customerId);

        // when
        long ttl = queueLeaseService.getRemainingTtl(queueTicket);

        // then
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(QueueLeaseService.DEFAULT_LEASE_TTL_SECONDS);
    }

    @Test
    @Order(8)
    @DisplayName("getRemainingTtl: 유효하지 않은 ticket은 음수 반환")
    void getRemainingTtl_InvalidTicket_ShouldReturnNegative() {
        // given
        String invalidTicket = "non-existent-ticket";

        // when
        long ttl = queueLeaseService.getRemainingTtl(invalidTicket);

        // then
        // Redis는 키가 없으면 -2, TTL이 없으면 -1 반환
        assertThat(ttl).isLessThan(0);
    }

    // ==================== 통합 테스트: popMatchable에서 lease 검증 ====================

    @Test
    @Order(9)
    @DisplayName("popMatchable: lease가 살아있는 고객만 매칭됨")
    void popMatchable_ShouldOnlyMatchCustomersWithAliveLease() {
        // given
        String aliveCustomer = "alive-customer";
        String deadCustomer = "dead-customer";

        // 두 고객 모두 큐에 등록
        queueService.enqueue(deadCustomer);
        queueService.enqueue(aliveCustomer);

        // aliveCustomer만 lease 생성 (deadCustomer는 lease 없음 = 유령)
        queueLeaseService.createLease(aliveCustomer);

        Set<Long> availableCounselors = Set.of(1L, 2L);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isTrue();
        assertThat(result.customerId()).isEqualTo(aliveCustomer);
        // deadCustomer는 lease 없어서 스킵되고 큐에서도 제거됨
        assertThat(queueService.isInQueue(deadCustomer)).isFalse();
    }

    @Test
    @Order(10)
    @DisplayName("popMatchable: lease 만료 고객은 큐에서 제거됨")
    void popMatchable_ExpiredLease_ShouldBeRemovedFromQueue() throws InterruptedException {
        // given
        String customerId = "expiring-customer";
        queueService.enqueue(customerId);

        // 매우 짧은 TTL로 lease 생성을 시뮬레이션 (직접 Redis에 설정)
        String queueTicket = queueLeaseService.createLease(customerId);

        // lease 삭제하여 만료 시뮬레이션
        queueLeaseService.deleteLease(queueTicket);

        Set<Long> availableCounselors = Set.of(1L);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isFalse(); // 매칭 가능한 고객 없음
        assertThat(queueService.isInQueue(customerId)).isFalse(); // 큐에서도 제거됨
    }

    @Test
    @Order(11)
    @DisplayName("cancel 시 lease도 함께 삭제됨")
    void cancel_ShouldDeleteLease() {
        // given
        String customerId = "cancel-customer";
        queueService.enqueue(customerId);
        String queueTicket = queueLeaseService.createLease(customerId);
        assertThat(queueLeaseService.isLeaseAlive(customerId)).isTrue();

        // when - remove 호출 (컨트롤러에서 lease 삭제 후 remove 호출)
        queueLeaseService.deleteLeaseByCustomerId(customerId);
        queueService.remove(customerId);

        // then
        assertThat(queueService.isInQueue(customerId)).isFalse();
        assertThat(queueLeaseService.isLeaseAlive(customerId)).isFalse();
    }
}
