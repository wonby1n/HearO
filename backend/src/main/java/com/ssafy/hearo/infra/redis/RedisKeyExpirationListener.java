package com.ssafy.hearo.infra.redis;

import com.ssafy.hearo.domain.user.service.HeartbeatService;
import com.ssafy.hearo.domain.user.service.UserStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final HeartbeatService heartbeatService;
    private final UserStateService userStateService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      HeartbeatService heartbeatService,
                                      UserStateService userStateService) {
        super(listenerContainer);
        this.heartbeatService = heartbeatService;
        this.userStateService = userStateService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        // 하트비트 키 만료 감지 (예: heartbeat:counselor:123)
        if (expiredKey.startsWith("heartbeat:counselor:")) {
            try {
                String userIdStr = expiredKey.substring("heartbeat:counselor:".length());
                Long userId = Long.parseLong(userIdStr);

                log.info("Heartbeat expired for user: {}", userId);

                // 1. 하트비트 상태 비활성화 (메모리/Redis 등 상태 동기화가 필요하다면)
                heartbeatService.setHeartbeat(userId, false);

                // 2. 사용자 상태를 REST로 변경하고 에너지 히스토리 저장
                userStateService.switchToRestOnHeartbeatTimeout(userId);

            } catch (NumberFormatException e) {
                log.error("Failed to parse userId from expired key: {}", expiredKey, e);
            } catch (Exception e) {
                log.error("Error handling heartbeat expiration for key: {}", expiredKey, e);
            }
        }
    }
}
