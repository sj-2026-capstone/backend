package com.sjcapstone.domain.dashboard.dto.projection;

import java.time.LocalDate;

public interface DailyDefectStatsProjection {
    LocalDate getDate();
    Long getInspectionCount();
    Long getDefectCount();
}
