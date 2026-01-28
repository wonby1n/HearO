package com.ssafy.hearo.domain.registration.service;

import com.ssafy.hearo.domain.registration.dto.ReceiptResponse;

public interface ReceiptService {
    ReceiptResponse getLatestReceiptByCustomerId(Integer customerId);
}
