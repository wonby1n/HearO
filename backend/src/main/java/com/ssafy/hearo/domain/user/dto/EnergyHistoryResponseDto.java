package com.ssafy.hearo.domain.user.dto;

import com.ssafy.hearo.domain.user.entity.EnergyHistory;
import com.ssafy.hearo.domain.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EnergyHistoryResponseDto {
    private Long id;
    private int currentEnergy;      // 변화 후 에너지
    private UserStatus status; // 당시 상태
    private String reason;          // 사유 (욕설 감지, 상태 변경 등)
    private LocalDateTime createdAt; // 발생 시간

    // 엔티티 -> DTO 변환 편의 메서드
    public static EnergyHistoryResponseDto from(EnergyHistory history) {
        return EnergyHistoryResponseDto.builder()
                .id(history.getId())
                .currentEnergy(history.getCurrentEnergy())
                .status(history.getStatus())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}