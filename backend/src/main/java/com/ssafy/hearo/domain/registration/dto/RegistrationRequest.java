package com.ssafy.hearo.domain.registration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegistrationRequest {

    @NotNull(message = "증상은 필수 입력값입니다.")
    private String symptom;

    private String errorCode;

    // [여기 수정] modelCode 삭제하고 productId 추가
    @NotNull(message = "제품 ID는 필수 입력값입니다.")
    private Integer productId;

    private String manufacturedAt;
    private String warrantyEndsAt;
}