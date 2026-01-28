package com.ssafy.hearo.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyChartDto {
    private String dayOfWeek; // "Mon", "Tue"...
    private long count;       // 상담 횟수
}