package com.sjcapstone.domain.line.dto;

import com.sjcapstone.domain.line.entity.LineCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LineStatusResponse {

    private Long lineId;
    private LineCode lineCode;
    private String lineName;
    private String lineStatus;
    private Long latestInspectionId;
    private LocalDateTime lastInspectedAt;
}