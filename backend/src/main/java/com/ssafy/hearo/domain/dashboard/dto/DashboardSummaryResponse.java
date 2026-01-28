package com.ssafy.hearo.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardSummaryResponse {

    // 1. 상단 사용자 정보
    private String userName;

    // 2. 좌측: 현재 스트레스 지수 (0~100)
    private int stressIndex; // 0% ~ 100%

    // 3. 중앙: 주간 실적 그래프 (이번 주 데이터)
    private List<WeeklyChartDto> weeklyChart;

    // 4. 하단: 통계
    private String totalDuration; // "135:12" 형식
    private double customerSatisfaction; // 4.8
}