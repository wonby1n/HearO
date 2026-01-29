package com.ssafy.hearo.domain.calls.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IssueTokenRequest {

    @NotBlank
    private String identity;

    @NotBlank
    private String roomName;
}
