package com.sjcapstone.domain.analysis.dto.projection;

public interface LineDefectRangeProjection {
    Long getLineId();
    String getLineName();
    long getInspectionCount();
    long getDefectCount();
}