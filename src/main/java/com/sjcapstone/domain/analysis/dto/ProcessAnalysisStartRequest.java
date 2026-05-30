package com.sjcapstone.domain.analysis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ProcessAnalysisStartRequest {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Long lineId;
}