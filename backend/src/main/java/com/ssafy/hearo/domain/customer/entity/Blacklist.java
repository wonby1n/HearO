package com.ssafy.hearo.domain.customer.entity;

import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blacklists",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_blacklists_user_customer", columnNames = {"user_id", "customer_id"})
        },
        indexes = {
                @Index(name = "idx_blacklists_customer_id", columnList = "customer_id"),
                @Index(name = "idx_blacklists_user_id", columnList = "user_id") // 상담원 기준 조회용 인덱스 추가
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blacklist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // [변경] Integer -> Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 200)
    private String reason;

    @Builder
    public Blacklist(User user, Customer customer, String reason) {
        this.user = user;
        this.customer = customer;
        this.reason = reason;
    }

    // 비즈니스 메서드
    public void updateReason(String reason) {
        this.reason = reason;
    }
}