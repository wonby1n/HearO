package com.ssafy.hearo.domain.user.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
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

    // ========== 에너지 관리 (Anchor Fields) ==========

    // 마지막으로 상태가 변경되거나 이벤트가 발생했을 때의 에너지 값 (0~100)
    @Column(name = "last_energy_value", nullable = false)
    private Integer lastEnergyValue = 100;

    // 마지막 상태 변경 시간 (기준 시간)
    @Column(name = "last_status_change_time", nullable = false)
    private LocalDateTime lastStatusChangeTime = LocalDateTime.now();

    // 현재 상담원 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private UserStatus status = UserStatus.OFFLINE;

    // ========== 생성자 ==========
    @Builder
    public User(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : UserRole.USER;
        this.lastEnergyValue = 100;
        this.status = UserStatus.REST;
        this.lastStatusChangeTime = LocalDateTime.now();
    }

    // ========== 비즈니스 메서드 (핵심 로직) ==========

    /**
     * [조회용] 현재 시간 기준으로 실시간 에너지를 계산해서 반환
     * DB를 업데이트하지 않고 계산값만 리턴함 (Lazy Calculation)
     */
    public int calculateRealTimeEnergy(LocalDateTime now) {
        if (this.status == UserStatus.OFFLINE) {
            return this.lastEnergyValue; // 오프라인이면 변화 없음 (정책에 따라 변경 가능)
        }

        // 1. 흐른 시간(분) 계산
        long minutesPassed = Duration.between(this.lastStatusChangeTime, now).toMinutes();

        // 2. 변화량 계산 (시간 * 분당 증감률)
        double change = minutesPassed * this.status.getEnergyRatePerMinute();

        // 3. 결과 계산 (0 ~ 100 클램핑)
        int currentEnergy = (int) (this.lastEnergyValue + change);
        return Math.max(0, Math.min(100, currentEnergy));
    }

    /**
     * [변경용] 상태 변경 또는 이벤트 발생 시 DB 필드 업데이트 (Anchor 갱신)
     * Service에서 호출됨
     */
    public void updateEnergyAnchor(UserStatus newStatus, int calculatedCurrentEnergy) {
        this.lastEnergyValue = calculatedCurrentEnergy;
        this.status = newStatus;
        this.lastStatusChangeTime = LocalDateTime.now();
    }

    /**
     * [변경용] 욕설 감지 등 즉시 차감 이벤트 발생 시
     */
    public void decreaseEnergyImmediately(int amount) {
        // 현재까지 흐른 시간 반영하여 최신화
        int current = calculateRealTimeEnergy(LocalDateTime.now());

        // 데미지 적용
        this.lastEnergyValue = Math.max(0, current - amount);
        this.lastStatusChangeTime = LocalDateTime.now(); // 기준 시간 갱신
        // 상태는 그대로 유지
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String name) {
        this.name = name;
    }
}