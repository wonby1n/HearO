package com.ssafy.hearo.domain.todo.dto;

import com.ssafy.hearo.domain.todo.entity.Todo;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 파싱용
public class TodoCreateRequest {
    @NotBlank(message = "할 일을 입력해주세요.")
    private String content;
    
    // 생성자나 빌더 추가 가능
}