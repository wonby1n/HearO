package com.ssafy.hearo.domain.queue.websocket;

import com.ssafy.hearo.domain.queue.service.QueueService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class QueueWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

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

    private WebSocketStompClient stompClient;
    private BlockingQueue<Map<String, Object>> receivedMessages;
    private List<StompSession> activeSessions;

    @BeforeEach
    void setup() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        receivedMessages = new LinkedBlockingQueue<>();
        activeSessions = new ArrayList<>();

        // Redis 초기화
        clearRedis();
    }

    @AfterEach
    void teardown() {
        // 모든 활성 세션 종료
        for (StompSession session : activeSessions) {
            try {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception ignored) {
            }
        }

        if (stompClient != null) {
            stompClient.stop();
        }

        // Redis 정리
        clearRedis();
    }

    void clearRedis() {
        try {
            redisTemplate.delete("queue:normal");
            redisTemplate.delete("queue:blacklist");
            Set<String> heartbeatKeys = redisTemplate.keys("heartbeat:*");
            if (heartbeatKeys != null && !heartbeatKeys.isEmpty()) {
                redisTemplate.delete(heartbeatKeys);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("WebSocket 연결이 정상 동작한다")
    void webSocket_ShouldConnect() throws Exception {
        // given
        String wsUrl = "ws://localhost:" + port + "/ws/queue";
        CountDownLatch connectLatch = new CountDownLatch(1);

        // when
        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        connectLatch.countDown();
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command,
                                                StompHeaders headers, byte[] payload, Throwable exception) {
                        exception.printStackTrace();
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        exception.printStackTrace();
                    }
                }
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        // then
        boolean connected = connectLatch.await(5, TimeUnit.SECONDS);
        assertThat(connected).isTrue();
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    @DisplayName("토픽 구독이 정상 동작한다")
    void webSocket_ShouldSubscribeToTopic() throws Exception {
        // given
        String wsUrl = "ws://localhost:" + port + "/ws/queue";
        CountDownLatch subscribeLatch = new CountDownLatch(1);

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        // when
        StompSession.Subscription subscription = session.subscribe(
                "/topic/queue-updates",
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Map.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        subscribeLatch.countDown();
                    }
                }
        );

        // then
        assertThat(subscription).isNotNull();
        assertThat(subscription.getSubscriptionId()).isNotNull();
    }

    @Test
    @DisplayName("Queue 상태 요청 시 WebSocket 메시지를 수신한다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReceiveQueueUpdates() throws Exception {
        // given
        String wsUrl = "ws://localhost:" + port + "/ws/queue";
        CountDownLatch messageLatch = new CountDownLatch(1);

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        session.subscribe("/topic/queue-updates", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((Map<String, Object>) payload);
                messageLatch.countDown();
            }
        });

        // 구독 완료 대기
        Thread.sleep(1000);

        // when - 상태 요청
        session.send("/app/queue/status", "");

        // then
        boolean received = messageLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        Map<String, Object> message = receivedMessages.poll(5, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        assertThat(message).containsKeys("normalQueueSize", "blacklistQueueSize", "totalWaiting", "timestamp");
    }

    @Test
    @DisplayName("여러 클라이언트가 동시에 연결할 수 있다")
    void webSocket_ShouldSupportMultipleClients() throws Exception {
        // given
        String wsUrl = "ws://localhost:" + port + "/ws/queue";
        int clientCount = 3;
        CountDownLatch connectLatch = new CountDownLatch(clientCount);

        // when - 여러 클라이언트 연결
        for (int i = 0; i < clientCount; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<Transport> transports = new ArrayList<>();
                    transports.add(new WebSocketTransport(new StandardWebSocketClient()));
                    WebSocketStompClient client = new WebSocketStompClient(new SockJsClient(transports));
                    client.setMessageConverter(new MappingJackson2MessageConverter());

                    StompSession session = client.connectAsync(
                            wsUrl,
                            new WebSocketHttpHeaders(),
                            new StompSessionHandlerAdapter() {
                                @Override
                                public void afterConnected(StompSession session, StompHeaders headers) {
                                    connectLatch.countDown();
                                }
                            }
                    ).get(10, TimeUnit.SECONDS);

                    synchronized (activeSessions) {
                        activeSessions.add(session);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // then
        boolean allConnected = connectLatch.await(15, TimeUnit.SECONDS);
        assertThat(allConnected).isTrue();
    }

    @Test
    @DisplayName("고객이 자신의 대기 순위를 실시간으로 조회한다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReceiveMyRank() throws Exception {
        // given - 대기열에 고객 3명 등록
        String customerId1 = "customer-1";
        String customerId2 = "customer-2";
        String customerId3 = "customer-3";

        queueService.enqueue(customerId1);
        queueService.enqueue(customerId2);
        queueService.enqueue(customerId3);

        // userId 파라미터로 WebSocket 연결 (customer-2로 연결)
        String wsUrl = "ws://localhost:" + port + "/ws/queue?userId=" + customerId2;
        CountDownLatch messageLatch = new CountDownLatch(1);
        BlockingQueue<Map<String, Object>> rankMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        // /user/queue/rank 구독 (개인 메시지)
        session.subscribe("/user/queue/rank", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                rankMessages.add((Map<String, Object>) payload);
                messageLatch.countDown();
            }
        });

        // 구독 완료 대기
        Thread.sleep(1000);

        // when - 순위 조회 요청
        session.send("/app/queue/rank", "");

        // then
        boolean received = messageLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        Map<String, Object> response = rankMessages.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.get("success")).isEqualTo(true);
        assertThat(response.get("customerId")).isEqualTo(customerId2);
        assertThat(response.get("rank")).isEqualTo(2); // 2번째로 등록했으므로 순위 2
        assertThat(response).containsKey("timestamp");
    }

    @Test
    @DisplayName("대기열에 없는 고객이 순위 조회 시 실패 응답을 받는다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReceiveFailureWhenNotInQueue() throws Exception {
        // given - 대기열에 등록하지 않은 고객으로 연결
        String customerId = "not-in-queue-customer";
        String wsUrl = "ws://localhost:" + port + "/ws/queue?userId=" + customerId;
        CountDownLatch messageLatch = new CountDownLatch(1);
        BlockingQueue<Map<String, Object>> rankMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        session.subscribe("/user/queue/rank", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                rankMessages.add((Map<String, Object>) payload);
                messageLatch.countDown();
            }
        });

        Thread.sleep(1000);

        // when
        session.send("/app/queue/rank", "");

        // then
        boolean received = messageLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        Map<String, Object> response = rankMessages.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.get("success")).isEqualTo(false);
        assertThat(response.get("message")).isEqualTo("대기열에서 순위를 찾을 수 없습니다");
        assertThat(response.get("customerId")).isEqualTo(customerId);
    }

    @Test
    @DisplayName("대기열 변동 시 순위가 실시간으로 반영된다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReflectRankChanges() throws Exception {
        // given - 대기열에 고객 3명 등록
        String customerId1 = "customer-a";
        String customerId2 = "customer-b";
        String customerId3 = "customer-c";

        queueService.enqueue(customerId1);
        queueService.enqueue(customerId2);
        queueService.enqueue(customerId3);

        // customer-3으로 연결 (초기 순위: 3)
        String wsUrl = "ws://localhost:" + port + "/ws/queue?userId=" + customerId3;
        BlockingQueue<Map<String, Object>> rankMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        session.subscribe("/user/queue/rank", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                rankMessages.add((Map<String, Object>) payload);
            }
        });

        Thread.sleep(1000);

        // when - 첫 번째 순위 조회 (순위 3)
        CountDownLatch firstLatch = new CountDownLatch(1);
        session.send("/app/queue/rank", "");

        Map<String, Object> firstResponse = rankMessages.poll(10, TimeUnit.SECONDS);
        assertThat(firstResponse).isNotNull();
        assertThat(firstResponse.get("rank")).isEqualTo(3);

        // 앞의 고객 1명 제거
        queueService.remove(customerId1);

        // 다시 순위 조회 (순위 2로 변경)
        session.send("/app/queue/rank", "");

        Map<String, Object> secondResponse = rankMessages.poll(10, TimeUnit.SECONDS);
        assertThat(secondResponse).isNotNull();
        assertThat(secondResponse.get("success")).isEqualTo(true);
        assertThat(secondResponse.get("rank")).isEqualTo(2); // 순위가 3에서 2로 변경
    }

    @Test
    @DisplayName("userId 없이 연결 시 순위 조회가 실패한다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldFailWithoutUserId() throws Exception {
        // given - userId 없이 연결
        String wsUrl = "ws://localhost:" + port + "/ws/queue";
        CountDownLatch messageLatch = new CountDownLatch(1);
        BlockingQueue<Map<String, Object>> rankMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        session.subscribe("/user/queue/rank", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                rankMessages.add((Map<String, Object>) payload);
                messageLatch.countDown();
            }
        });

        Thread.sleep(1000);

        // when
        session.send("/app/queue/rank", "");

        // then
        boolean received = messageLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        Map<String, Object> response = rankMessages.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.get("success")).isEqualTo(false);
        assertThat(response.get("message")).isEqualTo("사용자 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("고객 제거 시 뒤 고객들이 순위 업데이트를 자동 수신한다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReceiveAutoRankUpdateWhenCustomerRemoved() throws Exception {
        // given - 대기열에 고객 3명 등록
        String customerId1 = "auto-test-1";
        String customerId2 = "auto-test-2";
        String customerId3 = "auto-test-3";

        queueService.enqueue(customerId1);
        queueService.enqueue(customerId2);
        queueService.enqueue(customerId3);

        // customer-3으로 /topic/queue-rank/{customerId} 구독
        String wsUrl = "ws://localhost:" + port + "/ws/queue?userId=" + customerId3;
        CountDownLatch autoUpdateLatch = new CountDownLatch(1);
        BlockingQueue<Map<String, Object>> autoRankMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        // 자동 푸시 토픽 구독: /topic/queue-rank/{customerId}
        session.subscribe("/topic/queue-rank/" + customerId3, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                autoRankMessages.add((Map<String, Object>) payload);
                autoUpdateLatch.countDown();
            }
        });

        Thread.sleep(1000);

        // when - 앞의 고객 1명 제거 (customer-3의 순위가 3→2로 변경)
        queueService.remove(customerId1);

        // then - 자동으로 순위 업데이트 수신
        boolean received = autoUpdateLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        Map<String, Object> autoUpdate = autoRankMessages.poll(5, TimeUnit.SECONDS);
        assertThat(autoUpdate).isNotNull();
        assertThat(autoUpdate.get("customerId")).isEqualTo(customerId3);
        assertThat(autoUpdate.get("rank")).isEqualTo(2); // 순위가 3에서 2로 변경
        assertThat(autoUpdate.get("status")).isEqualTo("WAITING");
        assertThat(autoUpdate).containsKey("timestamp");
    }

    @Test
    @DisplayName("pop 시 남은 고객들이 순위 업데이트를 자동 수신한다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReceiveAutoRankUpdateOnPop() throws Exception {
        // given - 대기열에 고객 3명 등록
        String customerId1 = "pop-test-1";
        String customerId2 = "pop-test-2";
        String customerId3 = "pop-test-3";

        queueService.enqueue(customerId1);
        queueService.enqueue(customerId2);
        queueService.enqueue(customerId3);

        // customer-2로 연결 (초기 순위 2)
        String wsUrl = "ws://localhost:" + port + "/ws/queue?userId=" + customerId2;
        CountDownLatch autoUpdateLatch = new CountDownLatch(1);
        BlockingQueue<Map<String, Object>> autoRankMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(
                wsUrl,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);

        activeSessions.add(session);

        // 자동 푸시 토픽 구독
        session.subscribe("/topic/queue-rank/" + customerId2, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                autoRankMessages.add((Map<String, Object>) payload);
                autoUpdateLatch.countDown();
            }
        });

        Thread.sleep(1000);

        // when - pop으로 맨 앞 고객 추출
        queueService.pop();

        // then - 자동으로 순위 업데이트 수신
        boolean received = autoUpdateLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        Map<String, Object> autoUpdate = autoRankMessages.poll(5, TimeUnit.SECONDS);
        assertThat(autoUpdate).isNotNull();
        assertThat(autoUpdate.get("customerId")).isEqualTo(customerId2);
        assertThat(autoUpdate.get("rank")).isEqualTo(1); // 순위가 2에서 1로 변경
        assertThat(autoUpdate.get("status")).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("여러 고객이 동시에 순위 업데이트를 자동 수신한다")
    @SuppressWarnings("unchecked")
    void webSocket_ShouldReceiveAutoRankUpdateForMultipleCustomers() throws Exception {
        // given - 대기열에 고객 4명 등록
        String customerId1 = "multi-test-1";
        String customerId2 = "multi-test-2";
        String customerId3 = "multi-test-3";
        String customerId4 = "multi-test-4";

        queueService.enqueue(customerId1);
        queueService.enqueue(customerId2);
        queueService.enqueue(customerId3);
        queueService.enqueue(customerId4);

        // customer-3, customer-4 둘 다 연결
        CountDownLatch customer3Latch = new CountDownLatch(1);
        CountDownLatch customer4Latch = new CountDownLatch(1);
        BlockingQueue<Map<String, Object>> customer3Messages = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String, Object>> customer4Messages = new LinkedBlockingQueue<>();

        // Customer 3 연결
        String wsUrl3 = "ws://localhost:" + port + "/ws/queue?userId=" + customerId3;
        StompSession session3 = stompClient.connectAsync(
                wsUrl3,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);
        activeSessions.add(session3);

        session3.subscribe("/topic/queue-rank/" + customerId3, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                customer3Messages.add((Map<String, Object>) payload);
                customer3Latch.countDown();
            }
        });

        // Customer 4 연결
        String wsUrl4 = "ws://localhost:" + port + "/ws/queue?userId=" + customerId4;
        List<Transport> transports4 = new ArrayList<>();
        transports4.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient client4 = new WebSocketStompClient(new SockJsClient(transports4));
        client4.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session4 = client4.connectAsync(
                wsUrl4,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        ).get(10, TimeUnit.SECONDS);
        activeSessions.add(session4);

        session4.subscribe("/topic/queue-rank/" + customerId4, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                customer4Messages.add((Map<String, Object>) payload);
                customer4Latch.countDown();
            }
        });

        Thread.sleep(1000);

        // when - 맨 앞 고객 제거
        queueService.remove(customerId1);

        // then - 두 고객 모두 순위 업데이트 수신
        boolean received3 = customer3Latch.await(10, TimeUnit.SECONDS);
        boolean received4 = customer4Latch.await(10, TimeUnit.SECONDS);

        assertThat(received3).isTrue();
        assertThat(received4).isTrue();

        Map<String, Object> update3 = customer3Messages.poll(5, TimeUnit.SECONDS);
        Map<String, Object> update4 = customer4Messages.poll(5, TimeUnit.SECONDS);

        assertThat(update3).isNotNull();
        assertThat(update3.get("rank")).isEqualTo(2); // 3에서 2로

        assertThat(update4).isNotNull();
        assertThat(update4.get("rank")).isEqualTo(3); // 4에서 3으로
    }
}
