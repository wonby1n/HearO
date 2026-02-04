package com.ssafy.hearo.domain.queue.service;

import com.ssafy.hearo.domain.customer.repository.BlacklistRepository;
import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueServiceImpl implements QueueService {

    private static final String NORMAL_QUEUE_KEY = "queue:normal";
    private static final String BLACKLIST_QUEUE_KEY = "queue:blacklist";
    private static final String TEMP_STACK_KEY = "queue:temp-stack"; // 임시 스택

    private final RedisTemplate<String, String> redisTemplate;
    private final QueueEventPublisher queueEventPublisher;
    private final BlacklistRepository blacklistRepository;
    private final QueueLeaseService queueLeaseService;

    @Override
    public QueueStatusResponse enqueue(String customerId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 이미 대기열에 있는지 확인
        Optional<QueueType> existingQueue = getQueueType(customerId);
        if (existingQueue.isPresent()) {
            Optional<Long> rank = getWaitingRank(customerId);
            return QueueStatusResponse.of(customerId, rank.orElse(0L), existingQueue.get().name());
        }

        // Normal Queue에 추가 (timestamp를 score로 사용)
        double score = System.currentTimeMillis();
        zSetOps.add(NORMAL_QUEUE_KEY, customerId, score);

        Long rank = getWaitingRank(customerId).orElse(1L);

        log.info("고객 {} Normal Queue에 등록, 순위: {}", customerId, rank);

        // 대기열 변경 알림
        QueueSizes sizes = getQueueSizes();
        queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());

        return QueueStatusResponse.of(customerId, rank, QueueType.NORMAL.name());
    }

    @Override
    public Optional<Long> getWaitingRank(String customerId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // Blacklist Queue 확인
        Long blacklistRank = zSetOps.rank(BLACKLIST_QUEUE_KEY, customerId);
        if (blacklistRank != null) {
            return Optional.of(blacklistRank + 1); // 0-indexed -> 1-indexed
        }

        // Normal Queue 확인
        Long normalRank = zSetOps.rank(NORMAL_QUEUE_KEY, customerId);
        if (normalRank != null) {
            Long blacklistSize = zSetOps.zCard(BLACKLIST_QUEUE_KEY);
            // Rank = Blacklist 크기 + Normal Queue 순위 + 1 (1-indexed)
            return Optional.of((blacklistSize != null ? blacklistSize : 0) + normalRank + 1);
        }

        return Optional.empty();
    }

    @Override
    public boolean moveToBlacklistQueue(String customerId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // Normal Queue에서 score 조회
        Double score = zSetOps.score(NORMAL_QUEUE_KEY, customerId);
        if (score == null) {
            log.warn("고객 {}이(가) Normal Queue에 없음", customerId);
            return false;
        }

        // 이동 전 순위 조회 (영향받는 고객 알림용)
        Optional<Long> rankBefore = getWaitingRank(customerId);

        // Blacklist Queue로 이동 (원래 timestamp 유지)
        zSetOps.remove(NORMAL_QUEUE_KEY, customerId);
        zSetOps.add(BLACKLIST_QUEUE_KEY, customerId, score);

        log.info("고객 {}을(를) Blacklist Queue로 이동", customerId);

        // 대기열 변경 알림
        QueueSizes sizes = getQueueSizes();
        queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());

        // 이동한 고객의 새 순위 전송
        Optional<Long> newRank = getWaitingRank(customerId);
        newRank.ifPresent(rank -> queueEventPublisher.sendRankUpdate(customerId, rank));

        // 이동 후 영향받는 고객들에게 순위 업데이트 전송
        // Blacklist로 이동 시 기존 위치 이후의 Normal Queue 고객들 순위가 당겨짐
        rankBefore.ifPresent(this::notifyAffectedCustomers);

        return true;
    }

    @Override
    public boolean remove(String customerId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 제거 전 순위 조회 (영향받는 고객 알림용)
        Optional<Long> rankBefore = getWaitingRank(customerId);

        Long removedFromNormal = zSetOps.remove(NORMAL_QUEUE_KEY, customerId);
        Long removedFromBlacklist = zSetOps.remove(BLACKLIST_QUEUE_KEY, customerId);

        boolean removed = (removedFromNormal != null && removedFromNormal > 0)
                       || (removedFromBlacklist != null && removedFromBlacklist > 0);

        if (removed) {
            log.info("고객 {}을(를) 대기열에서 제거", customerId);

            // 대기열 변경 알림
            QueueSizes sizes = getQueueSizes();
            queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());

            // 제거된 고객 위치 이후의 고객들에게 순위 업데이트 전송
            rankBefore.ifPresent(this::notifyAffectedCustomers);
        }

        return removed;
    }

    @Override
    public Optional<String> pop() {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // Blacklist Queue 우선 처리
        Set<String> blacklistFirst = zSetOps.range(BLACKLIST_QUEUE_KEY, 0, 0);
        if (blacklistFirst != null && !blacklistFirst.isEmpty()) {
            String customerId = blacklistFirst.iterator().next();
            zSetOps.remove(BLACKLIST_QUEUE_KEY, customerId);
            log.info("Blacklist Queue에서 고객 {} 추출", customerId);

            // 대기열 변경 알림
            QueueSizes sizes = getQueueSizes();
            queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());

            // 순위 1부터 모든 고객에게 업데이트 전송
            notifyAffectedCustomers(1);

            return Optional.of(customerId);
        }

        // Normal Queue 처리
        Set<String> normalFirst = zSetOps.range(NORMAL_QUEUE_KEY, 0, 0);
        if (normalFirst != null && !normalFirst.isEmpty()) {
            String customerId = normalFirst.iterator().next();
            zSetOps.remove(NORMAL_QUEUE_KEY, customerId);
            log.info("Normal Queue에서 고객 {} 추출", customerId);

            // 대기열 변경 알림
            QueueSizes sizes = getQueueSizes();
            queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());

            // 순위 1부터 모든 고객에게 업데이트 전송
            notifyAffectedCustomers(1);

            return Optional.of(customerId);
        }

        return Optional.empty();
    }

    @Override
    public QueueSizes getQueueSizes() {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Long normalSize = zSetOps.zCard(NORMAL_QUEUE_KEY);
        Long blacklistSize = zSetOps.zCard(BLACKLIST_QUEUE_KEY);
        return new QueueSizes(
            normalSize != null ? normalSize : 0,
            blacklistSize != null ? blacklistSize : 0
        );
    }

    @Override
    public boolean isInQueue(String customerId) {
        return getQueueType(customerId).isPresent();
    }

    @Override
    public Optional<QueueType> getQueueType(String customerId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        if (zSetOps.score(BLACKLIST_QUEUE_KEY, customerId) != null) {
            return Optional.of(QueueType.BLACKLIST);
        }
        if (zSetOps.score(NORMAL_QUEUE_KEY, customerId) != null) {
            return Optional.of(QueueType.NORMAL);
        }
        return Optional.empty();
    }

    @Override
    public PopResult popMatchable(Set<Long> availableCounselorIds) {
        if (availableCounselorIds == null || availableCounselorIds.isEmpty()) {
            log.warn("가용 상담원이 없습니다");
            return PopResult.empty();
        }

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        List<CustomerWithScore> tempStack = new ArrayList<>();
        int skippedCount = 0;
        int movedToBlacklistCount = 0;

        try {
            // 1. Blacklist Queue에서 매칭 가능한 고객 탐색
            PopResult blacklistResult = findMatchableFromQueue(
                    BLACKLIST_QUEUE_KEY, availableCounselorIds, tempStack, false);

            if (blacklistResult.hasMatch()) {
                skippedCount = tempStack.size();
                restoreTempStack(tempStack, BLACKLIST_QUEUE_KEY);
                publishQueueUpdate();
                // 순위 1부터 모든 고객에게 업데이트 전송
                notifyAffectedCustomers(1);
                return new PopResult(
                        blacklistResult.customerId(),
                        blacklistResult.matchableCounselorIds(),
                        skippedCount,
                        0
                );
            }

            // Blacklist Queue에서 못 찾음 - 스킵된 고객들 복원
            skippedCount = tempStack.size();
            restoreTempStack(tempStack, BLACKLIST_QUEUE_KEY);
            tempStack.clear();

            // 2. Normal Queue에서 매칭 가능한 고객 탐색
            PopResult normalResult = findMatchableFromQueue(
                    NORMAL_QUEUE_KEY, availableCounselorIds, tempStack, true);

            if (normalResult.hasMatch()) {
                // Normal Queue에서 스킵된 고객들은 Blacklist Queue로 이동
                movedToBlacklistCount = tempStack.size();
                moveTempStackToBlacklistQueue(tempStack);
                publishQueueUpdate();
                // 순위 1부터 모든 고객에게 업데이트 전송
                notifyAffectedCustomers(1);
                return new PopResult(
                        normalResult.customerId(),
                        normalResult.matchableCounselorIds(),
                        skippedCount,
                        movedToBlacklistCount
                );
            }

            // Normal Queue에서도 못 찾음 - Blacklist Queue로 이동
            movedToBlacklistCount = tempStack.size();
            moveTempStackToBlacklistQueue(tempStack);
            publishQueueUpdate();
            // Blacklist로 이동된 고객들도 순위 변경이 있으므로 전체 알림
            if (movedToBlacklistCount > 0) {
                notifyAffectedCustomers(1);
            }

            log.info("매칭 가능한 고객 없음. Blacklist 스킵: {}, Normal→Blacklist 이동: {}",
                    skippedCount, movedToBlacklistCount);

            return new PopResult(null, Set.of(), skippedCount, movedToBlacklistCount);

        } catch (Exception e) {
            // 오류 발생 시 임시 스택 복원
            log.error("popMatchable 중 오류 발생, 임시 스택 복원", e);
            restoreTempStack(tempStack, BLACKLIST_QUEUE_KEY);
            throw e;
        }
    }

    /**
     * 지정된 큐에서 매칭 가능한 고객을 찾아 추출
     */
    private PopResult findMatchableFromQueue(
            String queueKey,
            Set<Long> availableCounselorIds,
            List<CustomerWithScore> tempStack,
            boolean isNormalQueue) {

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        // 대기열 항목 만료 시간 (5분)
        final long QUEUE_ENTRY_TIMEOUT_MS = 5 * 60 * 1000;

        while (true) {
            // 큐의 첫 번째 고객 조회
            Set<ZSetOperations.TypedTuple<String>> firstSet = zSetOps.rangeWithScores(queueKey, 0, 0);
            if (firstSet == null || firstSet.isEmpty()) {
                break; // 큐가 비었음
            }

            ZSetOperations.TypedTuple<String> first = firstSet.iterator().next();
            String customerId = first.getValue();
            Double score = first.getScore();

            if (customerId == null || score == null) {
                break;
            }

            // 큐에서 제거
            zSetOps.remove(queueKey, customerId);

            // 1. lease 검증 - heartbeat가 끊긴 고객인지 확인
            if (!queueLeaseService.isLeaseAlive(customerId)) {
                log.warn("{}에서 유령 고객 {} 제거 (lease 만료)",
                        isNormalQueue ? "Normal Queue" : "Blacklist Queue",
                        customerId);
                // lease가 없으면 유령 고객 - 큐에서 제거하고 스킵
                continue;
            }

            // 2. 대기열 항목이 너무 오래되었는지 확인 (백업 필터링)
            long now = System.currentTimeMillis();
            long entryAge = now - score.longValue();
            if (entryAge > QUEUE_ENTRY_TIMEOUT_MS) {
                log.warn("{}에서 유령 고객 {} 제거 (대기 시간: {}초)",
                        isNormalQueue ? "Normal Queue" : "Blacklist Queue",
                        customerId, entryAge / 1000);
                // 오래된 항목 - lease도 삭제
                queueLeaseService.deleteLeaseByCustomerId(customerId);
                continue;
            }

            // 매칭 가능한 상담원 확인
            Set<Long> matchableCounselors = findMatchableCounselors(customerId, availableCounselorIds);

            if (!matchableCounselors.isEmpty()) {
                // 매칭 성공
                log.info("{}에서 고객 {} 매칭 성공. 가능한 상담원: {}",
                        isNormalQueue ? "Normal Queue" : "Blacklist Queue",
                        customerId, matchableCounselors);
                return new PopResult(customerId, matchableCounselors, 0, 0);
            }

            // 매칭 실패 - 임시 스택에 보관
            tempStack.add(new CustomerWithScore(customerId, score));
            log.debug("고객 {} 매칭 불가, 임시 보관 (가용 상담원 {}명 모두 블랙리스트)",
                    customerId, availableCounselorIds.size());
        }

        return PopResult.empty();
    }

    /**
     * 고객과 매칭 가능한 상담원 ID 목록 조회
     */
    private Set<Long> findMatchableCounselors(String customerId, Set<Long> availableCounselorIds) {
        try {
            Integer customerIdInt = Integer.parseInt(customerId);
            Set<Long> blockedCounselorIds = blacklistRepository.findBlockedCounselorIdsByCustomerId(customerIdInt);

            return availableCounselorIds.stream()
                    .filter(counselorId -> !blockedCounselorIds.contains(counselorId))
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            // customerId가 숫자가 아닌 경우 (mock 테스트 등) - 모든 상담원과 매칭 가능
            log.debug("customerId '{}'가 숫자가 아님, 블랙리스트 체크 스킵", customerId);
            return availableCounselorIds;
        }
    }

    /**
     * 임시 스택의 고객들을 원래 큐로 복원 (원래 순서 유지)
     */
    private void restoreTempStack(List<CustomerWithScore> tempStack, String queueKey) {
        if (tempStack.isEmpty()) return;

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        for (CustomerWithScore item : tempStack) {
            zSetOps.add(queueKey, item.customerId, item.score);
        }
        log.debug("임시 스택 {}명을 {}로 복원", tempStack.size(), queueKey);
    }

    /**
     * 임시 스택의 고객들을 Blacklist Queue로 이동
     */
    private void moveTempStackToBlacklistQueue(List<CustomerWithScore> tempStack) {
        if (tempStack.isEmpty()) return;

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        for (CustomerWithScore item : tempStack) {
            zSetOps.add(BLACKLIST_QUEUE_KEY, item.customerId, item.score);
        }
        log.info("Normal Queue에서 {} 명을 Blacklist Queue로 이동", tempStack.size());
    }

    private void publishQueueUpdate() {
        QueueSizes sizes = getQueueSizes();
        queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());
    }

    /**
     * 고객 ID와 score를 함께 저장하는 내부 클래스
     */
    private record CustomerWithScore(String customerId, double score) {}

    @Override
    public Map<String, Long> getCustomersFromRank(long fromRank) {
        Map<String, Long> result = new LinkedHashMap<>();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        Long blacklistSize = zSetOps.zCard(BLACKLIST_QUEUE_KEY);
        blacklistSize = blacklistSize != null ? blacklistSize : 0;

        // fromRank가 Blacklist 범위 내인 경우
        if (fromRank <= blacklistSize) {
            // Blacklist에서 fromRank-1 인덱스부터 끝까지 조회
            Set<String> blacklistCustomers = zSetOps.range(BLACKLIST_QUEUE_KEY, fromRank - 1, -1);
            if (blacklistCustomers != null) {
                long rank = fromRank;
                for (String customerId : blacklistCustomers) {
                    result.put(customerId, rank++);
                }
            }
            // Normal Queue 전체 조회
            Set<String> normalCustomers = zSetOps.range(NORMAL_QUEUE_KEY, 0, -1);
            if (normalCustomers != null) {
                long rank = blacklistSize + 1;
                for (String customerId : normalCustomers) {
                    result.put(customerId, rank++);
                }
            }
        } else {
            // fromRank가 Normal Queue 범위인 경우
            long normalStartIndex = fromRank - blacklistSize - 1;
            Set<String> normalCustomers = zSetOps.range(NORMAL_QUEUE_KEY, normalStartIndex, -1);
            if (normalCustomers != null) {
                long rank = fromRank;
                for (String customerId : normalCustomers) {
                    result.put(customerId, rank++);
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, Long> getAllCustomersWithRanks() {
        return getCustomersFromRank(1);
    }

    /**
     * 대기열 변경 후 영향받는 고객들에게 순위 업데이트 전송
     * @param affectedFromRank 이 순위부터 영향을 받음 (이전에 이 순위에 있던 고객부터)
     */
    private void notifyAffectedCustomers(long affectedFromRank) {
        if (affectedFromRank <= 0) {
            return;
        }

        Map<String, Long> affectedCustomers = getCustomersFromRank(affectedFromRank);
        if (!affectedCustomers.isEmpty()) {
            log.debug("순위 변경 알림 전송: {}명 (순위 {}부터)", affectedCustomers.size(), affectedFromRank);
            queueEventPublisher.sendBatchRankUpdates(affectedCustomers);
        }
    }
}
