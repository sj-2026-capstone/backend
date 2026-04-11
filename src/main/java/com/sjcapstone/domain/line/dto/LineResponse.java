package com.sjcapstone.domain.line.dto;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.entity.LineCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LineResponse {

    private Long lineId;
    private LineCode lineCode;
    private String lineName;
    private Boolean isActive;

    public static LineResponse from(Line line) {
        return LineResponse.builder()
                .lineId(line.getId())
                .lineCode(line.getLineCode())
                .lineName(line.getLineName())
                .isActive(line.getIsActive())
                .build();
    }
}
