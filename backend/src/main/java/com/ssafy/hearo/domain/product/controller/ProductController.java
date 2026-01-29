package com.ssafy.hearo.domain.product.controller;

import com.ssafy.hearo.domain.product.dto.ProductResponse;
import com.ssafy.hearo.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 ID로 상세 정보 조회
     * 예: GET /api/v1/products/1
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable("productId") Integer productId) {
        ProductResponse response = productService.getProductDetail(productId);
        return ResponseEntity.ok(response);
    }
}