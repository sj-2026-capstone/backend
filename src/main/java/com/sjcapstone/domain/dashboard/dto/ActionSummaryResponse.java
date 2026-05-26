package com.sjcapstone.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ActionSummaryResponse {
    private long total;
    private long unresolvedCount;
    private long resolvedCount;
    private double completionRate;
}