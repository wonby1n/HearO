package com.ssafy.hearo.global.exception;

/**
 * 잘못된 요청 데이터나 검증 실패 시 발생하는 예외 (HTTP 400)
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
