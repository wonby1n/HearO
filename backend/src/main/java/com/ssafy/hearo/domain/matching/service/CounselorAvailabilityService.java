package com.ssafy.hearo.domain.matching.service;

import com.ssafy.hearo.domain.user.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 상담원 가용성 관리 서비스
 * Redis Set을 사용하여 현재 상담 가능한 상담원 목록을 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CounselorAvailabilityService {

    private static final String AVAILABLE_COUNSELORS_KEY = "counselors:available";

    private final RedisTemplate<String, String> redisTemplate;
    private final HeartbeatService heartbeatService;

    /**
     * 상담원을 가용 상태로 설정 (로그인/상담 종료 시)
     */
    public void setAvailable(Long counselorId) {
        redisTemplate.opsForSet().add(AVAILABLE_COUNSELORS_KEY, counselorId.toString());
        log.info("상담원 {} 가용 상태로 설정", counselorId);
    }

    /**
     * 상담원을 비가용 상태로 설정 (상담 시작/로그아웃 시)
     */
    public void setUnavailable(Long counselorId) {
        redisTemplate.opsForSet().remove(AVAILABLE_COUNSELORS_KEY, counselorId.toString());
        log.info("상담원 {} 비가용 상태로 설정", counselorId);
    }

    /**
     * 현재 가용한 모든 상담원 ID 조회
     */
    public Set<Long> getAvailableCounselorIds() {
        Set<String> members = redisTemplate.opsForSet().members(AVAILABLE_COUNSELORS_KEY);
        if (members == null || members.isEmpty()) {
            return Set.of();
        }
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    /**
     * 특정 상담원이 가용 상태인지 확인
     */
    public boolean isAvailable(Long counselorId) {
        Boolean isMember = redisTemplate.opsForSet().isMember(AVAILABLE_COUNSELORS_KEY, counselorId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 가용 상담원 수 조회
     */
    public long getAvailableCount() {
        Long size = redisTemplate.opsForSet().size(AVAILABLE_COUNSELORS_KEY);
        return size != null ? size : 0;
    }

    /**
     * 모든 상담원을 비가용 상태로 초기화 (시스템 재시작 시)
     */
    public void clearAll() {
        redisTemplate.delete(AVAILABLE_COUNSELORS_KEY);
        log.info("모든 상담원 가용성 초기화");
    }

    /**
     * 매칭 가능한 상담원 ID 조회
     * 가용 상태(available) AND 하트비트 활성(heartbeat active)인 상담원만 반환
     */
    public Set<Long> getMatchableCounselorIds() {
        Set<Long> availableCounselors = getAvailableCounselorIds();
        if (availableCounselors.isEmpty()) {
            return Set.of();
        }

        Set<Long> heartbeatActiveCounselors = heartbeatService.getActiveHeartbeatCounselorIds();
        if (heartbeatActiveCounselors.isEmpty()) {
            log.debug("하트비트 활성 상담원 없음");
            return Set.of();
        }

        // Intersection: available AND heartbeat active
        Set<Long> matchable = new HashSet<>(availableCounselors);
        matchable.retainAll(heartbeatActiveCounselors);

        log.debug("매칭 가능 상담원: {} (가용: {}, 하트비트: {})",
                matchable.size(), availableCounselors.size(), heartbeatActiveCounselors.size());

        return matchable;
    }
}
