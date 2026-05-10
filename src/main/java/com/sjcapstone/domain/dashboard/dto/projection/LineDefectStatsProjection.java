package com.sjcapstone.domain.dashboard.dto.projection;

public interface LineDefectStatsProjection {
    Long getLineId();
    Long getInspectionCount();
    Long getDefectCount();
}