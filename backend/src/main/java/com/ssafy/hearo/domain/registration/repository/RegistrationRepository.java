package com.ssafy.hearo.domain.registration.repository;

import com.ssafy.hearo.domain.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

}