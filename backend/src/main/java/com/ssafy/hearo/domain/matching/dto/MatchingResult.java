package com.ssafy.hearo.domain.matching.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 매칭 결과 DTO
 */
@Getter
@Builder
public class MatchingResult {

    private String customerId;
    private Long counselorId;
    private String roomName;
    private boolean success;
    private String message;

    public static MatchingResult success(String customerId, Long counselorId, String roomName) {
        return MatchingResult.builder()
                .customerId(customerId)
                .counselorId(counselorId)
                .roomName(roomName)
                .success(true)
                .message("매칭 성공")
                .build();
    }

    public static MatchingResult noAvailableCounselor() {
        return MatchingResult.builder()
                .success(false)
                .message("가용 상담원 없음")
                .build();
    }

    public static MatchingResult noWaitingCustomer() {
        return MatchingResult.builder()
                .success(false)
                .message("대기 고객 없음")
                .build();
    }

    public static MatchingResult noMatchableCustomer(int movedToBlacklist) {
        return MatchingResult.builder()
                .success(false)
                .message(String.format("매칭 가능한 고객 없음 (%d명 Blacklist Queue로 이동)", movedToBlacklist))
                .build();
    }
}
