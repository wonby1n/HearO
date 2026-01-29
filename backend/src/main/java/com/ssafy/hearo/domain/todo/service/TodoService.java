package com.ssafy.hearo.domain.todo.service;

import com.ssafy.hearo.domain.todo.dto.TodoCreateRequest;
import com.ssafy.hearo.domain.todo.dto.TodoResponse;
import com.ssafy.hearo.domain.todo.entity.Todo;
import com.ssafy.hearo.domain.todo.repository.TodoRepository;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    // 1. 내 투두 리스트 조회
    public List<TodoResponse> getMyTodos(Long userId) {
        return todoRepository.findAllByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());
    }

    // 2. 투두 생성
    @Transactional
    public TodoResponse createTodo(Long userId, TodoCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Todo todo = Todo.builder()
                .user(user)
                .content(request.getContent())
                .isCompleted(false) // 기본값 false
                .build();

        Todo savedTodo = todoRepository.save(todo);
        return TodoResponse.from(savedTodo);
    }

    // 3. 투두 완료/미완료 토글
    @Transactional
    public TodoResponse toggleTodo(Long userId, Long todoId) {
        Todo todo = findTodoAndCheckOwner(userId, todoId);
        todo.toggleCompletion(); // Dirty Checking으로 자동 업데이트
        return TodoResponse.from(todo);
    }

    // 4. 투두 삭제
    @Transactional
    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = findTodoAndCheckOwner(userId, todoId);
        todoRepository.delete(todo);
    }
    
    // 5. 투두 내용 수정 (선택 사항)
    @Transactional
    public TodoResponse updateTodoContent(Long userId, Long todoId, String newContent) {
        Todo todo = findTodoAndCheckOwner(userId, todoId);
        todo.updateContent(newContent);
        return TodoResponse.from(todo);
    }

    // 검증 로직 추출 (내 투두가 맞는지 확인)
    private Todo findTodoAndCheckOwner(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));
        
        if (!todo.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 할 일에 대한 권한이 없습니다.");
        }
        return todo;
    }
}