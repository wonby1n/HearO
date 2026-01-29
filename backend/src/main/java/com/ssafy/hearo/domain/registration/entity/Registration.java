package com.ssafy.hearo.domain.registration.entity;

import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.product.entity.Product;
import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "registrations", indexes = {
        @Index(name = "idx_registrations_customer_id", columnList = "customer_id"),
        @Index(name = "idx_registrations_product_id", columnList = "product_id"),
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String symptom; // 증상

    @Column(name = "error_code", length = 30)
    private String errorCode;

    // [이동] Product -> Registration
    // 개별 기기의 제조일자
    @Column(name = "manufactured_at")
    private LocalDate manufacturedAt;

    // [이동] Product -> Registration
    // 개별 기기의 보증마감일
    @Column(name = "warranty_ends_at")
    private LocalDate warrantyEndsAt;

    // boughtAt은 삭제됨

    @Builder
    public Registration(Customer customer, Product product, String symptom, String errorCode,
                        LocalDate manufacturedAt, LocalDate warrantyEndsAt) {
        this.customer = customer;
        this.product = product;
        this.symptom = symptom;
        this.errorCode = errorCode;
        this.manufacturedAt = manufacturedAt;
        this.warrantyEndsAt = warrantyEndsAt;
    }

    // ========== 비즈니스 메서드 ==========

    public void updateSymptom(String symptom) {
        this.symptom = symptom;
    }

    public void updateErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}