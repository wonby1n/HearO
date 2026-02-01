package com.ssafy.hearo.global.init;

import com.ssafy.hearo.domain.consultation.entity.Consultation;
import com.ssafy.hearo.domain.consultation.repository.ConsultationRepository;
import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.product.entity.Product;
import com.ssafy.hearo.domain.product.repository.ProductRepository;
import com.ssafy.hearo.domain.registration.entity.Registration;
import com.ssafy.hearo.domain.registration.repository.RegistrationRepository;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final RegistrationRepository registrationRepository;
    private final ConsultationRepository consultationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 기초 데이터 생성
        initTestUser();
        initTestCustomer();
        initTestProduct();

        // 2. 파생 데이터 생성 (순서 중요! Product, Customer가 있어야 Registration 생성 가능)
        initTestRegistrationAndConsultation();
    }

    private void initTestUser() {
        if (userRepository.findByEmail("test@ssafy.com").isPresent()) return;

        User testUser = User.builder()
                .email("test@ssafy.com")
                .password(passwordEncoder.encode("1234"))
                .name("김싸피")
                .role(UserRole.USER)
                .build();
        userRepository.save(testUser);
        System.out.println("✅ 테스트용 User 생성 완료: test@ssafy.com");
    }

    private void initTestCustomer() {
        if (customerRepository.findByPhone("010-1234-5678").isPresent()) return;

        Customer testCustomer = Customer.builder()
                .name("박고객")
                .phone("010-1234-5678")
                .build();
        customerRepository.save(testCustomer);
        System.out.println("✅ 테스트용 Customer 생성 완료: 박고객");
    }

    private void initTestProduct() {
        if (productRepository.count() > 0) return;

        Product bespokeFridge = Product.builder()
                .name("BESPOKE 냉장고 4도어 프리스탠딩 849 L")
                .code("RF85T92N1AP")
                .imageUrl("/images/fridge.png")
                .category("REFRIGERATOR")
                .build();
        productRepository.save(bespokeFridge);
        System.out.println("✅ 테스트용 Product 생성 완료: BESPOKE 냉장고");
    }

    // [수정됨] Registration 엔티티 필드 반영
    private void initTestRegistrationAndConsultation() {
        // 기존 데이터 조회
        User user = userRepository.findByEmail("test@ssafy.com").orElseThrow();
        Customer customer = customerRepository.findByPhone("010-1234-5678").orElseThrow();
        Product product = productRepository.findAll().get(0);

        // 1. Registration (제품 등록) 생성
        // 이미 등록된 내역이 있다면 생성하지 않음 (중복 방지)
        if (registrationRepository.count() > 0) return;

        Registration registration = Registration.builder()
                .customer(customer)
                .product(product)
                .symptom("소음이 심하고 가끔 전원이 꺼짐") // 증상 추가
                .errorCode("E-101") // 에러코드 예시
                .manufacturedAt(LocalDate.of(2023, 1, 15)) // 제조일자: 23년 1월
                .warrantyEndsAt(LocalDate.of(2026, 1, 14)) // 보증만료: 3년 뒤
                .build();

        registrationRepository.save(registration);
        System.out.println("✅ 테스트용 Registration 생성 완료 (ID: " + registration.getId() + ")");

        // 2. Consultation (상담) 생성
        if (consultationRepository.count() > 0) return;

        Consultation consultation = Consultation.builder()
                .user(user)                     // 상담사
                .customer(customer)             // 고객
                .registration(registration)     // 등록된 제품 정보 연결
                .title("냉장고 소음 문의")
                .subtitle("새벽에 웅웅거리는 소리가 심해요")
                .build();

        // 텍스트/메모 등 추가 정보 세팅
        consultation.appendTranscript("상담원: 안녕하세요, 무엇을 도와드릴까요?");
        consultation.appendTranscript("고객: 냉장고 산 지 좀 됐는데 요즘 소리가 너무 커요.");
        consultation.updateUserMemo("고객님 매우 예민한 상태. 빠른 AS 접수 필요.");

        consultationRepository.save(consultation);

        System.out.println("=========================================");
        System.out.println("🎉 테스트 데이터 세팅 완료!");
        System.out.println("👉 상담 ID (consultationId): " + consultation.getId());
        System.out.println("=========================================");
    }
}