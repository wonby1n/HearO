package com.ssafy.hearo.domain.ai.controller;

import com.ssafy.hearo.domain.ai.dto.AskRequest;
import com.ssafy.hearo.domain.ai.dto.AskResponse;
import com.ssafy.hearo.domain.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest req) {
        return ragService.ask(req.getQuestion());
    }
}
