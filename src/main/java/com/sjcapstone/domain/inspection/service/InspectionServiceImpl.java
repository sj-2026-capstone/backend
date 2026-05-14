package com.sjcapstone.domain.inspection.service;

import com.sjcapstone.domain.inspection.dto.*;
import com.sjcapstone.domain.inspection.entity.DefectType;
import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import com.sjcapstone.domain.inspection.exception.InspectionNotFoundException;
import com.sjcapstone.domain.inspection.exception.InvalidInspectionStatusException;
import com.sjcapstone.domain.inspection.repository.InspectionRepository;
import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.exception.LineNotFoundException;
import com.sjcapstone.domain.line.repository.LineRepository;
import com.sjcapstone.domain.notification.service.NotificationService;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.repository.ShiftRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.repository.UserRepository;
import com.sjcapstone.global.client.AiAnalysisClient;
import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InspectionServiceImpl implements InspectionService {

    private final InspectionRepository inspectionRepository;
    private final LineRepository lineRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AiAnalysisClient aiAnalysisClient;

    @Override
    public InspectionResponse createInspection(InspectionCreateRequest request) {
        return InspectionResponse.from(inspectionRepository.save(createInspectionEntity(request)));
    }

    @Override
    public InspectionResponse createInspectionAndStartAnalysis(InspectionCreateRequest request) {
        Inspection inspection = inspectionRepository.save(createInspectionEntity(request));

        inspection.startProcessing();
        try {
            aiAnalysisClient.requestAnalysis(inspection.getId(), inspection.getImageUrl());
        } catch (Exception e) {
            inspection.fail("AI 분석 요청 실패: " + e.getMessage());
        }

        return InspectionResponse.from(inspection);
    }

    private Inspection createInspectionEntity(InspectionCreateRequest request) {
        Line line = lineRepository.findByIdAndIsActiveTrue(request.getLineId())
                .orElseThrow(LineNotFoundException::new);

        Shift shift = null;
        if (request.getShiftId() != null) {
            shift = shiftRepository.findById(request.getShiftId()).orElse(null);
        }

        User worker = null;
        if (request.getWorkerId() != null) {
            worker = userRepository.findByIdAndDeletedAtIsNull(request.getWorkerId())
                    .orElseThrow(UserNotFoundException::new);
            if (shift == null && worker.getShift() != null) {
                shift = worker.getShift();
            }
        }

        Inspection inspection = Inspection.builder()
                .line(line)
                .shift(shift)
                .worker(worker)
                .imageUrl(request.getImageUrl())
                .build();

        return inspection;
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionPageResponse getInspections(Long userId, UserRole role, Long lineIdFilter, InspectionStatus statusFilter, Pageable pageable) {
        Page<Inspection> page;

        if (role == UserRole.WORKER) {
            User worker = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(UserNotFoundException::new);
            Long workerLineId = worker.getLine() != null ? worker.getLine().getId() : null;

            if (workerLineId == null) {
                return InspectionPageResponse.empty(pageable);
            }

            if (statusFilter != null) {
                page = inspectionRepository.findAllByLineIdAndStatusOrderByCreatedAtDesc(workerLineId, statusFilter, pageable);
            } else {
                page = inspectionRepository.findAllByLineIdOrderByCreatedAtDesc(workerLineId, pageable);
            }
        } else {
            if (lineIdFilter != null && statusFilter != null) {
                page = inspectionRepository.findAllByLineIdAndStatusOrderByCreatedAtDesc(lineIdFilter, statusFilter, pageable);
            } else if (lineIdFilter != null) {
                page = inspectionRepository.findAllByLineIdOrderByCreatedAtDesc(lineIdFilter, pageable);
            } else if (statusFilter != null) {
                page = inspectionRepository.findAllByStatusOrderByCreatedAtDesc(statusFilter, pageable);
            } else {
                page = inspectionRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        return InspectionPageResponse.from(page.map(InspectionListItemResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionResponse getInspection(Long inspectionId, Long userId, UserRole role) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);

        if (role == UserRole.WORKER) {
            validateWorkerLineAccess(userId, inspection);
        }

        return InspectionResponse.from(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionStatusResponse getInspectionStatus(Long inspectionId, Long userId, UserRole role) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);

        if (role == UserRole.WORKER) {
            validateWorkerLineAccess(userId, inspection);
        }

        return InspectionStatusResponse.from(inspection);
    }

    @Override
    public InspectionResponse startAnalysis(Long inspectionId, Long userId, UserRole role) {
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);

        if (inspection.getStatus() != InspectionStatus.PENDING) {
            throw new InvalidInspectionStatusException();
        }

        inspection.startProcessing();
        try {
            aiAnalysisClient.requestAnalysis(inspection.getId(), inspection.getImageUrl());
        } catch (Exception e) {
            inspection.fail("AI 분석 요청 실패: " + e.getMessage());
        }
        return InspectionResponse.from(inspection);
    }

    @Override
    public void processAnalysisCallback(Long inspectionId, boolean hasDefect, DefectType defectType, String resultNote, String gradCamImageUrl) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);

        if (inspection.getStatus() != InspectionStatus.PROCESSING) {
            throw new InvalidInspectionStatusException();
        }

        inspection.complete(hasDefect, defectType, resultNote, gradCamImageUrl);

        if (hasDefect) {
            String defectDisplayName = defectType != null ? defectType.getDisplayName() : "알 수 없음";
            String workerName = inspection.getWorker() != null ? inspection.getWorker().getUserName() : null;
            notificationService.sendDefectDetected(
                    inspection.getLine().getLineName(),
                    defectDisplayName,
                    workerName
            );
        }
    }

    private void validateWorkerLineAccess(Long userId, Inspection inspection) {
        User worker = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);
        if (worker.getLine() == null || !inspection.getLine().getId().equals(worker.getLine().getId())) {
            throw new InspectionNotFoundException();
        }
    }
}
