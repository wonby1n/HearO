package com.ssafy.hearo.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// JSON 응답 순서를 보기 좋게 고정 (성공여부 -> 코드 -> 메시지 -> 데이터)
@JsonPropertyOrder({"isSuccess", "code", "message", "data"})
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;

    private final String message;

    private final int code;

    // data가 null일 경우 JSON 응답에서 아예 제외하려면 아래 어노테이션 사용 (선택사항)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // =============================
    // 성공 응답 팩토리 메서드
    // =============================

    /**
     * 요청 성공 (데이터 포함)
     * 예: 조회 성공 시
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "요청에 성공하였습니다.", 200, data);
    }

    /**
     * 요청 성공 (메시지 커스텀 가능)
     */
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, 200, data);
    }
    
    /**
     * 요청 성공 (데이터 없음)
     * 예: 삭제 성공, 수정 성공 등 데이터 반환이 필요 없을 때
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(true, "요청에 성공하였습니다.", 200, null);
    }

    // =============================
    // 실패 응답 팩토리 메서드
    // =============================

    /**
     * 요청 실패
     * 예: 예외 처리 핸들러에서 사용
     */
    public static <T> BaseResponse<T> fail(String message, int code) {
        return new BaseResponse<>(false, message, code, null);
    }
}