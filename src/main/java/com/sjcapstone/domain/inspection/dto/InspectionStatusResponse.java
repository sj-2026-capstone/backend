package com.sjcapstone.domain.inspection.dto;

import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InspectionStatusResponse {

    private Long inspectionId;
    private InspectionStatus status;

    public static InspectionStatusResponse from(Inspection inspection) {
        return InspectionStatusResponse.builder()
                .inspectionId(inspection.getId())
                .status(inspection.getStatus())
                .build();
    }
}