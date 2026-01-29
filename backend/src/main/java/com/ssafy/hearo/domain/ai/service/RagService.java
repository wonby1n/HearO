package com.ssafy.hearo.domain.ai.service;

import com.ssafy.hearo.domain.ai.dto.AskResponse;

public interface RagService {
    AskResponse ask(String question);
}
