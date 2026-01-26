package com.ssafy.hearo.domain.registration.controller;


import com.ssafy.hearo.domain.registration.dto.ReceiptResponse;
import com.ssafy.hearo.domain.registration.service.ReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/registerations/receipt")
public class ReceiptController {

    private final Integer mockId = 123;
    private final ReceiptService receiptService;

    @GetMapping("/latest")
    public ResponseEntity<ReceiptResponse> getLatestReceipt(HttpServletRequest request) {
        Integer customerId = mockId;
        return ResponseEntity.ok(receiptService.getLatestReceiptByCustomerId(customerId));
    }

}
