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

import com.ssafy.hearo.domain.user.dto.EnergyHistoryResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
     * 1-2. 로그아웃 시: REST로 전환 + 히스토리 저장
     * - 로그아웃은 "앱 사용 종료"이지만, 정책상 상태는 OFFLINE이 아니라 REST로 둔다.
     * - 히스토리는 "로그아웃 이벤트로 인해 REST 전환"을 남긴다.
     */
    public void switchToRestOnLogout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        LocalDateTime now = LocalDateTime.now();

        // 변경 직전 실시간 에너지 계산
        int currentEnergy = user.calculateRealTimeEnergy(now);
        int previousEnergy = user.getLastEnergyValue();

        // 당시 상태(변경 전 상태)로 히스토리 기록
        saveHistory(user, previousEnergy, currentEnergy, user.getStatus(), "로그아웃: 휴식(REST) 전환");

        // REST로 전환 (이 시점 에너지를 앵커로 고정)
        user.updateEnergyAnchor(UserStatus.REST, currentEnergy);
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

    /**
     * [조회] 특정 기간 동안의 에너지 히스토리 가져오기
     * (startDate가 null이면 '오늘'을 기본값으로 사용)
     */
    @Transactional(readOnly = true)
    public List<EnergyHistoryResponseDto> getEnergyHistory(Long userId, LocalDate date) {
        // 날짜가 안 들어오면 '오늘'로 설정
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 해당 날짜의 00:00:00 ~ 23:59:59 설정
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59);

        // 조회 및 DTO 변환
        return energyHistoryRepository.findAllByUserIdAndCreatedAtBetween(userId, start, end)
                .stream()
                .map(EnergyHistoryResponseDto::from)
                .collect(Collectors.toList());
    }
}