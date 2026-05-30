package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AnalysisMetadataDto {

    private LocalDateTime analysisBaseTime;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalInspectionCount;
    private int totalDefectCount;
    private String modelName;
    private Boolean ragUsed;

    public static AnalysisMetadataDto from(ProcessAnalysis analysis) {
        return AnalysisMetadataDto.builder()
                .analysisBaseTime(analysis.getAnalyzedAt())
                .fromDate(analysis.getFromDate())
                .toDate(analysis.getToDate())
                .totalInspectionCount(analysis.getTotalInspectionCount())
                .totalDefectCount(analysis.getTotalDefectCount())
                .modelName(analysis.getModelName())
                .ragUsed(analysis.getRagUsed())
                .build();
    }
}