package com.ssafy.hearo.domain.product.entity;

import com.ssafy.hearo.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_code", columnList = "code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name; // 제품명

    @Column(nullable = false, length = 50, unique = true)
    private String code; // 제품 코드

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // 이미지 URL

    @Column(length = 30)
    private String category; // 제품 카테고리

    @Builder
    public Product(String name, String code, String imageUrl, String category) {
        this.name = name;
        this.code = code;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}