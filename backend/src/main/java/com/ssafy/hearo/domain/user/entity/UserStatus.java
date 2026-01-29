package com.ssafy.hearo.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    AVAILABLE("대기 중", -0.5),    // 대기: 분당 -0.5
    IN_CALL("통화 중", -0.5),      // 통화: 분당 -0.5
    REST("휴식 중", 1.5),          // 휴식: 분당 +1.5
    OFFLINE("상담 OFF", 1.5);      // 오프라인: 변화 없음 (또는 자동 회복 설정 가능)

    private final String description;
    private final double energyRatePerMinute;
}