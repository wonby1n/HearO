package com.ssafy.hearo.domain.dashboard.service;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRatingRepository;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import com.ssafy.hearo.domain.dashboard.dto.DashboardSummaryResponse;
import com.ssafy.hearo.domain.dashboard.dto.WeeklyChartDto;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;
    private final ConsultationRatingRepository ratingRepository;

    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 스트레스 지수 계산 (User 엔티티 메서드 활용)
        // getDailyAvgStress()는 0.0~1.0을 반환하므로 * 100
        BigDecimal stressScore = user.getDailyAvgStress().multiply(BigDecimal.valueOf(100));
        int stressIndex = stressScore.intValue(); 

        // 2. 주간 차트 데이터 (이번 주 월~일)
        List<WeeklyChartDto> weeklyChart = getWeeklyChartData(userId);

        // 3. 총 상담 시간 계산 (초 -> 시:분 포맷팅)
        Long totalSeconds = consultationRepository.sumDurationByUserId(userId);
        String formattedDuration = formatDuration(totalSeconds != null ? totalSeconds : 0);

        // 4. 평균 평점
        BigDecimal avgRating = ratingRepository.findAverageRatingByUserId(userId);
        double customerSatisfaction = avgRating != null 
                ? avgRating.setScale(1, RoundingMode.HALF_UP).doubleValue() 
                : 0.0;

        // 5. 대기 고객 수 & 상담 상태 (이건 보통 Redis나 실시간 큐에서 가져와야 함. 여기선 더미)
        int waitingCount = 10; // TODO: WebSocket/Redis 연결 필요
        boolean isConsultationOn = false; // TODO: User Status 관리 필요

        return DashboardSummaryResponse.builder()
                .userName(user.getName())
                .waitingCustomerCount(waitingCount)
                .isConsultationOn(isConsultationOn)
                .stressIndex(stressIndex)
                .conditionMessage(getConditionMessage(stressIndex))
                .weeklyChart(weeklyChart)
                .totalDuration(formattedDuration)
                .customerSatisfaction(customerSatisfaction)
                .build();
    }

    // 주간 차트 데이터 생성 로직
    private List<WeeklyChartDto> getWeeklyChartData(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 이번 주 상담 기록 조회
        List<Consultation> consultations = consultationRepository.findAllByUserIdAndCreatedAtBetween(
                userId, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59)
        );

        // 날짜별로 그룹화해서 카운트 (Map<LocalDate, Long>)
        Map<LocalDate, Long> dailyCounts = consultations.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        // 월~금(또는 일)까지 빈 날짜도 0으로 채워서 리스트 생성
        List<WeeklyChartDto> chartData = new ArrayList<>();
        // 평일만 보여줄거면 i < 5, 주말 포함이면 i < 7
        for (int i = 0; i < 5; i++) { 
            LocalDate date = startOfWeek.plusDays(i);
            long count = dailyCounts.getOrDefault(date, 0L);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // Mon, Tue...
            
            chartData.add(new WeeklyChartDto(dayName, count));
        }
        return chartData;
    }

    // 초 -> HH:mm 형식 변환
    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return String.format("%d:%02d", hours, minutes);
    }

    // 스트레스 지수에 따른 메시지
    private String getConditionMessage(int stressIndex) {
        if (stressIndex < 30) return "좋은 컨디션이에요! 이 상태를 유지하세요.";
        if (stressIndex < 70) return "조금 지친 것 같아요. 스트레칭 어때요?";
        return "휴식이 필요해요! 잠시 상담을 멈추고 쉬세요.";
    }
}