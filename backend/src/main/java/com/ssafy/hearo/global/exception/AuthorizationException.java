package com.ssafy.hearo.global.exception;

/**
 * 권한이 없는 리소스에 접근 시 발생하는 예외 (HTTP 403)
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
