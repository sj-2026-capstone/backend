package com.sjcapstone.domain.line.service;

import com.sjcapstone.domain.line.dto.LineResponse;
import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.exception.LineNotFoundException;
import com.sjcapstone.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LineServiceImpl implements LineService {

    private final LineRepository lineRepository;

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
}
