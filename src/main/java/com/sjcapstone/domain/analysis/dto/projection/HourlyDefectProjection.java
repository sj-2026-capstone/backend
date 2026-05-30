package com.sjcapstone.domain.analysis.dto.projection;

public interface HourlyDefectProjection {
    int getHour();
    long getInspectionCount();
    long getDefectCount();
}