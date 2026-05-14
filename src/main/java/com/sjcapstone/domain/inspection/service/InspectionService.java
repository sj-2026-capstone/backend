package com.sjcapstone.domain.inspection.service;

import com.sjcapstone.domain.inspection.dto.*;
import com.sjcapstone.domain.inspection.entity.DefectType;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import com.sjcapstone.domain.user.entity.UserRole;
import org.springframework.data.domain.Pageable;

public interface InspectionService {

    InspectionResponse createInspection(InspectionCreateRequest request);

    InspectionResponse createInspectionAndStartAnalysis(InspectionCreateRequest request);

    InspectionPageResponse getInspections(Long userId, UserRole role, Long lineId, InspectionStatus status, Pageable pageable);

    InspectionResponse getInspection(Long inspectionId, Long userId, UserRole role);

    InspectionStatusResponse getInspectionStatus(Long inspectionId, Long userId, UserRole role);

    InspectionResponse startAnalysis(Long inspectionId, Long userId, UserRole role);

    void processAnalysisCallback(Long inspectionId, boolean hasDefect, DefectType defectType, String resultNote, String gradCamImageUrl);
}
