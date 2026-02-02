package com.ssafy.hearo.domain.queue.service;

import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;

import java.util.Optional;
import java.util.Set;

public interface QueueService {

    /**
     * 고객을 Normal Queue에 추가
     * @param customerId 고객 식별자
     * @return QueueStatusResponse 초기 대기 순위 포함
     */
    QueueStatusResponse enqueue(String customerId);

    /**
     * 현재 대기 순위 조회
     * Rank = (Blacklist Queue 크기) + (Normal Queue 순위) + 1
     * @param customerId 고객 식별자
     * @return Optional<Long> 순위 (대기열에 없으면 empty)
     */
    Optional<Long> getWaitingRank(String customerId);

    /**
     * Normal Queue에서 Blacklist Queue로 이동
     * @param customerId 고객 식별자
     * @return 이동 성공 여부
     */
    boolean moveToBlacklistQueue(String customerId);

    /**
     * 대기열에서 고객 제거
     * @param customerId 고객 식별자
     * @return 제거 성공 여부
     */
    boolean remove(String customerId);

    /**
     * 다음 고객 추출 (Blacklist Queue 우선, 그 다음 Normal Queue)
     * @return Optional<String> 고객 ID (대기열이 비어있으면 empty)
     */
    Optional<String> pop();

    /**
     * 매칭 가능한 다음 고객 추출 (블랙리스트 관계 고려)
     *
     * 로직:
     * 1. Blacklist Queue에서 가용 상담원과 매칭 가능한 고객 탐색
     *    - 매칭 불가능한 고객은 임시 스택에 보관
     * 2. Blacklist Queue에서 매칭 실패 시 Normal Queue 탐색
     *    - Normal Queue에서 매칭 불가능한 고객은 Blacklist Queue로 이동
     * 3. 매칭 성공 시 임시 스택의 고객들을 Blacklist Queue로 복원
     *
     * @param availableCounselorIds 현재 가용한 상담원 ID 목록
     * @return PopResult 매칭된 고객 ID와 매칭 가능한 상담원 ID 목록
     */
    PopResult popMatchable(Set<Long> availableCounselorIds);

    /**
     * 매칭 결과
     * @param customerId 매칭된 고객 ID (없으면 null)
     * @param matchableCounselorIds 해당 고객과 매칭 가능한 상담원 ID 목록
     * @param skippedCount Blacklist Queue에서 스킵된 고객 수
     * @param movedToBlacklistCount Normal Queue에서 Blacklist Queue로 이동된 고객 수
     */
    record PopResult(
            String customerId,
            Set<Long> matchableCounselorIds,
            int skippedCount,
            int movedToBlacklistCount
    ) {
        public boolean hasMatch() {
            return customerId != null;
        }

        public static PopResult empty() {
            return new PopResult(null, Set.of(), 0, 0);
        }
    }

    /**
     * 양 큐의 크기 조회
     * @return QueueSizes
     */
    QueueSizes getQueueSizes();

    /**
     * 고객이 대기열에 있는지 확인
     * @param customerId 고객 식별자
     * @return 대기 중 여부
     */
    boolean isInQueue(String customerId);

    /**
     * 고객이 어느 큐에 있는지 확인
     * @param customerId 고객 식별자
     * @return Optional<QueueType> (NORMAL 또는 BLACKLIST)
     */
    Optional<QueueType> getQueueType(String customerId);

    /**
     * 지정된 순위 이후의 모든 대기 고객 ID와 새 순위를 조회
     * 대기열 변경으로 순위가 바뀐 고객들에게 알림을 보내기 위해 사용
     * @param fromRank 이 순위부터 조회 (inclusive, 1-indexed)
     * @return customerId -> newRank 맵
     */
    java.util.Map<String, Long> getCustomersFromRank(long fromRank);

    /**
     * 전체 대기열의 모든 고객과 순위 조회
     * @return customerId -> rank 맵
     */
    java.util.Map<String, Long> getAllCustomersWithRanks();

    record QueueSizes(long normalQueueSize, long blacklistQueueSize) {
        public long totalSize() {
            return normalQueueSize + blacklistQueueSize;
        }
    }

    enum QueueType {
        NORMAL, BLACKLIST
    }
}
