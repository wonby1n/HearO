package com.ssafy.hearo.domain.customer.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_phone", columnList = "phone")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20)
    private String name;

    @Column(length = 15)
    private String phone;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Blacklist> blacklists = new ArrayList<>();

    @Builder
    public Customer(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // ========== 비즈니스 메서드 ==========

    public void updateInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
