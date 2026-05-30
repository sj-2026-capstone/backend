package com.sjcapstone.domain.dashboard.dto;

import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import com.sjcapstone.domain.analysis.entity.SeverityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LatestAnalysisSummaryResponse {

    private Long analysisId;
    private AnalysisStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalInspectionCount;
    private int totalDefectCount;
    private int patternCount;
    private SeverityLevel highestSeverity;
    private LocalDateTime analyzedAt;

    public static LatestAnalysisSummaryResponse from(ProcessAnalysis analysis, int patternCount, SeverityLevel highestSeverity) {
        return LatestAnalysisSummaryResponse.builder()
                .analysisId(analysis.getId())
                .status(analysis.getStatus())
                .fromDate(analysis.getFromDate())
                .toDate(analysis.getToDate())
                .totalInspectionCount(analysis.getTotalInspectionCount())
                .totalDefectCount(analysis.getTotalDefectCount())
                .patternCount(patternCount)
                .highestSeverity(highestSeverity)
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }

    public static LatestAnalysisSummaryResponse none() {
        return LatestAnalysisSummaryResponse.builder()
                .analysisId(null)
                .status(null)
                .build();
    }
}