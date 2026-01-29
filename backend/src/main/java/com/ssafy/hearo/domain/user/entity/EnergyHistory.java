package com.ssafy.hearo.domain.user.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "energy_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnergyHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer previousEnergy; // 변경 전 에너지

    @Column(nullable = false)
    private Integer currentEnergy;  // 변경 후 에너지 (결과값)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // 당시 상태

    @Column(length = 100)
    private String reason; // 변경 사유 (예: "통화 종료", "욕설 감지", "휴식 시작")

    @Builder
    public EnergyHistory(User user, Integer previousEnergy, Integer currentEnergy, UserStatus status, String reason) {
        this.user = user;
        this.previousEnergy = previousEnergy;
        this.currentEnergy = currentEnergy;
        this.status = status;
        this.reason = reason;
    }
}