package com.ssafy.hearo.domain.user.service;

import com.ssafy.hearo.domain.matching.service.CounselorAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HeartbeatServiceImpl implements HeartbeatService {

    private static final String HEARTBEAT_KEY_PREFIX = "heartbeat:counselor:";
    private static final String HEARTBEAT_KEY_PATTERN = "heartbeat:counselor:*";
    private static final long HEARTBEAT_TTL_SECONDS = 30;
    private static final String HEARTBEAT_VALUE = "active";

    private final RedisTemplate<String, String> redisTemplate;
    private final CounselorAvailabilityService counselorAvailabilityService;

    public HeartbeatServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            @Lazy CounselorAvailabilityService counselorAvailabilityService
    ) {
        this.redisTemplate = redisTemplate;
        this.counselorAvailabilityService = counselorAvailabilityService;
    }

    @Override
    public void setHeartbeat(Long userId, boolean isActive) {
        String key = HEARTBEAT_KEY_PREFIX + userId;

        if (isActive) {
            // Set heartbeat with TTL
            redisTemplate.opsForValue().set(key, HEARTBEAT_VALUE, HEARTBEAT_TTL_SECONDS, TimeUnit.SECONDS);
            // 상담 가능 버튼 클릭 시 가용 상태로 전환
            counselorAvailabilityService.setAvailable(userId);
            log.debug("Heartbeat activated and counselor available: {}", userId);
        } else {
            // Remove heartbeat
            redisTemplate.delete(key);
            // 상담 불가능 상태로 전환
            counselorAvailabilityService.setUnavailable(userId);
            log.debug("Heartbeat deactivated and counselor unavailable: {}", userId);
        }
    }

    @Override
    public boolean isHeartbeatActive(Long userId) {
        String key = HEARTBEAT_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Set<Long> getActiveHeartbeatCounselorIds() {
        // Use KEYS command to find all heartbeat keys
        // Note: In production with large datasets, consider using SCAN instead
        Set<String> keys = redisTemplate.keys(HEARTBEAT_KEY_PATTERN);

        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }

        // Extract user IDs from keys
        return keys.stream()
                .map(key -> key.substring(HEARTBEAT_KEY_PREFIX.length()))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }
}
