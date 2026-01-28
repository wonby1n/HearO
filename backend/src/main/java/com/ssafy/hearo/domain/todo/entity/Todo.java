package com.ssafy.hearo.domain.todo.entity;

import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todos", indexes = {
        @Index(name = "idx_todos_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Todo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String content;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean isCompleted = false;

    // 비즈니스 로직: 완료 상태 토글
    public void toggleCompletion() {
        this.isCompleted = !this.isCompleted;
    }
    
    // 내용 수정 (필요하다면)
    public void updateContent(String content) {
        this.content = content;
    }
}