package com.sjcapstone.domain.inspection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class InspectionPageResponse {

    private List<InspectionListItemResponse> inspections;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static InspectionPageResponse from(Page<InspectionListItemResponse> page) {
        return InspectionPageResponse.builder()
                .inspections(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    public static InspectionPageResponse empty(Pageable pageable) {
        return InspectionPageResponse.builder()
                .inspections(List.of())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(0L)
                .totalPages(0)
                .last(true)
                .build();
    }
}