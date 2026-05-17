package com.sjcapstone.domain.analysis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProcessAnalysisCallbackRequest {

    @NotNull(message = "패턴 목록은 필수입니다.")
    private List<PatternDto> patterns;

    @NotNull(message = "추천 조치 목록은 필수입니다.")
    private List<RecommendationDto> recommendations;
}