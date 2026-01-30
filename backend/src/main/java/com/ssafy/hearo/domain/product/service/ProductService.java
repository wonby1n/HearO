package com.ssafy.hearo.domain.product.service;

import com.ssafy.hearo.domain.product.dto.ProductResponse;
import com.ssafy.hearo.domain.product.entity.Product;
import com.ssafy.hearo.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse getProductDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("해당 제품을 찾을 수 없습니다. ID: " + productId));

        return ProductResponse.from(product);
    }
}