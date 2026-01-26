package com.ssafy.hearo.domain.registration.service;

import com.ssafy.hearo.domain.registration.dto.ReceiptResponse;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptServiceImpl implements ReceiptService {

    private final RegistrationRepository registrationRepository;

    @Override
    public ReceiptResponse getLatestReceiptByCustomerId(Integer customerId) {
        return registrationRepository.findTopByCustomerIdOrderByCreatedAtDesc(customerId)
                .map(ReceiptResponse::from)
                .orElseThrow(() ->
                        new IllegalStateException("해당 고객의 접수 내역이 없습니다. customerId=" + customerId)
                );
    }
}
