package com.sjcapstone.domain.dashboard.dto;

import com.sjcapstone.domain.line.entity.Line;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LineDefectRateResponse {
    private Long lineId;
    private String lineCode;
    private String lineName;
    private double defectRate;
    private long inspectionCount;
    private long defectCount;

    public static LineDefectRateResponse of(Line line, long inspectionCount, long defectCount) {
        double defectRate = inspectionCount == 0 ? 0.0
                : Math.round((double) defectCount / inspectionCount * 100 * 10) / 10.0;
        return LineDefectRateResponse.builder()
                .lineId(line.getId())
                .lineCode(line.getLineCode().name())
                .lineName(line.getLineName())
                .defectRate(defectRate)
                .inspectionCount(inspectionCount)
                .defectCount(defectCount)
                .build();
    }
}