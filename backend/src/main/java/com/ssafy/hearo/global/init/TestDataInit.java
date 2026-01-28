package com.ssafy.hearo.global.init;

import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // Security 설정에 따라 다름
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 Bean으로 등록되어 있어야 함

    @Override
    public void run(String... args) throws Exception {
        // 중복 생성 방지
        if (userRepository.findByEmail("test@ssafy.com").isPresent()) {
            return;
        }

        User testUser = User.builder()
                .email("test@ssafy.com")
                .password(passwordEncoder.encode("1234")) // 비밀번호 암호화 필수!
                .name("김싸피")
                .role(UserRole.USER)
                .build();

        userRepository.save(testUser);
        System.out.println("✅ 테스트용 계정 생성 완료: test@ssafy.com / 1234");
    }
}