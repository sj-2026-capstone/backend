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
public class ProcessAnalysisResponse {

    private Long analysisId;
    private AnalysisStatus status;
    private List<ProcessPatternDto> patterns;
    private List<ProcessRecommendationDto> recommendations;
    private AnalysisMetadataDto metadata;
    private LocalDateTime requestedAt;
    private LocalDateTime analyzedAt;
    private String errorMessage;

    public static ProcessAnalysisResponse from(ProcessAnalysis analysis,
                                               List<ProcessPatternDto> patterns,
                                               List<ProcessRecommendationDto> recommendations) {
        return ProcessAnalysisResponse.builder()
                .analysisId(analysis.getId())
                .status(analysis.getStatus())
                .patterns(patterns)
                .recommendations(recommendations)
                .metadata(AnalysisMetadataDto.from(analysis))
                .requestedAt(analysis.getCreatedAt())
                .analyzedAt(analysis.getAnalyzedAt())
                .errorMessage(analysis.getErrorMessage())
                .build();
    }
}