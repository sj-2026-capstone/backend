package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProcessAnalysisListItemResponse {

    private Long analysisId;
    private AnalysisStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalInspectionCount;
    private int totalDefectCount;
    private String modelName;
    private Boolean ragUsed;
    private LocalDateTime requestedAt;
    private LocalDateTime analyzedAt;

    public static ProcessAnalysisListItemResponse from(ProcessAnalysis analysis) {
        return ProcessAnalysisListItemResponse.builder()
                .analysisId(analysis.getId())
                .status(analysis.getStatus())
                .fromDate(analysis.getFromDate())
                .toDate(analysis.getToDate())
                .totalInspectionCount(analysis.getTotalInspectionCount())
                .totalDefectCount(analysis.getTotalDefectCount())
                .modelName(analysis.getModelName())
                .ragUsed(analysis.getRagUsed())
                .requestedAt(analysis.getCreatedAt())
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
}