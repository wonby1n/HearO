package com.ssafy.hearo.domain.customer.repository;

import com.ssafy.hearo.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByNameAndPhone(String name, String phone);

    Optional<Customer> findByPhone(String phone);
}
