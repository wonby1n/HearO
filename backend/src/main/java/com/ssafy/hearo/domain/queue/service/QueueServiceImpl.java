package com.ssafy.hearo.domain.queue.service;

import com.ssafy.hearo.domain.customer.repository.BlacklistRepository;
import com.ssafy.hearo.domain.queue.dto.QueueStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueServiceImpl implements QueueService {

    private static final String NORMAL_QUEUE_KEY = "queue:normal";
    private static final String BLACKLIST_QUEUE_KEY = "queue:blacklist";

    // 대기열 항목 만료 시간 (5분) — lease 검증의 백업 필터
    private static final long QUEUE_ENTRY_TIMEOUT_MS = 5 * 60 * 1000;

    private final RedisTemplate<String, String> redisTemplate;
    private final QueueEventPublisher queueEventPublisher;
    private final BlacklistRepository blacklistRepository;
    private final QueueLeaseService queueLeaseService;

    @Override
    public QueueStatusResponse enqueue(String customerId) {
        // 이미 대기열에 있는지 확인
        Optional<QueueType> existingQueue = getQueueType(customerId);
        if (existingQueue.isPresent()) {
            Optional<Long> rank = getWaitingRank(customerId);
            return QueueStatusResponse.of(customerId, rank.orElse(0L), existingQueue.get().name());
        }

        // Normal Queue에 추가 (timestamp를 score로 사용)
        redisTemplate.opsForZSet().add(NORMAL_QUEUE_KEY, customerId, System.currentTimeMillis());

        Long rank = getWaitingRank(customerId).orElse(1L);
        log.info("[대기열] 고객 {} Normal Queue에 등록 (순위: {}위)", customerId, rank);

        publishQueueUpdate();

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

        // Normal Queue 확인: Rank = Blacklist 크기 + Normal Queue 순위 + 1
        Long normalRank = zSetOps.rank(NORMAL_QUEUE_KEY, customerId);
        if (normalRank != null) {
            return Optional.of(size(BLACKLIST_QUEUE_KEY) + normalRank + 1);
        }

        return Optional.empty();
    }

    @Override
    public boolean moveToBlacklistQueue(String customerId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

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

        publishQueueUpdate();

        // 이동한 고객의 새 순위 전송
        getWaitingRank(customerId).ifPresent(rank -> queueEventPublisher.sendRankUpdate(customerId, rank));

        // 기존 위치 이후의 Normal Queue 고객들 순위가 당겨짐
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
            publishQueueUpdate();
            rankBefore.ifPresent(this::notifyAffectedCustomers);
        }

        return removed;
    }

    @Override
    public Optional<String> pop() {
        // Blacklist Queue 우선 처리
        Optional<String> customerId = popFirst(BLACKLIST_QUEUE_KEY)
                .or(() -> popFirst(NORMAL_QUEUE_KEY))
                .map(TypedTuple::getValue);

        customerId.ifPresent(id -> {
            publishQueueUpdate();
            notifyAffectedCustomers(1);
        });

        return customerId;
    }

    @Override
    public QueueSizes getQueueSizes() {
        return new QueueSizes(size(NORMAL_QUEUE_KEY), size(BLACKLIST_QUEUE_KEY));
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

        List<CustomerWithScore> tempStack = new ArrayList<>();
        // 오류 시 임시 스택을 원래 큐로 되돌리기 위해 현재 탐색 중인 큐를 추적
        String scanningQueueKey = BLACKLIST_QUEUE_KEY;

        try {
            // 1. Blacklist Queue에서 매칭 가능한 고객 탐색 (스킵된 고객은 원래 자리로 복원)
            PopResult blacklistResult = findMatchableFromQueue(BLACKLIST_QUEUE_KEY, availableCounselorIds, tempStack);
            int skippedCount = tempStack.size();
            restoreTempStack(tempStack, BLACKLIST_QUEUE_KEY);
            tempStack.clear();

            if (blacklistResult.hasMatch()) {
                publishQueueUpdate();
                notifyAffectedCustomers(1);
                return new PopResult(blacklistResult.customerId(), blacklistResult.matchableCounselorIds(), skippedCount, 0);
            }

            // 2. Normal Queue에서 매칭 가능한 고객 탐색 (스킵된 고객은 Blacklist Queue로 이동)
            scanningQueueKey = NORMAL_QUEUE_KEY;
            PopResult normalResult = findMatchableFromQueue(NORMAL_QUEUE_KEY, availableCounselorIds, tempStack);
            int movedToBlacklistCount = tempStack.size();
            moveTempStackToBlacklistQueue(tempStack);
            tempStack.clear();
            publishQueueUpdate();

            if (normalResult.hasMatch()) {
                notifyAffectedCustomers(1);
                return new PopResult(normalResult.customerId(), normalResult.matchableCounselorIds(), skippedCount, movedToBlacklistCount);
            }

            // Blacklist로 이동된 고객들도 순위 변경이 있으므로 전체 알림
            if (movedToBlacklistCount > 0) {
                notifyAffectedCustomers(1);
            }

            log.info("매칭 가능한 고객 없음. Blacklist 스킵: {}, Normal→Blacklist 이동: {}",
                    skippedCount, movedToBlacklistCount);

            return new PopResult(null, Set.of(), skippedCount, movedToBlacklistCount);

        } catch (Exception e) {
            log.error("popMatchable 중 오류 발생, 임시 스택을 {}로 복원", scanningQueueKey, e);
            restoreTempStack(tempStack, scanningQueueKey);
            throw e;
        }
    }

    /**
     * 지정된 큐에서 매칭 가능한 고객을 찾아 추출.
     * 매칭 불가 고객은 tempStack에 보관하고, 유령 고객(lease 만료/대기시간 초과)은 버린다.
     */
    private PopResult findMatchableFromQueue(String queueKey, Set<Long> availableCounselorIds, List<CustomerWithScore> tempStack) {
        String queueName = queueName(queueKey);

        Optional<TypedTuple<String>> next;
        while ((next = popFirst(queueKey)).isPresent()) {
            String customerId = next.get().getValue();
            double score = next.get().getScore();

            // 1. lease 검증 - heartbeat가 끊긴 고객인지 확인
            if (!queueLeaseService.isLeaseAlive(customerId)) {
                log.warn("[대기열] 유령고객 제거: {} ({}에서, lease 만료)", customerId, queueName);
                continue;
            }

            // 2. 대기열 항목이 너무 오래되었는지 확인 (백업 필터링)
            long entryAge = System.currentTimeMillis() - (long) score;
            if (entryAge > QUEUE_ENTRY_TIMEOUT_MS) {
                log.warn("[대기열] 유령고객 제거: {} ({}에서, 대기시간 {}초 초과)", customerId, queueName, entryAge / 1000);
                queueLeaseService.deleteLeaseByCustomerId(customerId);
                continue;
            }

            Set<Long> matchableCounselors = findMatchableCounselors(customerId, availableCounselorIds);
            if (!matchableCounselors.isEmpty()) {
                log.info("[대기열] 고객 {} 매칭 후보 발견 ({}에서) → 가능한 상담원: {}",
                        customerId, queueName, matchableCounselors);
                return new PopResult(customerId, matchableCounselors, 0, 0);
            }

            // 매칭 실패 - 임시 스택에 보관
            tempStack.add(new CustomerWithScore(customerId, score));
            log.info("[대기열] 고객 {} 매칭 불가 (가용 상담원 {} 모두 블랙리스트) → 임시 보관",
                    customerId, availableCounselorIds);
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
     * 임시 스택의 고객들을 원래 큐로 복원 (score를 유지하므로 원래 순서 유지)
     */
    private void restoreTempStack(List<CustomerWithScore> tempStack, String queueKey) {
        if (tempStack.isEmpty()) return;

        addAll(queueKey, tempStack);
        log.debug("임시 스택 {}명을 {}로 복원", tempStack.size(), queueKey);
    }

    /**
     * 임시 스택의 고객들을 Blacklist Queue로 이동
     */
    private void moveTempStackToBlacklistQueue(List<CustomerWithScore> tempStack) {
        if (tempStack.isEmpty()) return;

        addAll(BLACKLIST_QUEUE_KEY, tempStack);
        log.info("[대기열] {}명 Normal → Blacklist 이동: {}",
                tempStack.size(), tempStack.stream().map(CustomerWithScore::customerId).toList());
    }

    private void addAll(String queueKey, List<CustomerWithScore> customers) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        for (CustomerWithScore item : customers) {
            zSetOps.add(queueKey, item.customerId(), item.score());
        }
    }

    /**
     * 큐의 첫 번째 고객을 원자적으로 꺼냄 (ZPOPMIN).
     * 조회 후 삭제하는 방식과 달리 동시에 호출되어도 같은 고객이 두 번 꺼내지지 않는다.
     */
    private Optional<TypedTuple<String>> popFirst(String queueKey) {
        TypedTuple<String> first = redisTemplate.opsForZSet().popMin(queueKey);
        if (first == null || first.getValue() == null || first.getScore() == null) {
            return Optional.empty();
        }
        log.debug("{}에서 고객 {} 추출", queueName(queueKey), first.getValue());
        return Optional.of(first);
    }

    private long size(String queueKey) {
        Long size = redisTemplate.opsForZSet().zCard(queueKey);
        return size != null ? size : 0;
    }

    private String queueName(String queueKey) {
        return BLACKLIST_QUEUE_KEY.equals(queueKey) ? "Blacklist" : "Normal";
    }

    private void publishQueueUpdate() {
        QueueSizes sizes = getQueueSizes();
        queueEventPublisher.publishQueueUpdate(sizes.normalQueueSize(), sizes.blacklistQueueSize());
    }

    /**
     * 고객 ID와 score를 함께 저장하는 내부 레코드
     */
    private record CustomerWithScore(String customerId, double score) {}

    @Override
    public Map<String, Long> getCustomersFromRank(long fromRank) {
        Map<String, Long> result = new LinkedHashMap<>();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        long blacklistSize = size(BLACKLIST_QUEUE_KEY);

        if (fromRank <= blacklistSize) {
            // fromRank가 Blacklist 범위 내: Blacklist(fromRank-1부터) + Normal 전체
            putWithRanks(result, zSetOps.range(BLACKLIST_QUEUE_KEY, fromRank - 1, -1), fromRank);
            putWithRanks(result, zSetOps.range(NORMAL_QUEUE_KEY, 0, -1), blacklistSize + 1);
        } else {
            // fromRank가 Normal Queue 범위
            long normalStartIndex = fromRank - blacklistSize - 1;
            putWithRanks(result, zSetOps.range(NORMAL_QUEUE_KEY, normalStartIndex, -1), fromRank);
        }

        return result;
    }

    private void putWithRanks(Map<String, Long> result, Set<String> customerIds, long startRank) {
        if (customerIds == null) return;
        long rank = startRank;
        for (String customerId : customerIds) {
            result.put(customerId, rank++);
        }
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
