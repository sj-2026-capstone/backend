package com.sjcapstone.domain.analysis.controller;

import com.sjcapstone.domain.analysis.dto.AnalysisPageResponse;
import com.sjcapstone.domain.analysis.dto.AnalysisResponse;
import com.sjcapstone.domain.analysis.dto.AnalysisStartResponse;
import com.sjcapstone.domain.analysis.service.AnalysisService;
import com.sjcapstone.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<CommonResponse<AnalysisStartResponse>> startAnalysis() {
        return ResponseEntity.ok(CommonResponse.ok("공정 분석이 시작되었습니다.", analysisService.startAnalysis()));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<AnalysisPageResponse>> getAnalysisList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(CommonResponse.ok("분석 목록 조회 성공", analysisService.getAnalysisList(pageable)));
    }

    @GetMapping("/latest")
    public ResponseEntity<CommonResponse<AnalysisResponse>> getLatestAnalysis() {
        return ResponseEntity.ok(CommonResponse.ok("최신 분석 결과 조회 성공", analysisService.getLatestAnalysis()));
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<CommonResponse<AnalysisResponse>> getAnalysis(@PathVariable Long analysisId) {
        return ResponseEntity.ok(CommonResponse.ok("분석 상세 조회 성공", analysisService.getAnalysis(analysisId)));
    }
}