package com.ssafy.hearo.domain.todo.repository;

import com.ssafy.hearo.domain.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    // 특정 사용자의 투두 리스트를 생성 시간 순(또는 ID순)으로 조회
    List<Todo> findAllByUserIdOrderByCreatedAtAsc(Long userId);
}