package com.ssafy.hearo.domain.product.repository;

import com.ssafy.hearo.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}