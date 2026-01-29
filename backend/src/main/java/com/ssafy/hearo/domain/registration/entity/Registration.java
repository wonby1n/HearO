package com.ssafy.hearo.domain.registration.entity;

import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", indexes = {
        @Index(name = "idx_registrations_customer_id", columnList = "customer_id"),
        @Index(name = "idx_registrations_product_category", columnList = "product_category"),
        @Index(name = "idx_registrations_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(columnDefinition = "TEXT")
    private String symptom;

    @Column(name = "error_code", length = 30)
    private String errorCode;

    @Column(name = "model_code", length = 50)
    private String modelCode;

    @Column(name = "product_category", length = 30)
    private String productCategory;

    @Column(name = "bought_at")
    private LocalDateTime boughtAt;

    @Builder
    public Registration(Customer customer, String symptom, String errorCode, 
                        String modelCode, String productCategory, LocalDateTime boughtAt) {
        this.customer = customer;
        this.symptom = symptom;
        this.errorCode = errorCode;
        this.modelCode = modelCode;
        this.productCategory = productCategory;
        this.boughtAt = boughtAt;
    }

    // ========== 비즈니스 메서드 ==========

    public void updateSymptom(String symptom) {
        this.symptom = symptom;
    }

    public void updateErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
