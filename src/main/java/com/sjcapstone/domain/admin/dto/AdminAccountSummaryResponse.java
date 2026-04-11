package com.sjcapstone.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminAccountSummaryResponse {

    private long totalCount;
    private long activeCount;
    private long inactiveCount;
    private long pendingCount;
}
