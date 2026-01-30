package com.ssafy.hearo.domain.customer.dto;

import com.ssafy.hearo.domain.customer.entity.Blacklist;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BlacklistResponse(
        Long blacklistId,
        Integer customerId,
        String customerName,
        String customerPhone,
        String reason,
        LocalDateTime createdAt
) {
    public static BlacklistResponse from(Blacklist blacklist) {
        return BlacklistResponse.builder()
                .blacklistId(blacklist.getId())
                .customerId(blacklist.getCustomer().getId())
                .customerName(blacklist.getCustomer().getName())
                .customerPhone(blacklist.getCustomer().getPhone())
                .reason(blacklist.getReason())
                .createdAt(blacklist.getCreatedAt())
                .build();
    }
}