package com.ssafy.hearo.domain.queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상담을 신청 할 때 서버로 보내는 데이터를 담음
 * 요청 데이터가 유효한지 검사하는 Validation 설정이 핵심
 */

@Getter
@NoArgsConstructor
public class RegistrationRequest {

//    @NotBlank(message = "증상 설명은 필수입니다")
    private String symptom;

    private String errorCode;

    private String modelCode;

//    @NotBlank(message = "제품 카테고리는 필수입니다")
    private String productCategory;

//    @NotNull(message = "구매일자는 필수입니다")
    private String boughtAt;
}
