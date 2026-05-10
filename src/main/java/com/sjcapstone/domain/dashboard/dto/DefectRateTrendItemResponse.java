package com.sjcapstone.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class DefectRateTrendItemResponse {
    private String date;
    private double defectRate;
    private long inspectionCount;
    private long defectCount;

    public static DefectRateTrendItemResponse of(LocalDate date, long inspectionCount, long defectCount) {
        double defectRate = inspectionCount == 0 ? 0.0
                : Math.round((double) defectCount / inspectionCount * 100 * 10) / 10.0;
        return DefectRateTrendItemResponse.builder()
                .date(date.toString())
                .defectRate(defectRate)
                .inspectionCount(inspectionCount)
                .defectCount(defectCount)
                .build();
    }
}