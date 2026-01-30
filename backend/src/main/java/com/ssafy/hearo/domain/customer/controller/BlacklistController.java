package com.ssafy.hearo.domain.customer.controller;

import com.ssafy.hearo.domain.customer.dto.BlacklistRequest;
import com.ssafy.hearo.domain.customer.dto.BlacklistResponse;
import com.ssafy.hearo.domain.customer.service.BlacklistService;
import com.ssafy.hearo.global.common.response.BaseResponse;
import com.ssafy.hearo.global.util.MockUserIdExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blacklists")
@RequiredArgsConstructor
@Slf4j
public class BlacklistController {

    private final BlacklistService blacklistService;
    private final MockUserIdExtractor userIdExtractor;

    /**
     * 블랙리스트 등록
     * POST /api/v1/blacklists
     */
    @PostMapping
    public ResponseEntity<BaseResponse<Long>> addBlacklist(
            @Valid @RequestBody BlacklistRequest request,
            HttpServletRequest httpRequest) {

        Long userId = Long.parseLong(userIdExtractor.extract(httpRequest)); // 상담원 ID 추출
        Long blacklistId = blacklistService.addBlacklist(userId, request);

        return ResponseEntity.ok(BaseResponse.success("블랙리스트에 등록되었습니다.", blacklistId));
    }

    /**
     * 내 블랙리스트 조회
     * GET /api/v1/blacklists
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<BlacklistResponse>>> getMyBlacklists(
            HttpServletRequest httpRequest) {

        Long userId = Long.parseLong(userIdExtractor.extract(httpRequest));
        List<BlacklistResponse> responses = blacklistService.getMyBlacklists(userId);

        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    /**
     * 블랙리스트 삭제 (해제)
     * DELETE /api/v1/blacklists/{blacklistId}
     */
    @DeleteMapping("/{blacklistId}")
    public ResponseEntity<BaseResponse<Void>> deleteBlacklist(
            @PathVariable("blacklistId") Long blacklistId,
            HttpServletRequest httpRequest) {

        Long userId = Long.parseLong(userIdExtractor.extract(httpRequest));
        blacklistService.deleteBlacklist(userId, blacklistId);

        return ResponseEntity.ok(BaseResponse.success("블랙리스트가 해제되었습니다.", null));
    }
}