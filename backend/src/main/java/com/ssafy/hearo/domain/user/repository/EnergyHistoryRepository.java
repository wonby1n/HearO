package com.ssafy.hearo.domain.user.repository;

import com.ssafy.hearo.domain.user.entity.EnergyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EnergyHistoryRepository extends JpaRepository<EnergyHistory, Long> {

    // 특정 유저의 모든 히스토리 조회
    List<EnergyHistory> findAllByUserId(Long userId);

    // [그래프용] 특정 기간 동안의 히스토리 조회
    // 예: 오늘 하루 동안의 변화 내역을 가져와서 꺾은선 그래프 그릴 때 사용
    List<EnergyHistory> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}