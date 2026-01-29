package com.ssafy.hearo.domain.auth.dto;

import com.ssafy.hearo.domain.customer.entity.Customer;

public record CustomerLoginResponse(
        String accessToken,
        Integer customerId,
        String name,
        String phone
) {
    public static CustomerLoginResponse of(String accessToken, Customer customer) {
        return new CustomerLoginResponse(
                accessToken,
                customer.getId(),
                customer.getName(),
                customer.getPhone()
        );
    }
}
