package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AnalysisStartResponse {

    private Long analysisId;
    private AnalysisStatus status;

    public static AnalysisStartResponse from(ProcessAnalysis analysis) {
        return AnalysisStartResponse.builder()
                .analysisId(analysis.getId())
                .status(analysis.getStatus())
                .build();
    }
}