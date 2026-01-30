package com.ssafy.hearo.domain.product.dto;

import com.ssafy.hearo.domain.product.entity.Product;
import lombok.Builder;

@Builder
public record ProductResponse(
        Integer id,
        String name,
        String code,
        String imageUrl,
        String category
) {
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .code(product.getCode())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .build();
    }
}