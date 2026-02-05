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

                log.warn("[하트비트] ⚠ 상담원 {} 하트비트 TTL 만료! (30초간 갱신 없음)", userId);

                // 1. 하트비트 상태 비활성화 (메모리/Redis 등 상태 동기화가 필요하다면)
                heartbeatService.setHeartbeat(userId, false);

                // 2. 사용자 상태를 REST로 변경하고 에너지 히스토리 저장
                log.info("[하트비트] 상담원 {} → REST 상태로 전환", userId);
                userStateService.switchToRestOnHeartbeatTimeout(userId);

            } catch (NumberFormatException e) {
                log.error("[하트비트] userId 파싱 실패: {}", expiredKey, e);
            } catch (Exception e) {
                log.error("[하트비트] 만료 처리 중 오류: {}", expiredKey, e);
            }
        }
    }
}
