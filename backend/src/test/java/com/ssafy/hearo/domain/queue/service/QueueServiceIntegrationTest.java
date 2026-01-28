package com.ssafy.hearo.domain.queue.service;

import com.ssafy.hearo.domain.customer.entity.Blacklist;
import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
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
class QueueServiceIntegrationTest {

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
        // 테스트 시 테이블 자동 생성
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // WebSocket 메시지 템플릿을 Mock으로 대체 (WebSocket 없이 테스트)
    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        clearQueues();
    }

    void clearQueues() {
        redisTemplate.delete("queue:normal");
        redisTemplate.delete("queue:blacklist");
    }

    @AfterEach
    void cleanup() {
        // Redis 정리
        clearQueues();
        // DB 정리 (테스트 데이터) - 외래 키 제약 조건 순서 준수: Blacklist -> Customer -> User
        transactionTemplate.execute(status -> {
            entityManager.createQuery("DELETE FROM Blacklist").executeUpdate();
            entityManager.createQuery("DELETE FROM Customer").executeUpdate();
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            return null;
        });
    }

    @Test
    @Order(1)
    @DisplayName("Enqueue: 고객을 Normal Queue에 추가하고 순위를 반환한다")
    void enqueue_ShouldAddToNormalQueueAndReturnRank() {
        // given
        String customerId = "customer-001";

        // when
        var response = queueService.enqueue(customerId);

        // then
        assertThat(response.getWaitingRank()).isEqualTo(1L);
        assertThat(response.getQueueType()).isEqualTo("NORMAL");
        assertThat(queueService.isInQueue(customerId)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Enqueue: 여러 고객을 순서대로 추가하면 FCFS 순위가 부여된다")
    void enqueue_MultipleCustomers_ShouldMaintainFCFSOrder() throws InterruptedException {
        // given
        String[] customers = {"c1", "c2", "c3"};

        // when
        for (String c : customers) {
            queueService.enqueue(c);
            Thread.sleep(10); // 다른 타임스탬프 보장
        }

        // then
        assertThat(queueService.getWaitingRank("c1")).contains(1L);
        assertThat(queueService.getWaitingRank("c2")).contains(2L);
        assertThat(queueService.getWaitingRank("c3")).contains(3L);
    }

    @Test
    @Order(3)
    @DisplayName("Rank 계산: Blacklist Queue 크기가 Normal Queue 순위에 더해진다")
    void getWaitingRank_ShouldIncludeBlacklistQueueOffset() throws InterruptedException {
        // given - Blacklist Queue에 2명 추가
        queueService.enqueue("b1");
        Thread.sleep(10);
        queueService.enqueue("b2");
        queueService.moveToBlacklistQueue("b1");
        queueService.moveToBlacklistQueue("b2");

        // when - Normal Queue에 고객 추가
        queueService.enqueue("n1");

        // then - 순위: blacklist_size + normal_rank + 1 = 2 + 0 + 1 = 3
        assertThat(queueService.getWaitingRank("n1")).contains(3L);

        // Blacklist 고객 순위는 1, 2
        assertThat(queueService.getWaitingRank("b1")).contains(1L);
        assertThat(queueService.getWaitingRank("b2")).contains(2L);
    }

    @Test
    @Order(4)
    @DisplayName("moveToBlacklistQueue: Normal에서 Blacklist로 이동한다")
    void moveToBlacklistQueue_ShouldTransferCorrectly() {
        // given
        queueService.enqueue("customer-move");

        // when
        boolean moved = queueService.moveToBlacklistQueue("customer-move");

        // then
        assertThat(moved).isTrue();
        assertThat(queueService.getQueueType("customer-move"))
                .contains(QueueService.QueueType.BLACKLIST);
    }

    @Test
    @Order(5)
    @DisplayName("pop: Blacklist Queue를 먼저 처리하고, 그 다음 Normal Queue를 처리한다")
    void pop_ShouldPrioritizeBlacklistQueue() throws InterruptedException {
        // given
        queueService.enqueue("normal-1");
        Thread.sleep(10);
        queueService.enqueue("blacklist-1");
        queueService.moveToBlacklistQueue("blacklist-1");

        // when/then - Blacklist 먼저
        assertThat(queueService.pop()).contains("blacklist-1");

        // when/then - 그 다음 Normal
        assertThat(queueService.pop()).contains("normal-1");

        // when/then - 비어있음
        assertThat(queueService.pop()).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("remove: 고객을 대기열에서 제거한다")
    void remove_ShouldRemoveFromQueue() {
        // given
        queueService.enqueue("to-remove");

        // when
        boolean removed = queueService.remove("to-remove");

        // then
        assertThat(removed).isTrue();
        assertThat(queueService.isInQueue("to-remove")).isFalse();
    }

    @Test
    @Order(7)
    @DisplayName("getQueueSizes: 양 큐의 크기를 정확히 반환한다")
    void getQueueSizes_ShouldReturnCorrectSizes() {
        // given
        queueService.enqueue("n1");
        queueService.enqueue("n2");
        queueService.enqueue("b1");
        queueService.moveToBlacklistQueue("b1");

        // when
        var sizes = queueService.getQueueSizes();

        // then
        assertThat(sizes.normalQueueSize()).isEqualTo(2);
        assertThat(sizes.blacklistQueueSize()).isEqualTo(1);
        assertThat(sizes.totalSize()).isEqualTo(3);
    }

    @Test
    @Order(8)
    @DisplayName("이미 대기열에 있는 고객이 다시 등록하면 기존 상태를 반환한다")
    void enqueue_AlreadyInQueue_ShouldReturnExistingStatus() {
        // given
        queueService.enqueue("duplicate-customer");

        // when
        var response = queueService.enqueue("duplicate-customer");

        // then
        assertThat(response.getWaitingRank()).isEqualTo(1L);
        assertThat(queueService.getQueueSizes().normalQueueSize()).isEqualTo(1);
    }

    // ==================== popMatchable 테스트 ====================

    @Test
    @Order(9)
    @DisplayName("popMatchable: 가용 상담원이 없으면 빈 결과 반환")
    void popMatchable_NoAvailableCounselors_ShouldReturnEmpty() {
        // given
        queueService.enqueue("customer-1");

        // when
        var result = queueService.popMatchable(Set.of());

        // then
        assertThat(result.hasMatch()).isFalse();
        // 고객은 여전히 대기열에 있어야 함
        assertThat(queueService.isInQueue("customer-1")).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("popMatchable: 블랙리스트 관계 없는 고객은 바로 매칭된다 (문자열 ID)")
    void popMatchable_NonNumericId_ShouldMatchWithAllCounselors() {
        // given - 문자열 ID는 블랙리스트 체크를 스킵
        queueService.enqueue("string-customer");
        Set<Long> availableCounselors = Set.of(1L, 2L, 3L);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isTrue();
        assertThat(result.customerId()).isEqualTo("string-customer");
        assertThat(result.matchableCounselorIds()).containsExactlyInAnyOrderElementsOf(availableCounselors);
        assertThat(result.skippedCount()).isZero();
        assertThat(result.movedToBlacklistCount()).isZero();
    }

    @Test
    @Order(11)
    @DisplayName("popMatchable: Blacklist Queue에서 스킵된 고객은 복원된다")
    void popMatchable_BlacklistQueueSkip_ShouldRestoreSkipped() {
        // given - 트랜잭션으로 데이터 생성 및 커밋
        TestData testData = transactionTemplate.execute(status -> {
            User counselor1 = createUser("counselor1@test.com", "상담원1");
            User counselor2 = createUser("counselor2@test.com", "상담원2");

            Customer customer1 = createCustomer("고객1", "010-1111-1111");
            Customer customer2 = createCustomer("고객2", "010-2222-2222");

            entityManager.flush();

            // customer1은 모든 상담원과 블랙리스트
            createBlacklist(counselor1, customer1, "테스트");
            createBlacklist(counselor2, customer1, "테스트");

            // customer2는 counselor1과만 블랙리스트 (counselor2와 매칭 가능)
            createBlacklist(counselor1, customer2, "테스트");

            entityManager.flush();

            return new TestData(counselor1.getId(), counselor2.getId(),
                    customer1.getId(), customer2.getId(), null);
        });

        // Redis에 대기열 등록
        queueService.enqueue(testData.customer1Id.toString());
        queueService.moveToBlacklistQueue(testData.customer1Id.toString());
        queueService.enqueue(testData.customer2Id.toString());
        queueService.moveToBlacklistQueue(testData.customer2Id.toString());

        Set<Long> availableCounselors = Set.of(testData.counselor1Id, testData.counselor2Id);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isTrue();
        assertThat(result.customerId()).isEqualTo(testData.customer2Id.toString());
        assertThat(result.matchableCounselorIds()).containsExactly(testData.counselor2Id);
        assertThat(result.skippedCount()).isEqualTo(1); // customer1이 스킵됨

        // customer1은 Blacklist Queue에 복원되어 있어야 함
        assertThat(queueService.isInQueue(testData.customer1Id.toString())).isTrue();
        assertThat(queueService.getQueueType(testData.customer1Id.toString()))
                .contains(QueueService.QueueType.BLACKLIST);
    }

    @Test
    @Order(12)
    @DisplayName("popMatchable: Normal Queue에서 매칭 불가 시 Blacklist Queue로 이동")
    void popMatchable_NormalQueueUnmatchable_ShouldMoveToBlacklist() {
        // given
        TestData testData = transactionTemplate.execute(status -> {
            User counselor1 = createUser("counselor3@test.com", "상담원3");

            Customer customer1 = createCustomer("고객4", "010-4444-4444");
            Customer customer2 = createCustomer("고객5", "010-5555-5555");

            entityManager.flush();

            // customer1은 유일한 상담원과 블랙리스트
            createBlacklist(counselor1, customer1, "테스트");

            entityManager.flush();

            return new TestData(counselor1.getId(), null,
                    customer1.getId(), customer2.getId(), null);
        });

        // Normal Queue에 customer1, customer2 순서로 추가
        queueService.enqueue(testData.customer1Id.toString());
        queueService.enqueue(testData.customer2Id.toString());

        Set<Long> availableCounselors = Set.of(testData.counselor1Id);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isTrue();
        assertThat(result.customerId()).isEqualTo(testData.customer2Id.toString());
        assertThat(result.movedToBlacklistCount()).isEqualTo(1); // customer1이 Blacklist로 이동

        // customer1은 이제 Blacklist Queue에 있어야 함
        assertThat(queueService.getQueueType(testData.customer1Id.toString()))
                .contains(QueueService.QueueType.BLACKLIST);
    }

    @Test
    @Order(13)
    @DisplayName("popMatchable: 모든 고객이 매칭 불가능하면 빈 결과 반환")
    void popMatchable_AllUnmatchable_ShouldReturnEmpty() {
        // given
        TestData testData = transactionTemplate.execute(status -> {
            User counselor1 = createUser("counselor4@test.com", "상담원4");

            Customer customer1 = createCustomer("고객6", "010-6666-6666");
            Customer customer2 = createCustomer("고객7", "010-7777-7777");

            entityManager.flush();

            // 모든 고객이 유일한 상담원과 블랙리스트
            createBlacklist(counselor1, customer1, "테스트");
            createBlacklist(counselor1, customer2, "테스트");

            entityManager.flush();

            return new TestData(counselor1.getId(), null,
                    customer1.getId(), customer2.getId(), null);
        });

        // Normal Queue에 추가
        queueService.enqueue(testData.customer1Id.toString());
        queueService.enqueue(testData.customer2Id.toString());

        Set<Long> availableCounselors = Set.of(testData.counselor1Id);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isFalse();
        assertThat(result.movedToBlacklistCount()).isEqualTo(2); // 둘 다 Blacklist로 이동

        // 둘 다 Blacklist Queue에 있어야 함
        assertThat(queueService.getQueueType(testData.customer1Id.toString()))
                .contains(QueueService.QueueType.BLACKLIST);
        assertThat(queueService.getQueueType(testData.customer2Id.toString()))
                .contains(QueueService.QueueType.BLACKLIST);
    }

    @Test
    @Order(14)
    @DisplayName("popMatchable: Blacklist Queue 전체 스킵 후 Normal Queue에서 매칭")
    void popMatchable_SkipAllBlacklistThenMatchNormal() throws InterruptedException {
        // given
        TestData testData = transactionTemplate.execute(status -> {
            User counselor1 = createUser("counselor5@test.com", "상담원5");

            Customer blacklistCustomer = createCustomer("블랙고객", "010-8888-8888");
            Customer normalCustomer = createCustomer("일반고객", "010-9999-9999");

            entityManager.flush();

            // blacklistCustomer만 상담원과 블랙리스트
            createBlacklist(counselor1, blacklistCustomer, "테스트");

            entityManager.flush();

            return new TestData(counselor1.getId(), null,
                    blacklistCustomer.getId(), normalCustomer.getId(), null);
        });

        // Blacklist Queue에 blacklistCustomer 추가
        queueService.enqueue(testData.customer1Id.toString());
        queueService.moveToBlacklistQueue(testData.customer1Id.toString());

        Thread.sleep(10);

        // Normal Queue에 normalCustomer 추가
        queueService.enqueue(testData.customer2Id.toString());

        Set<Long> availableCounselors = Set.of(testData.counselor1Id);

        // when
        var result = queueService.popMatchable(availableCounselors);

        // then
        assertThat(result.hasMatch()).isTrue();
        assertThat(result.customerId()).isEqualTo(testData.customer2Id.toString());
        assertThat(result.skippedCount()).isEqualTo(1); // Blacklist Queue에서 1명 스킵
        assertThat(result.movedToBlacklistCount()).isZero();

        // blacklistCustomer는 여전히 Blacklist Queue에 있어야 함
        assertThat(queueService.getQueueType(testData.customer1Id.toString()))
                .contains(QueueService.QueueType.BLACKLIST);
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

    // 테스트 데이터 홀더
    private record TestData(Long counselor1Id, Long counselor2Id,
                            Long customer1Id, Long customer2Id, Long customer3Id) {}
}
