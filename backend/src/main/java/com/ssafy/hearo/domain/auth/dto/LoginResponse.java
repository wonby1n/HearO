package com.ssafy.hearo.domain.auth.dto;

import com.ssafy.hearo.domain.user.entity.User;
import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        Long userId,
        String username,
        String userRole
) {
    public static LoginResponse of(String accessToken, User user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .username(user.getName())
                .userRole(user.getRole().name())
                .build();
    }
}
