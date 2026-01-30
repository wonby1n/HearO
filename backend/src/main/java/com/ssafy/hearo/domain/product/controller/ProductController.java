package com.ssafy.hearo.domain.product.controller;

import com.ssafy.hearo.domain.product.dto.ProductResponse;
import com.ssafy.hearo.domain.product.service.ProductService;
import com.ssafy.hearo.global.common.response.BaseResponse; // BaseResponse 임포트!
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
     */
    @GetMapping("/{productId}")
    public ResponseEntity<BaseResponse<ProductResponse>> getProductDetail(@PathVariable("productId") Integer productId) {
        ProductResponse response = productService.getProductDetail(productId);

        // [수정] BaseResponse.success()로 감싸기
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}