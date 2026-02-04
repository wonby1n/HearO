package com.ssafy.hearo.domain.matching.service;

import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 상담원 매칭 점수 계산 서비스
 *
 * 가중치 정책:
 * 1. 과거 상담 평점 (비중: 높음)
 *    - 평점 3점 미만: 음수 가중치
 *    - 평점 3점 이상: 양수 가중치
 *    - 여러 번 상담 시 평균 평점 사용
 *
 * 2. 카테고리 경험 (비중: 낮음)
 *    - 해당 카테고리 상담 경험이 많으면 양수 가중치
 *    - 음수 가중치 없음
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CounselorScoreService {

    private final ConsultationRepository consultationRepository;
    private final RegistrationRepository registrationRepository;

    // 가중치 설정값
    private static final double RATING_WEIGHT = 10.0;      // 평점 가중치 배수 (높음)
    private static final double CATEGORY_WEIGHT = 3.0;     // 카테고리 가중치 배수 (낮음)
    private static final double RATING_THRESHOLD = 3.0;    // 평점 기준점
    private static final int CATEGORY_MAX_BONUS = 5;       // 카테고리 경험 최대 보너스 (5회 이상은 동일)

    /**
     * 가용 상담원 중 최적의 상담원을 점수 기반으로 선택
     *
     * @param customerId 고객 ID (문자열, "customer_123" 형태 가능)
     * @param availableCounselorIds 매칭 가능한 상담원 ID 목록
     * @return 최적의 상담원 ID
     */
    public Long selectBestCounselor(String customerId, Set<Long> availableCounselorIds) {
        if (availableCounselorIds == null || availableCounselorIds.isEmpty()) {
            return null;
        }

        // customerId에서 숫자 추출
        Integer customerIdInt = extractCustomerId(customerId);

        // 고객의 최신 접수 정보 조회 (카테고리 확인용) - Product 함께 fetch
        String category = null;
        if (customerIdInt != null) {
            Optional<Registration> registration = registrationRepository
                    .findLatestByCustomerIdWithProduct(customerIdInt);
            if (registration.isPresent() && registration.get().getProduct() != null) {
                category = registration.get().getProduct().getCategory();
            }
        }

        // 각 상담원의 점수 계산
        Map<Long, Double> counselorScores = new HashMap<>();

        for (Long counselorId : availableCounselorIds) {
            double score = calculateScore(counselorId, customerIdInt, category);
            counselorScores.put(counselorId, score);
            log.debug("상담원 {} 점수: {} (고객: {}, 카테고리: {})",
                    counselorId, score, customerId, category);
        }

        // 최고 점수의 상담원 선택
        Long bestCounselor = counselorScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(availableCounselorIds.iterator().next());

        log.info("최적 상담원 선택: {} (점수: {}) - 고객: {}, 카테고리: {}",
                bestCounselor, counselorScores.get(bestCounselor), customerId, category);

        return bestCounselor;
    }

    /**
     * 상담원-고객 매칭 점수 계산
     */
    private double calculateScore(Long counselorId, Integer customerId, String category) {
        double score = 0.0;

        // 1. 과거 상담 평점 기반 가중치 (비중 높음)
        if (customerId != null) {
            score += calculateRatingScore(counselorId, customerId);
        }

        // 2. 카테고리 경험 기반 가중치 (비중 낮음)
        if (category != null && !category.isBlank()) {
            score += calculateCategoryScore(counselorId, category);
        }

        return score;
    }

    /**
     * 과거 상담 평점 기반 점수 계산
     * - 평점 3점 미만: 음수 점수
     * - 평점 3점 이상: 양수 점수
     */
    private double calculateRatingScore(Long counselorId, Integer customerId) {
        // 과거 상담 횟수 확인
        Long consultationCount = consultationRepository.countByCounselorAndCustomer(counselorId, customerId);

        if (consultationCount == null || consultationCount == 0) {
            // 과거 상담 이력 없음 - 중립 (0점)
            return 0.0;
        }

        // 평균 평점 조회
        BigDecimal avgRating = consultationRepository.findAvgRatingByCounselorAndCustomer(counselorId, customerId);

        if (avgRating == null) {
            // 평점이 없는 상담만 있음 (평가 미완료) - 중립
            return 0.0;
        }

        double rating = avgRating.doubleValue();

        // 평점 3점 기준으로 음수/양수 가중치 계산
        // 예: 평점 4.5 → (4.5 - 3.0) * 10 = +15점
        // 예: 평점 2.0 → (2.0 - 3.0) * 10 = -10점
        double ratingScore = (rating - RATING_THRESHOLD) * RATING_WEIGHT;

        log.debug("상담원 {} - 고객 {} 평점 점수: {} (평균 평점: {}, 상담 횟수: {})",
                counselorId, customerId, ratingScore, rating, consultationCount);

        return ratingScore;
    }

    /**
     * 카테고리 경험 기반 점수 계산
     * - 해당 카테고리 상담 경험이 많을수록 높은 점수 (양수만)
     */
    private double calculateCategoryScore(Long counselorId, String category) {
        Long categoryCount = consultationRepository.countByCounselorAndCategory(counselorId, category);

        if (categoryCount == null || categoryCount == 0) {
            return 0.0;
        }

        // 경험 횟수에 따른 보너스 (최대 CATEGORY_MAX_BONUS회까지)
        // 예: 3회 경험 → 3 * 3.0 = 9점
        // 예: 10회 경험 → 5 * 3.0 = 15점 (최대)
        long effectiveCount = Math.min(categoryCount, CATEGORY_MAX_BONUS);
        double categoryScore = effectiveCount * CATEGORY_WEIGHT;

        log.debug("상담원 {} 카테고리 '{}' 점수: {} (경험 횟수: {})",
                counselorId, category, categoryScore, categoryCount);

        return categoryScore;
    }

    /**
     * customerId 문자열에서 숫자 추출
     */
    private Integer extractCustomerId(String customerId) {
        if (customerId == null) {
            return null;
        }
        String idPart = customerId.startsWith("customer_")
                ? customerId.substring("customer_".length())
                : customerId;
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            log.debug("customerId '{}' 파싱 실패", customerId);
            return null;
        }
    }
}
