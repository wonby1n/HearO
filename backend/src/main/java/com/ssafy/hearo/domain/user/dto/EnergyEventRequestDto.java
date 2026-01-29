package com.ssafy.hearo.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EnergyEventRequestDto {
    private int damage; // 깎을 에너지 양 (예: 5)
    // 필요하면 eventType (욕설, 고성 등) 필드 추가 가능
}