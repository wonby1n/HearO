package com.ssafy.hearo.domain.queue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 lease 관리 구현
 *
 * 저장 구조:
 * - queue:lease:{queueTicket} -> customerId (TTL 40초)
 * - queue:ticket:{customerId} -> queueTicket (역참조용)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueueLeaseServiceImpl implements QueueLeaseService {

    private static final String LEASE_KEY_PREFIX = "queue:lease:";
    private static final String TICKET_KEY_PREFIX = "queue:ticket:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String createLease(String customerId) {
        String queueTicket = UUID.randomUUID().toString();

        String leaseKey = LEASE_KEY_PREFIX + queueTicket;
        String ticketKey = TICKET_KEY_PREFIX + customerId;

        // 기존 lease가 있으면 삭제
        String existingTicket = redisTemplate.opsForValue().get(ticketKey);
        if (existingTicket != null) {
            redisTemplate.delete(LEASE_KEY_PREFIX + existingTicket);
            log.debug("기존 lease 삭제: customerId={}", customerId);
        }

        // 새 lease 생성
        redisTemplate.opsForValue().set(leaseKey, customerId, DEFAULT_LEASE_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(ticketKey, queueTicket, DEFAULT_LEASE_TTL_SECONDS, TimeUnit.SECONDS);

        log.info("[Lease] 생성: 고객 {} (ticket: {}, TTL: {}초)", customerId, queueTicket, DEFAULT_LEASE_TTL_SECONDS);
        return queueTicket;
    }

    @Override
    public boolean renewLease(String queueTicket) {
        if (queueTicket == null || queueTicket.isBlank()) {
            return false;
        }

        String leaseKey = LEASE_KEY_PREFIX + queueTicket;
        String customerId = redisTemplate.opsForValue().get(leaseKey);

        if (customerId == null) {
            log.warn("[Lease] 갱신 실패: 유효하지 않은 ticket {}", queueTicket);
            return false;
        }

        // TTL 갱신
        redisTemplate.expire(leaseKey, DEFAULT_LEASE_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.expire(TICKET_KEY_PREFIX + customerId, DEFAULT_LEASE_TTL_SECONDS, TimeUnit.SECONDS);

        log.debug("Lease 갱신: customerId={}, ticket={}", customerId, queueTicket);
        return true;
    }

    @Override
    public Optional<String> validateLease(String queueTicket) {
        if (queueTicket == null || queueTicket.isBlank()) {
            return Optional.empty();
        }

        String leaseKey = LEASE_KEY_PREFIX + queueTicket;
        String customerId = redisTemplate.opsForValue().get(leaseKey);
        return Optional.ofNullable(customerId);
    }

    @Override
    public boolean isLeaseAlive(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return false;
        }

        String ticketKey = TICKET_KEY_PREFIX + customerId;
        String queueTicket = redisTemplate.opsForValue().get(ticketKey);

        if (queueTicket == null) {
            return false;
        }

        // ticket이 있으면 lease도 확인
        String leaseKey = LEASE_KEY_PREFIX + queueTicket;
        return Boolean.TRUE.equals(redisTemplate.hasKey(leaseKey));
    }

    @Override
    public void deleteLease(String queueTicket) {
        if (queueTicket == null || queueTicket.isBlank()) {
            return;
        }

        String leaseKey = LEASE_KEY_PREFIX + queueTicket;
        String customerId = redisTemplate.opsForValue().get(leaseKey);

        redisTemplate.delete(leaseKey);
        if (customerId != null) {
            redisTemplate.delete(TICKET_KEY_PREFIX + customerId);
        }

        log.info("[Lease] 삭제: 고객 {} (ticket: {})", customerId, queueTicket);
    }

    @Override
    public void deleteLeaseByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return;
        }

        String ticketKey = TICKET_KEY_PREFIX + customerId;
        String queueTicket = redisTemplate.opsForValue().get(ticketKey);

        redisTemplate.delete(ticketKey);
        if (queueTicket != null) {
            redisTemplate.delete(LEASE_KEY_PREFIX + queueTicket);
        }

        log.info("[Lease] 삭제 (매칭 완료): 고객 {} (ticket: {})", customerId, queueTicket);
    }

    @Override
    public long getRemainingTtl(String queueTicket) {
        if (queueTicket == null || queueTicket.isBlank()) {
            return -1;
        }

        String leaseKey = LEASE_KEY_PREFIX + queueTicket;
        Long ttl = redisTemplate.getExpire(leaseKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
}
