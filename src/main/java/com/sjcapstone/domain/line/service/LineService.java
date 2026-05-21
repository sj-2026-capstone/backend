package com.sjcapstone.domain.line.service;

import com.sjcapstone.domain.line.dto.LineResponse;
import com.sjcapstone.domain.line.dto.LineStatusResponse;

import java.util.List;

public interface LineService {

    List<LineResponse> getLines();

    LineResponse getLine(Long lineId);

    List<LineStatusResponse> getLineStatuses();
}
