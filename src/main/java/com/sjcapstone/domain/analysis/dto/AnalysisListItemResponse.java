package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AnalysisListItemResponse {

    private Long analysisId;
    private AnalysisStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime analyzedAt;

    public static AnalysisListItemResponse from(ProcessAnalysis analysis) {
        return AnalysisListItemResponse.builder()
                .analysisId(analysis.getId())
                .status(analysis.getStatus())
                .requestedAt(analysis.getCreatedAt())
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
}