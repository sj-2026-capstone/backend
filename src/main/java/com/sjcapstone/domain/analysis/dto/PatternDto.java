package com.sjcapstone.domain.analysis.dto;

import com.sjcapstone.domain.analysis.entity.SeverityLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatternDto {

    private String title;
    private String description;
    private SeverityLevel severity;
}