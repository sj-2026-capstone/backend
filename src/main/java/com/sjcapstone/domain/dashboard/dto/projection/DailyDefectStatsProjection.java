package com.sjcapstone.domain.dashboard.dto.projection;

import java.sql.Date;

public interface DailyDefectStatsProjection {
    Date getDate();
    Long getInspectionCount();
    Long getDefectCount();
}
