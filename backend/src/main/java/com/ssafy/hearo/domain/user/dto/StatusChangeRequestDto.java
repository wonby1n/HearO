package com.ssafy.hearo.domain.user.dto;

import com.ssafy.hearo.domain.user.entity.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StatusChangeRequestDto {
    private UserStatus status; // 변경할 상태 (REST, IN_CALL, AVAILABLE...)
}