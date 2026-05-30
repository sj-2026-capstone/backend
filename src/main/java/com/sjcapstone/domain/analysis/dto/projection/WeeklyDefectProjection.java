package com.sjcapstone.domain.analysis.dto.projection;

public interface WeeklyDefectProjection {
    String getWeek();
    long getInspectionCount();
    long getDefectCount();
}