package com.sjcapstone.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ActionSummaryResponse {
    private int total;
    private int pendingCount;
    private int inProgressCount;
    private int completedCount;
    private double completionRate;

    // TODO: DefectAction 또는 InspectionAction 도메인 추가 후 실제 집계로 교체
    public static ActionSummaryResponse empty() {
        return ActionSummaryResponse.builder()
                .total(0)
                .pendingCount(0)
                .inProgressCount(0)
                .completedCount(0)
                .completionRate(0.0)
                .build();
    }
}