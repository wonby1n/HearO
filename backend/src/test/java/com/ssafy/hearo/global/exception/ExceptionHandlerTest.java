package com.ssafy.hearo.global.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("예외 처리 상태코드 테스트")
class ExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("커스텀 예외 핸들러")
    class CustomExceptionTest {

        @Test
        @DisplayName("BadRequestException → 400 Bad Request")
        void badRequestExceptionShouldReturn400() {
            // given
            BadRequestException ex = new BadRequestException("잘못된 요청입니다.");

            // when
            var response = handler.handleBadRequestException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getCode()).isEqualTo(400);
            assertThat(response.getBody().getMessage()).isEqualTo("잘못된 요청입니다.");
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ BadRequestException → 400 OK");
        }

        @Test
        @DisplayName("AuthorizationException → 403 Forbidden")
        void authorizationExceptionShouldReturn403() {
            // given
            AuthorizationException ex = new AuthorizationException("해당 리소스에 대한 권한이 없습니다.");

            // when
            var response = handler.handleAuthorizationException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getCode()).isEqualTo(403);
            assertThat(response.getBody().getMessage()).isEqualTo("해당 리소스에 대한 권한이 없습니다.");
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ AuthorizationException → 403 OK");
        }

        @Test
        @DisplayName("ResourceNotFoundException → 404 Not Found")
        void resourceNotFoundExceptionShouldReturn404() {
            // given
            ResourceNotFoundException ex = new ResourceNotFoundException("사용자를 찾을 수 없습니다.");

            // when
            var response = handler.handleResourceNotFoundException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getCode()).isEqualTo(404);
            assertThat(response.getBody().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ ResourceNotFoundException → 404 OK");
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException 문자열 분기 (레거시 호환)")
    class IllegalArgumentExceptionTest {

        @Test
        @DisplayName("'권한' 포함 메시지 → 403 Forbidden")
        void shouldReturn403WhenMessageContainsPermission() {
            // given
            IllegalArgumentException ex = new IllegalArgumentException("해당 할 일에 대한 권한이 없습니다.");

            // when
            var response = handler.handleIllegalArgumentException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getCode()).isEqualTo(403);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalArgumentException('권한') → 403 OK");
        }

        @Test
        @DisplayName("'찾을 수 없' 포함 메시지 → 404 Not Found")
        void shouldReturn404WhenMessageContainsNotFound() {
            // given
            IllegalArgumentException ex = new IllegalArgumentException("사용자를 찾을 수 없습니다.");

            // when
            var response = handler.handleIllegalArgumentException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getCode()).isEqualTo(404);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalArgumentException('찾을 수 없') → 404 OK");
        }

        @Test
        @DisplayName("'없음' 포함 메시지 → 404 Not Found")
        void shouldReturn404WhenMessageContainsEmpty() {
            // given
            IllegalArgumentException ex = new IllegalArgumentException("데이터 없음");

            // when
            var response = handler.handleIllegalArgumentException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getCode()).isEqualTo(404);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalArgumentException('없음') → 404 OK");
        }

        @Test
        @DisplayName("'not found' 포함 메시지 → 404 Not Found")
        void shouldReturn404WhenMessageContainsNotFoundEnglish() {
            // given
            IllegalArgumentException ex = new IllegalArgumentException("Rating not found");

            // when
            var response = handler.handleIllegalArgumentException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getCode()).isEqualTo(404);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalArgumentException('not found') → 404 OK");
        }

        @Test
        @DisplayName("기타 메시지 → 400 Bad Request")
        void shouldReturn400ForOtherMessages() {
            // given
            IllegalArgumentException ex = new IllegalArgumentException("이미 블랙리스트에 등록된 고객입니다.");

            // when
            var response = handler.handleIllegalArgumentException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getCode()).isEqualTo(400);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalArgumentException(기타) → 400 OK");
        }

        @Test
        @DisplayName("null 메시지 → 400 Bad Request")
        void shouldReturn400ForNullMessage() {
            // given
            IllegalArgumentException ex = new IllegalArgumentException((String) null);

            // when
            var response = handler.handleIllegalArgumentException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getCode()).isEqualTo(400);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalArgumentException(null) → 400 OK");
        }
    }

    @Nested
    @DisplayName("IllegalStateException")
    class IllegalStateExceptionTest {

        @Test
        @DisplayName("IllegalStateException → 409 Conflict")
        void shouldReturn409ForIllegalState() {
            // given
            IllegalStateException ex = new IllegalStateException("이미 처리된 요청입니다.");

            // when
            var response = handler.handleIllegalStateException(ex);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().getCode()).isEqualTo(409);
            assertThat(response.getBody().getIsSuccess()).isFalse();
            System.out.println("✓ IllegalStateException → 409 OK");
        }
    }
}
