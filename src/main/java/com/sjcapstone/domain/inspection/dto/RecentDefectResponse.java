package com.sjcapstone.domain.inspection.dto;

import com.sjcapstone.domain.inspection.entity.Inspection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RecentDefectResponse {

    private Long inspectionId;
    private String lineName;
    private String imageUrl;
    private String gradCamImageUrl;
    private LocalDateTime inspectedAt;

    public static RecentDefectResponse from(Inspection inspection) {
        return RecentDefectResponse.builder()
                .inspectionId(inspection.getId())
                .lineName(inspection.getLine().getLineName())
                .imageUrl(inspection.getImageUrl())
                .gradCamImageUrl(inspection.getGradCamImageUrl())
                .inspectedAt(inspection.getInspectedAt())
                .build();
    }
}