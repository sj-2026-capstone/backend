package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AnalysisResponse {

    private Long analysisId;
    private AnalysisStatus status;
    private List<PatternDto> patterns;
    private List<RecommendationDto> recommendations;
    private LocalDateTime requestedAt;
    private LocalDateTime analyzedAt;
    private String errorMessage;

    public static AnalysisResponse from(ProcessAnalysis analysis,
                                        List<PatternDto> patterns,
                                        List<RecommendationDto> recommendations) {
        return AnalysisResponse.builder()
                .analysisId(analysis.getId())
                .status(analysis.getStatus())
                .patterns(patterns)
                .recommendations(recommendations)
                .requestedAt(analysis.getCreatedAt())
                .analyzedAt(analysis.getAnalyzedAt())
                .errorMessage(analysis.getErrorMessage())
                .build();
    }
}