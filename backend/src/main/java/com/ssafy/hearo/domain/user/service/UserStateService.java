package com.ssafy.hearo.domain.user.service;

import com.ssafy.hearo.domain.user.entity.UserStatus;
import com.ssafy.hearo.domain.user.entity.EnergyHistory;
import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.repository.EnergyHistoryRepository;
import com.ssafy.hearo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserStateService {

    private final UserRepository userRepository;
    private final EnergyHistoryRepository energyHistoryRepository;

    /**
     * 1. 상담원 상태 변경 (예: 대기 -> 통화, 통화 -> 휴식)
     */
    public void changeStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 1-1. 현재(변경 직전) 에너지 계산 (이게 히스토리에 저장될 '결과값'이자 다음 상태의 '시작값')
        int currentEnergy = user.calculateRealTimeEnergy(LocalDateTime.now());
        int previousEnergy = user.getLastEnergyValue(); // 변경 전 기준값 (참고용)

        // 1-2. 히스토리 저장 (로그 남기기)
        saveHistory(user, previousEnergy, currentEnergy, user.getStatus(), "상태 변경: " + newStatus.getDescription());

        // 1-3. User 엔티티 앵커 업데이트 (이제부터 newStatus로 계산 시작)
        user.updateEnergyAnchor(newStatus, currentEnergy);
    }

    /**
     * 2. 욕설 감지 등 즉시 차감 이벤트
     */
    public void applyImmediateDamage(Long userId, int damageAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 2-1. 현재 에너지 계산
        int currentEnergyBeforeDamage = user.calculateRealTimeEnergy(LocalDateTime.now());
        
        // 2-2. 데미지 적용
        int finalEnergy = Math.max(0, currentEnergyBeforeDamage - damageAmount);

        // 2-3. 히스토리 저장
        saveHistory(user, currentEnergyBeforeDamage, finalEnergy, user.getStatus(), "이벤트: 욕설 감지(-" + damageAmount + ")");

        // 2-4. User 엔티티 업데이트 (상태는 유지하되, 에너지 값과 기준 시간만 갱신)
        user.decreaseEnergyImmediately(damageAmount);
    }

    // 히스토리 저장 헬퍼 메서드
    private void saveHistory(User user, int prev, int curr, UserStatus status, String reason) {
        EnergyHistory history = EnergyHistory.builder()
                .user(user)
                .previousEnergy(prev)
                .currentEnergy(curr)
                .status(status)
                .reason(reason)
                .build();
        energyHistoryRepository.save(history);
    }
}