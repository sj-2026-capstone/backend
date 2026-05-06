package com.sjcapstone.domain.inspection.dto;

import com.sjcapstone.domain.inspection.entity.DefectType;
import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InspectionResponse {

    private Long inspectionId;
    private String lineCode;
    private String lineName;
    private String shiftName;
    private String workerName;
    private InspectionStatus status;
    private DefectType defectType;
    private String defectDisplayName;
    private Boolean hasDefect;
    private String imageUrl;
    private String resultNote;
    private LocalDateTime inspectedAt;
    private LocalDateTime createdAt;

    public static InspectionResponse from(Inspection inspection) {
        return InspectionResponse.builder()
                .inspectionId(inspection.getId())
                .lineCode(inspection.getLine().getLineCode().name())
                .lineName(inspection.getLine().getLineName())
                .shiftName(inspection.getShift() != null ? inspection.getShift().getShiftName() : null)
                .workerName(inspection.getWorker() != null ? inspection.getWorker().getUserName() : null)
                .status(inspection.getStatus())
                .defectType(inspection.getDefectType())
                .defectDisplayName(inspection.getDefectType() != null ? inspection.getDefectType().getDisplayName() : null)
                .hasDefect(inspection.getHasDefect())
                .imageUrl(inspection.getImageUrl())
                .resultNote(inspection.getResultNote())
                .inspectedAt(inspection.getInspectedAt())
                .createdAt(inspection.getCreatedAt())
                .build();
    }
}