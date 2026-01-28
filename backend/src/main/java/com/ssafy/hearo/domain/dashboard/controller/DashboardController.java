package com.ssafy.hearo.domain.dashboard.controller;

import com.ssafy.hearo.domain.dashboard.dto.DashboardSummaryResponse;
import com.ssafy.hearo.domain.dashboard.service.DashboardService;
import com.ssafy.hearo.global.common.response.BaseResponse; // 공통 응답 객체 가정
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public BaseResponse<DashboardSummaryResponse> getDashboardSummary(
            @AuthenticationPrincipal UserDetails userDetails // 스프링 시큐리티 사용 가정
    ) {
        // UserDetails에서 ID 추출하는 로직은 프로젝트 설정에 따라 다를 수 있음
        Long userId = Long.parseLong(userDetails.getUsername()); 
        
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(userId);
        return BaseResponse.success(response);
    }
}