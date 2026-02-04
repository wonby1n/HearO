package com.ssafy.hearo.domain.queue.service;

import java.util.Optional;

/**
 * 대기열 lease 관리 서비스
 * queueTicket 기반 heartbeat + TTL로 유령 회원을 제거
 */
public interface QueueLeaseService {

    /**
     * 새 lease 생성 (접수 성공 시 호출)
     * @param customerId 고객 ID
     * @return queueTicket (UUID 형태)
     */
    String createLease(String customerId);

    /**
     * heartbeat 수신 시 TTL 갱신
     * @param queueTicket 발급받은 티켓
     * @return 갱신 성공 여부 (티켓이 유효하지 않으면 false)
     */
    boolean renewLease(String queueTicket);

    /**
     * lease 유효성 검증
     * @param queueTicket 발급받은 티켓
     * @return 유효한 경우 customerId 반환, 아니면 empty
     */
    Optional<String> validateLease(String queueTicket);

    /**
     * customerId로 lease가 유효한지 확인
     * @param customerId 고객 ID
     * @return lease가 살아있으면 true
     */
    boolean isLeaseAlive(String customerId);

    /**
     * lease 삭제 (취소, 매칭 성공 시 호출)
     * @param queueTicket 발급받은 티켓
     */
    void deleteLease(String queueTicket);

    /**
     * customerId로 lease 삭제 (명시적 취소 시)
     * @param customerId 고객 ID
     */
    void deleteLeaseByCustomerId(String customerId);

    /**
     * 남은 TTL 조회
     * @param queueTicket 발급받은 티켓
     * @return 남은 초, 유효하지 않으면 -1
     */
    long getRemainingTtl(String queueTicket);

    /**
     * lease TTL 기본값 (초)
     */
    int DEFAULT_LEASE_TTL_SECONDS = 40;
}
