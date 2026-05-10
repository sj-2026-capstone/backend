package com.sjcapstone.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalInspectionCount;
    private double totalInspectionChangeRate;
    private double defectRate;
    private double defectRateChange;
    private long todayInspectionCount;
    private long todayDefectCount;
}