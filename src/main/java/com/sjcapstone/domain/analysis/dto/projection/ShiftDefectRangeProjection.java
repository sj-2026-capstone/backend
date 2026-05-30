package com.sjcapstone.domain.analysis.dto.projection;

public interface ShiftDefectRangeProjection {
    String getShiftName();
    String getStartTime();
    String getEndTime();
    long getInspectionCount();
    long getDefectCount();
}