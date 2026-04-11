package com.sjcapstone.domain.line.controller;

import com.sjcapstone.domain.line.dto.LineResponse;
import com.sjcapstone.domain.line.service.LineService;
import com.sjcapstone.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
public class LineController {

    private final LineService lineService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<LineResponse>>> getLines() {
        List<LineResponse> response = lineService.getLines();
        return ResponseEntity.ok(CommonResponse.ok("라인 목록 조회 성공", response));
    }

    @GetMapping("/{lineId}")
    public ResponseEntity<CommonResponse<LineResponse>> getLine(@PathVariable Long lineId) {
        LineResponse response = lineService.getLine(lineId);
        return ResponseEntity.ok(CommonResponse.ok("라인 조회 성공", response));
    }
}
