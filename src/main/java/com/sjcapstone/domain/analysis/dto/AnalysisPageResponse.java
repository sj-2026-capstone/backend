package com.sjcapstone.domain.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AnalysisPageResponse {

    private List<AnalysisListItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static AnalysisPageResponse from(Page<AnalysisListItemResponse> page) {
        return AnalysisPageResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}