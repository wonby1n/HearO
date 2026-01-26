package com.ssafy.hearo.domain.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth") // Nginx에서 /api를 떼고 넘겨주므로 /auth만 적으면 돼
public class AuthController {

    @GetMapping("/test")
    public Map<String, String> authTest() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Hearo 백엔드 연결 성공! 인증 도메인 정상 작동 중입니다.");
        return response;
    }
}