package com.ssafy.hearo.domain.matching.service;

import com.ssafy.hearo.domain.customer.entity.Blacklist;
import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.queue.service.QueueService;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.service.HeartbeatService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(MatchingSchedulerTest.TestConfig.class)
class MatchingSchedulerTest {

    // 기존 실행 중인 컨테이너 사용 (Testcontainers 대신)
    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        // 기존 Redis 컨테이너 (localhost:8379)
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "8379");
        registry.add("spring.data.redis.password", () -> "**");
        // 기존 PostgreSQL 컨테이너 (localhost:8432)
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:8432/hearo_test");
        registry.add("spring.datasource.username", () -> "hearo_user");
        registry.add("spring.datasource.password", () -> "**");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // 스케줄링 비활성화 (수동으로 호출할 것이므로)
        registry.add("spring.task.scheduling.pool.size", () -> "0");
    }

    @Autowired
    private MatchingScheduler matchingScheduler;

    @Autowired
    private CounselorAvailabilityService availabilityService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private HeartbeatService heartbeatService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TestMatchingEventListener eventListener;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        clearRedis();
        eventListener.clear();
    }

    @AfterEach
    void cleanup() {
        clearRedis();
        transactionTemplate.execute(status -> {
            // FK 제약조건 순서대로 삭제
            entityManager.createNativeQuery("DELETE FROM consultation_ratings").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM voice_recordings").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM consultations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM registrations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM blacklists").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM customers").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM products").executeUpdate();
            return null;
        });
    }

    void clearRedis() {
        redisTemplate.delete("queue:normal");
        redisTemplate.delete("queue:blacklist");
        redisTemplate.delete("counselors:available");
        // Clear heartbeat keys
        java.util.Set<String> heartbeatKeys = redisTemplate.keys("heartbeat:counselor:*");
        if (heartbeatKeys != null && !heartbeatKeys.isEmpty()) {
            redisTemplate.delete(heartbeatKeys);
        }
    }

    /**
     * 상담원을 매칭 가능 상태로 설정 (가용 + 하트비트 활성)
     */
    private void setCounselorMatchable(Long counselorId) {
        availabilityService.setAvailable(counselorId);
        heartbeatService.setHeartbeat(counselorId, true);
    }

    @Test
    @Order(1)
    @DisplayName("executeMatching: 가용 상담원이 없으면 매칭을 스킵한다")
    void executeMatching_NoAvailableCounselors_ShouldSkip() {
        // given
        queueService.enqueue("customer-1");
        // 가용 상담원 없음

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).isEmpty();
        assertThat(queueService.isInQueue("customer-1")).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("executeMatching: 대기 고객이 없으면 매칭을 스킵한다")
    void executeMatching_NoWaitingCustomers_ShouldSkip() {
        // given
        setCounselorMatchable(1L);
        // 대기 고객 없음

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("executeMatching: 정상 매칭 시 이벤트가 발행되고 상담원이 비가용으로 전환된다")
    void executeMatching_SuccessfulMatch_ShouldPublishEventAndSetUnavailable() {
        // given
        queueService.enqueue("customer-1");
        setCounselorMatchable(1L);

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).hasSize(1);
        var event = eventListener.getEvents().get(0);
        assertThat(event.customerId()).isEqualTo("customer-1");
        assertThat(event.counselorId()).isEqualTo(1L);
        assertThat(event.roomName()).startsWith("room-customer-1-1-");

        // 상담원이 비가용으로 전환됨
        assertThat(availabilityService.isAvailable(1L)).isFalse();

        // 고객이 대기열에서 제거됨
        assertThat(queueService.isInQueue("customer-1")).isFalse();
    }

    @Test
    @Order(4)
    @DisplayName("executeMatching: 여러 가용 상담원이 있으면 여러 고객을 매칭한다")
    void executeMatching_MultipleCounselors_ShouldMatchMultipleCustomers() throws InterruptedException {
        // given
        queueService.enqueue("customer-1");
        Thread.sleep(10);
        queueService.enqueue("customer-2");
        Thread.sleep(10);
        queueService.enqueue("customer-3");

        setCounselorMatchable(1L);
        setCounselorMatchable(2L);

        // when
        matchingScheduler.executeMatching();

        // then - 2명의 상담원이 있으므로 2명 매칭
        assertThat(eventListener.getEvents()).hasSize(2);
        assertThat(availabilityService.getAvailableCount()).isZero();

        // 3번째 고객은 여전히 대기 중
        assertThat(queueService.isInQueue("customer-3")).isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("executeMatching: Blacklist Queue가 Normal Queue보다 우선 처리된다")
    void executeMatching_BlacklistQueuePriority_ShouldProcessFirst() throws InterruptedException {
        // given
        queueService.enqueue("normal-customer");
        Thread.sleep(10);
        queueService.enqueue("blacklist-customer");
        queueService.moveToBlacklistQueue("blacklist-customer");

        setCounselorMatchable(1L);

        // when
        matchingScheduler.executeMatching();

        // then - Blacklist 고객이 먼저 매칭됨
        assertThat(eventListener.getEvents()).hasSize(1);
        assertThat(eventListener.getEvents().get(0).customerId()).isEqualTo("blacklist-customer");

        // Normal 고객은 여전히 대기 중
        assertThat(queueService.isInQueue("normal-customer")).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("executeMatching: 블랙리스트 관계로 매칭 불가능한 고객은 Blacklist Queue로 이동된다")
    void executeMatching_UnmatchableCustomer_ShouldMoveToBlacklistQueue() {
        // given
        TestData testData = transactionTemplate.execute(status -> {
            User counselor = createUser("counselor@test.com", "상담원");
            Customer customer = createCustomer("고객", "010-1111-1111");
            entityManager.flush();

            createBlacklist(counselor, customer, "테스트");
            entityManager.flush();

            return new TestData(counselor.getId(), customer.getId().longValue());
        });

        queueService.enqueue(testData.customerId.toString());
        setCounselorMatchable(testData.counselor1Id);

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).isEmpty();

        // 고객이 Blacklist Queue로 이동됨
        assertThat(queueService.getQueueType(testData.customerId.toString()))
                .contains(QueueService.QueueType.BLACKLIST);

        // 상담원은 여전히 가용 상태
        assertThat(availabilityService.isAvailable(testData.counselor1Id)).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("executeMatching: 일부 상담원과만 매칭 가능한 고객도 정상 매칭된다")
    void executeMatching_PartialMatch_ShouldSelectMatchableCounselor() {
        // given
        TestData testData = transactionTemplate.execute(status -> {
            User counselor1 = createUser("counselor1@test.com", "상담원1");
            User counselor2 = createUser("counselor2@test.com", "상담원2");
            Customer customer = createCustomer("고객", "010-1111-1111");
            entityManager.flush();

            // counselor1과만 블랙리스트
            createBlacklist(counselor1, customer, "테스트");
            entityManager.flush();

            return new TestData(counselor1.getId(), counselor2.getId(), customer.getId().longValue());
        });

        queueService.enqueue(testData.customerId.toString());
        setCounselorMatchable(testData.counselor1Id);
        setCounselorMatchable(testData.counselor2Id);

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).hasSize(1);
        var event = eventListener.getEvents().get(0);
        assertThat(event.customerId()).isEqualTo(testData.customerId.toString());
        assertThat(event.counselorId()).isEqualTo(testData.counselor2Id); // counselor2와 매칭

        // counselor2만 비가용
        assertThat(availabilityService.isAvailable(testData.counselor1Id)).isTrue();
        assertThat(availabilityService.isAvailable(testData.counselor2Id)).isFalse();
    }

    @Test
    @Order(8)
    @DisplayName("executeMatching: 매칭 시 생성되는 roomName이 올바른 형식을 가진다")
    void executeMatching_RoomName_ShouldHaveCorrectFormat() {
        // given
        queueService.enqueue("test-customer");
        setCounselorMatchable(42L);

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).hasSize(1);
        var event = eventListener.getEvents().get(0);

        // room-{customerId}-{counselorId}-{uuid8chars} 형식
        String roomName = event.roomName();
        assertThat(roomName).startsWith("room-test-customer-42-");
        assertThat(roomName.split("-")).hasSizeGreaterThanOrEqualTo(4);

        // UUID 부분이 8자
        String uuidPart = roomName.substring(roomName.lastIndexOf("-") + 1);
        assertThat(uuidPart).hasSize(8);
    }

    @Test
    @Order(9)
    @DisplayName("executeMatching: 연속 실행 시 이전 매칭 결과에 영향받지 않는다")
    void executeMatching_ConsecutiveExecutions_ShouldWorkIndependently() throws InterruptedException {
        // given - 첫 번째 매칭
        queueService.enqueue("customer-1");
        setCounselorMatchable(1L);

        matchingScheduler.executeMatching();
        assertThat(eventListener.getEvents()).hasSize(1);
        eventListener.clear();

        // 두 번째 매칭
        queueService.enqueue("customer-2");
        setCounselorMatchable(2L);

        // when
        matchingScheduler.executeMatching();

        // then
        assertThat(eventListener.getEvents()).hasSize(1);
        assertThat(eventListener.getEvents().get(0).customerId()).isEqualTo("customer-2");
        assertThat(eventListener.getEvents().get(0).counselorId()).isEqualTo(2L);
    }

    // ==================== 헬퍼 메서드 ====================

    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .password("password")
                .name(name)
                .role(UserRole.USER)
                .build();
        entityManager.persist(user);
        return user;
    }

    private Customer createCustomer(String name, String phone) {
        Customer customer = Customer.builder()
                .name(name)
                .phone(phone)
                .build();
        entityManager.persist(customer);
        return customer;
    }

    private Blacklist createBlacklist(User user, Customer customer, String reason) {
        Blacklist blacklist = Blacklist.builder()
                .user(user)
                .customer(customer)
                .reason(reason)
                .build();
        entityManager.persist(blacklist);
        return blacklist;
    }

    private record TestData(Long counselor1Id, Long counselor2Id, Long customerId) {
        TestData(Long counselorId, Long customerId) {
            this(counselorId, null, customerId);
        }
    }

    /**
     * 테스트 설정 - 이벤트 리스너 빈 등록
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestMatchingEventListener testMatchingEventListener() {
            return new TestMatchingEventListener();
        }
    }

    /**
     * 매칭 완료 이벤트를 수집하는 테스트용 리스너
     */
    static class TestMatchingEventListener {
        private final List<MatchingScheduler.MatchingCompletedEvent> events =
                Collections.synchronizedList(new ArrayList<>());

        @EventListener
        public void handleMatchingCompleted(MatchingScheduler.MatchingCompletedEvent event) {
            events.add(event);
        }

        public List<MatchingScheduler.MatchingCompletedEvent> getEvents() {
            return new ArrayList<>(events);
        }

        public void clear() {
            events.clear();
        }
    }
}
