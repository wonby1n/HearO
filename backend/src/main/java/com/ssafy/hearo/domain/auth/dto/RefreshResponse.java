package com.ssafy.hearo.domain.auth.dto;

public record RefreshResponse(
        String accessToken
) {
    public static RefreshResponse of(String accessToken) {
        return new RefreshResponse(accessToken);
    }
}
