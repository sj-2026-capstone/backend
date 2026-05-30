package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.SeverityLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPatternDto {

    private Integer patternId;
    private String title;
    private String description;
    private SeverityLevel severity;
    private String relatedLine;
    private String relatedTimeRange;
    private String metric;
    private String evidenceSummary;
}