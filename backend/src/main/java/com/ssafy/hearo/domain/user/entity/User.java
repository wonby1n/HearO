package com.ssafy.hearo.domain.user.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_stress_reset_date", columnList = "stress_reset_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;

    // ========== 일일 스트레스 관리 필드 ==========
    
    @Column(name = "daily_profanity_count", nullable = false)
    private Integer dailyProfanityCount = 0;

    @Column(name = "daily_aggression_sum", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyAggressionSum = BigDecimal.ZERO;

    @Column(name = "daily_call_count", nullable = false)
    private Integer dailyCallCount = 0;

    @Column(name = "stress_reset_date", nullable = false)
    private LocalDate stressResetDate = LocalDate.now();

    // ========== 생성자 ==========

    @Builder
    public User(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : UserRole.USER;
        this.dailyProfanityCount = 0;
        this.dailyAggressionSum = BigDecimal.ZERO;
        this.dailyCallCount = 0;
        this.stressResetDate = LocalDate.now();
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 일일 평균 스트레스 점수 계산 (0.00 ~ 1.00)
     */
    public BigDecimal getDailyAvgStress() {
        if (dailyCallCount == 0) {
            return BigDecimal.ZERO;
        }
        return dailyAggressionSum.divide(
                BigDecimal.valueOf(dailyCallCount), 
                2, 
                RoundingMode.HALF_UP
        );
    }

    /**
     * 상담 종료 후 스트레스 데이터 업데이트
     */
    public void addConsultationStress(int profanityCount, BigDecimal avgAggressionScore) {
        checkAndResetDaily();
        this.dailyProfanityCount += profanityCount;
        this.dailyAggressionSum = this.dailyAggressionSum.add(
                avgAggressionScore != null ? avgAggressionScore : BigDecimal.ZERO
        );
        this.dailyCallCount++;
    }

    /**
     * 날짜가 바뀌었으면 일일 스트레스 초기화
     */
    public void checkAndResetDaily() {
        if (!LocalDate.now().equals(this.stressResetDate)) {
            resetDailyStress();
        }
    }

    /**
     * 일일 스트레스 수동 초기화
     */
    public void resetDailyStress() {
        this.dailyProfanityCount = 0;
        this.dailyAggressionSum = BigDecimal.ZERO;
        this.dailyCallCount = 0;
        this.stressResetDate = LocalDate.now();
    }

    /**
     * 비밀번호 변경
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 역할 변경 (관리자용)
     */
    public void updateRole(UserRole role) {
        this.role = role;
    }

    /**
     * 프로필 수정
     */
    public void updateProfile(String name) {
        this.name = name;
    }
}
