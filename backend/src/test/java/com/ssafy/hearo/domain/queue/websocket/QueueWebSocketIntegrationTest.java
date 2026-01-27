package com.ssafy.hearo.domain.queue.websocket;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
}
