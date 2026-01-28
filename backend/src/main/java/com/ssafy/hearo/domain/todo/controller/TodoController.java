package com.ssafy.hearo.domain.todo.controller;

import com.ssafy.hearo.domain.todo.dto.TodoCreateRequest;
import com.ssafy.hearo.domain.todo.dto.TodoResponse;
import com.ssafy.hearo.domain.todo.service.TodoService;
import com.ssafy.hearo.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 1. 조회 (GET /api/v1/todos)
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getMyTodos(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(todoService.getMyTodos(user.getId()));
    }

    // 2. 생성 (POST /api/v1/todos)
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(todoService.createTodo(user.getId(), request));
    }

    // 3. 완료 토글 (PATCH /api/v1/todos/{todoId}/check)
    @PatchMapping("/{todoId}/check")
    public ResponseEntity<TodoResponse> toggleTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long todoId) {
        return ResponseEntity.ok(todoService.toggleTodo(user.getId(), todoId));
    }
    
    // 4. 내용 수정 (PATCH /api/v1/todos/{todoId}) -> 필요시 사용
    @PatchMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long todoId,
            @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(todoService.updateTodoContent(user.getId(), todoId, request.getContent()));
    }

    // 5. 삭제 (DELETE /api/v1/todos/{todoId})
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long todoId) {
        todoService.deleteTodo(user.getId(), todoId);
        return ResponseEntity.noContent().build();
    }
}