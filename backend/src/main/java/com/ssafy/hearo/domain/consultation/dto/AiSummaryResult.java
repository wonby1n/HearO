package com.ssafy.hearo.domain.consultation.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummaryResult {
    private String title;
    private String summary;
}
