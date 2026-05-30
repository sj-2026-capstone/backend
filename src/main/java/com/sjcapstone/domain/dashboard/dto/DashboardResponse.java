package com.sjcapstone.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashboardResponse {
    private DashboardSummaryResponse summary;
    private List<DefectRateTrendItemResponse> defectRateTrend;
    private ActionSummaryResponse actionSummary;
    private List<LineDefectRateResponse> lineDefectRates;
    private LatestAnalysisSummaryResponse latestAnalysis;
    private LocalDateTime lastUpdatedAt;
}