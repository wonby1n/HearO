package com.ssafy.hearo.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("일반 상담원"),
    VETERAN("베테랑 상담원"),
    ADMIN("관리자");

    private final String description;
}
