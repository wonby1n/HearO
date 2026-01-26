package com.ssafy.hearo.domain.consultation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TerminationReason {
    NORMAL("정상 종료"),
    PROFANITY_LIMIT("욕설 횟수 초과"),
    AGGRESSION_LIMIT("공격성 점수 초과"),
    CUSTOMER_REQUEST("고객 요청 종료"),
    AGENT_REQUEST("상담원 요청 종료"),
    SYSTEM_ERROR("시스템 오류"),
    TRANSFERRED("AI 상담사로 전환");

    private final String description;
}
