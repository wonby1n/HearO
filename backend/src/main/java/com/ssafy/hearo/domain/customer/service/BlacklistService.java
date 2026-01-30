package com.ssafy.hearo.domain.customer.service;

import com.ssafy.hearo.domain.customer.dto.BlacklistRequest;
import com.ssafy.hearo.domain.customer.dto.BlacklistResponse;
import com.ssafy.hearo.domain.customer.entity.Blacklist;
import com.ssafy.hearo.domain.customer.entity.Customer;
import com.ssafy.hearo.domain.customer.repository.BlacklistRepository;
import com.ssafy.hearo.domain.customer.repository.CustomerRepository;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /**
     * 블랙리스트 추가
     */
    public Long addBlacklist(Long userId, BlacklistRequest request) {
        // 1. 이미 블랙리스트인지 확인
        if (blacklistRepository.existsByUserIdAndCustomerId(userId, request.getCustomerId())) {
            throw new IllegalArgumentException("이미 블랙리스트에 등록된 고객입니다.");
        }

        // 2. 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("상담원 정보를 찾을 수 없습니다."));
        
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다."));

        // 3. 저장
        Blacklist blacklist = Blacklist.builder()
                .user(user)
                .customer(customer)
                .reason(request.getReason())
                .build();

        return blacklistRepository.save(blacklist).getId();
    }

    /**
     * 내 블랙리스트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BlacklistResponse> getMyBlacklists(Long userId) {
        return blacklistRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BlacklistResponse::from)
                .toList();
    }

    /**
     * 블랙리스트 삭제 (해제)
     * - Blacklist PK(ID)로 삭제
     */
    public void deleteBlacklist(Long userId, Long blacklistId) {
        Blacklist blacklist = blacklistRepository.findById(blacklistId)
                .orElseThrow(() -> new EntityNotFoundException("해당 블랙리스트 내역을 찾을 수 없습니다."));

        // 본인이 등록한 건인지 검증
        if (!blacklist.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 등록한 블랙리스트만 삭제할 수 있습니다.");
        }

        blacklistRepository.delete(blacklist);
    }
}