package com.ssafy.hearo.global.init;

import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.product.entity.Product;
import com.ssafy.hearo.domain.product.repository.ProductRepository;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository; // [추가] Customer 리포지토리 주입
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initTestUser();
        initTestCustomer(); // [추가] 고객 생성 실행
        initTestProduct();
    }

    // 1. 테스트 유저 생성 (로그인용)
    private void initTestUser() {
        if (userRepository.findByEmail("test@ssafy.com").isPresent()) {
            return;
        }

        User testUser = User.builder()
                .email("test@ssafy.com")
                .password(passwordEncoder.encode("1234"))
                .name("김싸피")
                .role(UserRole.USER)
                .build();

        userRepository.save(testUser);
        System.out.println("✅ 테스트용 User 생성 완료: test@ssafy.com (ID: " + testUser.getId() + ")");
    }

    // 2. 테스트 고객 생성 (상담 접수용)
    private void initTestCustomer() {
        String testPhone = "010-1234-5678";

        // 이미 있으면 생성 안 함
        if (customerRepository.findByPhone(testPhone).isPresent()) {
            return;
        }

        Customer testCustomer = Customer.builder()
                .name("박고객")
                .phone(testPhone)
                .build();

        customerRepository.save(testCustomer);
        // [중요] 이 ID를 X-User-ID 헤더에 넣어야 해!
        System.out.println("✅ 테스트용 Customer 생성 완료: 박고객 (ID: " + testCustomer.getId() + ")");
    }

    // 3. 테스트 상품(냉장고) 생성
    private void initTestProduct() {
        if (productRepository.count() > 0) {
            return;
        }

        Product bespokeFridge = Product.builder()
                .name("BESPOKE 냉장고 4도어 프리스탠딩 849 L")
                .code("RF85T92N1AP")
                .imageUrl("/images/fridge.png")
                .category("REFRIGERATOR")
                .build();

        productRepository.save(bespokeFridge);
        System.out.println("✅ 테스트용 Product 생성 완료 (ID: " + bespokeFridge.getId() + ")");
    }
}