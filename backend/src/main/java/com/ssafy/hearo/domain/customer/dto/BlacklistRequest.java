package com.ssafy.hearo.domain.customer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BlacklistRequest {
    @NotNull(message = "고객 ID는 필수입니다.")
    private Integer customerId; // 블랙리스트에 넣을 고객 ID

    private String reason; // 차단 사유 (선택)
}