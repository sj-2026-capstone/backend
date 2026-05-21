package com.sjcapstone.domain.line.service;

import com.sjcapstone.domain.inspection.entity.ActionStatus;
import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import com.sjcapstone.domain.inspection.repository.InspectionRepository;
import com.sjcapstone.domain.line.dto.LineResponse;
import com.sjcapstone.domain.line.dto.LineStatusResponse;
import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.exception.LineNotFoundException;
import com.sjcapstone.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LineServiceImpl implements LineService {

    private final LineRepository lineRepository;
    private final InspectionRepository inspectionRepository;

    @Override
    public List<LineResponse> getLines() {
        return lineRepository.findAllByIsActiveTrueOrderByIdAsc()
                .stream()
                .map(LineResponse::from)
                .toList();
    }

    @Override
    public LineResponse getLine(Long lineId) {
        Line line = lineRepository.findByIdAndIsActiveTrue(lineId)
                .orElseThrow(LineNotFoundException::new);

        return LineResponse.from(line);
    }

    @Override
    public List<LineStatusResponse> getLineStatuses() {
        return lineRepository.findAllByIsActiveTrueOrderByIdAsc()
                .stream()
                .map(line -> {
                    boolean hasUnresolved = inspectionRepository.existsByLineIdAndHasDefectTrueAndActionStatus(
                            line.getId(), ActionStatus.UNRESOLVED);
                    Optional<Inspection> latest = inspectionRepository
                            .findTopByLineIdAndStatusOrderByInspectedAtDesc(line.getId(), InspectionStatus.DONE);
                    return LineStatusResponse.builder()
                            .lineId(line.getId())
                            .lineCode(line.getLineCode())
                            .lineName(line.getLineName())
                            .lineStatus(hasUnresolved ? "ALARM" : "NORMAL")
                            .latestInspectionId(latest.map(Inspection::getId).orElse(null))
                            .lastInspectedAt(latest.map(Inspection::getInspectedAt).orElse(null))
                            .build();
                })
                .toList();
    }
}
