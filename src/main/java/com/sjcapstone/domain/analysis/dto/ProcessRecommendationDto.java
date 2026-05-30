package com.sjcapstone.domain.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRecommendationDto {

    private Integer priority;
    private String title;
    private String description;
    private String targetLine;
    private String expectedEffect;
    private List<Integer> relatedPatternIds;
}