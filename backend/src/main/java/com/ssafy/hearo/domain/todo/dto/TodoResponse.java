package com.ssafy.hearo.domain.todo.dto;

import com.ssafy.hearo.domain.todo.entity.Todo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoResponse {
    private Long id;
    private String content;
    private boolean isCompleted;

    // Entity -> DTO 변환 메서드 (편의성)
    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .content(todo.getContent())
                .isCompleted(todo.isCompleted())
                .build();
    }
}