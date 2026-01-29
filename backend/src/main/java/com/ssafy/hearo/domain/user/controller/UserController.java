package com.ssafy.hearo.domain.user.controller;

import com.ssafy.hearo.domain.user.dto.EnergyEventRequestDto;
import com.ssafy.hearo.domain.user.dto.HeartbeatRequest;
import com.ssafy.hearo.domain.user.dto.StatusChangeRequestDto;
import com.ssafy.hearo.domain.user.service.HeartbeatService;
import com.ssafy.hearo.domain.user.service.UserStateService;
import com.ssafy.hearo.global.common.response.BaseResponse;
import com.ssafy.hearo.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final HeartbeatService heartbeatService;

    /**
     * Set heartbeat status for the current counselor
     * POST /api/v1/users/me/heartbeat
     *
     * Request Body: { "isHeartbeatActive": true/false }
     * Response: 200 OK with message
     *
     * When isHeartbeatActive is true:
     *   - Stores counselor ID in Redis with 30-second TTL
     *   - Counselor will be included in matching candidates
     *
     * When isHeartbeatActive is false:
     *   - Removes counselor ID from Redis
     *   - Counselor will be excluded from matching
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<HeartbeatResponse> setHeartbeat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody HeartbeatRequest request
    ) {
        Long userId = userDetails.getId();
        boolean isActive = request.isHeartbeatActive();

        heartbeatService.setHeartbeat(userId, isActive);

        String message = isActive
                ? "하트비트가 활성화되었습니다. 30초 후 자동 만료됩니다."
                : "하트비트가 비활성화되었습니다.";

        log.info("Heartbeat {} for userId={}", isActive ? "activated" : "deactivated", userId);

        return ResponseEntity.ok(new HeartbeatResponse(isActive, message));
    }

    public record HeartbeatResponse(boolean isHeartbeatActive, String message) {
    }

    private final UserStateService userStateService;

    /**
     * 1. 상태 변경 API
     */
    @PatchMapping("/status")
    public BaseResponse<Void> changeStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StatusChangeRequestDto request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        userStateService.changeStatus(userId, request.getStatus());

        // [변경] BaseResponse 사용
        // success(String message, T data) 메서드 활용 -> data 자리에 null을 넣으면 돼
        return BaseResponse.success("상태가 변경되었습니다.", null);
    }

    /**
     * 2. 이벤트 발생 API
     */
    @PostMapping("/status/event")
    public BaseResponse<Void> triggerEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EnergyEventRequestDto request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        userStateService.applyImmediateDamage(userId, request.getDamage());

        // [변경] BaseResponse 사용
        return BaseResponse.success("에너지 차감이 반영되었습니다.", null);
    }
}
