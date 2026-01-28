package com.ssafy.hearo.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardSummaryResponse {
    
    // 1. 상단 사용자 정보 & 상태
    private String userName;
    private Integer waitingCustomerCount; // 대기 고객 수 (별도 로직 필요)
    private boolean isConsultationOn;     // 상담 ON/OFF 상태 (Redis나 별도 상태 관리 추천)

    // 2. 좌측: 현재 스트레스 지수 (0~100)
    private int stressIndex; // 0% ~ 100%
    private String conditionMessage; // "좋은 컨디션이에요!", "휴식이 필요해요" 등

    // 3. 중앙: 주간 실적 그래프 (이번 주 데이터)
    private List<WeeklyChartDto> weeklyChart;

    // 4. 하단: 통계
    private String totalDuration; // "135:12" 형식
    private double customerSatisfaction; // 4.8
}