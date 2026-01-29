package com.ssafy.hearo.domain.todo.controller;

import com.ssafy.hearo.domain.todo.dto.TodoCreateRequest;
import com.ssafy.hearo.domain.todo.dto.TodoResponse;
import com.ssafy.hearo.domain.todo.service.TodoService;
import com.ssafy.hearo.global.common.response.BaseResponse;
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

    // 1. 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<TodoResponse>>> getMyTodos(@AuthenticationPrincipal CustomUserDetails user) {
        List<TodoResponse> todoList = todoService.getMyTodos(user.getId());
        // 데이터가 있으니까 success(data) 사용
        return ResponseEntity.ok(BaseResponse.success(todoList));
    }

    // 2. 생성
    @PostMapping
    public ResponseEntity<BaseResponse<TodoResponse>> createTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody TodoCreateRequest request) {
        TodoResponse newTodo = todoService.createTodo(user.getId(), request);
        return ResponseEntity.ok(BaseResponse.success(newTodo));
    }

    // 3. 완료 토글
    @PatchMapping("/{todoId}/check")
    public ResponseEntity<BaseResponse<TodoResponse>> toggleTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long todoId) {
        TodoResponse updatedTodo = todoService.toggleTodo(user.getId(), todoId);
        return ResponseEntity.ok(BaseResponse.success(updatedTodo));
    }

    // 4. 내용 수정
    @PatchMapping("/{todoId}")
    public ResponseEntity<BaseResponse<TodoResponse>> updateTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long todoId,
            @RequestBody TodoCreateRequest request) {
        TodoResponse updatedTodo = todoService.updateTodoContent(user.getId(), todoId, request.getContent());
        return ResponseEntity.ok(BaseResponse.success(updatedTodo));
    }

    // 5. 삭제
    @DeleteMapping("/{todoId}")
    public ResponseEntity<BaseResponse<Void>> deleteTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long todoId) {
        todoService.deleteTodo(user.getId(), todoId);
        // 데이터가 없으니까 인자 없는 success() 사용 (BaseResponse가 알아서 data 필드를 빼줌)
        return ResponseEntity.ok(BaseResponse.success());
    }
}