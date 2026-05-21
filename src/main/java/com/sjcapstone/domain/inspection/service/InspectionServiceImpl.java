package com.sjcapstone.domain.inspection.service;

import com.sjcapstone.domain.inspection.dto.*;
import java.util.List;
import java.util.stream.Collectors;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
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
    private final PlatformTransactionManager transactionManager;

    @Override
    public InspectionResponse createInspection(InspectionCreateRequest request) {
        return InspectionResponse.from(inspectionRepository.save(createInspectionEntity(request)));
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InspectionResponse createInspectionAndStartAnalysis(InspectionCreateRequest request) {
        // DB 저장 후 즉시 커밋 → 커넥션 반환
        Long[] idRef = new Long[1];
        String[] imageUrlRef = new String[1];
        InspectionResponse[] responseRef = new InspectionResponse[1];

        new TransactionTemplate(transactionManager).execute(status -> {
            Inspection inspection = inspectionRepository.save(createInspectionEntity(request));
            inspection.startProcessing();
            idRef[0] = inspection.getId();
            imageUrlRef[0] = inspection.getImageUrl();
            responseRef[0] = InspectionResponse.from(inspection);
            return null;
        });

        // AI 호출은 커넥션 반환 후 수행
        try {
            aiAnalysisClient.requestAnalysis(idRef[0], imageUrlRef[0]);
        } catch (Exception e) {
            log.warn("AI 분석 요청 실패 — inspectionId={}, error={}", idRef[0], e.getMessage());
        }

        return responseRef[0];
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InspectionResponse startAnalysis(Long inspectionId, Long userId, UserRole role) {
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String[] imageUrlRef = new String[1];
        InspectionResponse[] responseRef = new InspectionResponse[1];

        new TransactionTemplate(transactionManager).execute(status -> {
            Inspection inspection = inspectionRepository.findById(inspectionId)
                    .orElseThrow(InspectionNotFoundException::new);
            if (inspection.getStatus() != InspectionStatus.PENDING) {
                throw new InvalidInspectionStatusException();
            }
            inspection.startProcessing();
            imageUrlRef[0] = inspection.getImageUrl();
            responseRef[0] = InspectionResponse.from(inspection);
            return null;
        });

        try {
            aiAnalysisClient.requestAnalysis(inspectionId, imageUrlRef[0]);
        } catch (Exception e) {
            log.warn("AI 분석 요청 실패 — inspectionId={}, error={}", inspectionId, e.getMessage());
        }

        return responseRef[0];
    }

    @Override
    public void processAnalysisCallback(Long inspectionId, boolean hasDefect, String gradCamImageUrl) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);

        if (inspection.getStatus() != InspectionStatus.PROCESSING) {
            throw new InvalidInspectionStatusException();
        }

        inspection.complete(hasDefect, gradCamImageUrl);

        if (hasDefect) {
            String workerName = inspection.getWorker() != null ? inspection.getWorker().getUserName() : null;
            notificationService.sendDefectDetected(
                    inspection.getLine().getLineName(),
                    "불량 감지",
                    workerName
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentDefectResponse> getRecentDefects(Long userId, UserRole role) {
        if (role == UserRole.WORKER) {
            User worker = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(UserNotFoundException::new);
            Long workerLineId = worker.getLine() != null ? worker.getLine().getId() : null;
            if (workerLineId == null) {
                return List.of();
            }
            return inspectionRepository
                    .findTop5ByLineIdAndHasDefectTrueAndStatusOrderByInspectedAtDesc(workerLineId, InspectionStatus.DONE)
                    .stream()
                    .map(RecentDefectResponse::from)
                    .collect(Collectors.toList());
        }
        return inspectionRepository
                .findTop5ByHasDefectTrueAndStatusOrderByInspectedAtDesc(InspectionStatus.DONE)
                .stream()
                .map(RecentDefectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void resolveAction(Long inspectionId, Long userId, UserRole role) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(InspectionNotFoundException::new);

        if (role == UserRole.WORKER) {
            validateWorkerLineAccess(userId, inspection);
        }

        inspection.resolveAction();
        inspectionRepository.save(inspection);
    }

    private void validateWorkerLineAccess(Long userId, Inspection inspection) {
        User worker = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);
        if (worker.getLine() == null || !inspection.getLine().getId().equals(worker.getLine().getId())) {
            throw new InspectionNotFoundException();
        }
    }
}
