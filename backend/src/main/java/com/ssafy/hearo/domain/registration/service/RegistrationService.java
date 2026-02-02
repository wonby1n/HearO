package com.ssafy.hearo.domain.registration.service;

import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.product.entity.Product;
import com.ssafy.hearo.domain.product.repository.ProductRepository;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.registration.dto.RegistrationRequest;
import com.ssafy.hearo.domain.registration.dto.RegistrationDetailResponse;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository; // Product 조회를 위해 필요

    public Long createRegistration(Integer customerId, RegistrationRequest request) {
        // 1. 고객 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객을 찾을 수 없습니다."));

        // 2. 제품 조회 [변경 포인트!]
        // modelCode로 찾던 걸 id로 바로 조회 (훨씬 빠르고 표준적임)
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("해당 제품을 찾을 수 없습니다. ID: " + request.getProductId()));

        // 3. 접수 정보 생성
        LocalDate manufacturedDate = request.getManufacturedAt() != null
                ? LocalDate.parse(request.getManufacturedAt()) : null;

        LocalDate warrantyEndsDate = request.getWarrantyEndsAt() != null
                ? LocalDate.parse(request.getWarrantyEndsAt()) : null;

        Registration registration = Registration.builder()
                .customer(customer)
                .product(product)
                .symptom(request.getSymptom())
                .errorCode(request.getErrorCode())
                .manufacturedAt(manufacturedDate)
                .warrantyEndsAt(warrantyEndsDate)
                .build();

        // ID 반환 (Long 변환 포함)
        return registrationRepository.save(registration).getId().longValue();
    }

    @Transactional(readOnly = true)
    public RegistrationDetailResponse getRegistrationDetail(Integer registrationId) {
        Registration registration = registrationRepository.findDetailById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("해당 접수 내역을 찾을 수 없습니다. ID: " + registrationId));

        return RegistrationDetailResponse.from(registration);
    }
}