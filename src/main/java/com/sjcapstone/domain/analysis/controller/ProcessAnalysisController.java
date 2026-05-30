package com.sjcapstone.domain.analysis.controller;

import com.sjcapstone.domain.analysis.dto.ProcessAnalysisPageResponse;
import com.sjcapstone.domain.analysis.dto.ProcessAnalysisResponse;
import com.sjcapstone.domain.analysis.dto.ProcessAnalysisStartRequest;
import com.sjcapstone.domain.analysis.service.ProcessAnalysisService;
import com.sjcapstone.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis/process")
@RequiredArgsConstructor
public class ProcessAnalysisController {

    private final ProcessAnalysisService processAnalysisService;

    @PostMapping("/start")
    public ResponseEntity<CommonResponse<ProcessAnalysisResponse>> startAnalysis(
            @RequestBody(required = false) ProcessAnalysisStartRequest request) {
        ProcessAnalysisStartRequest req = request != null ? request : new ProcessAnalysisStartRequest();
        return ResponseEntity.ok(CommonResponse.ok(
                "공정 분석이 완료되었습니다.",
                processAnalysisService.startAnalysis(req)));
    }

    @GetMapping("/latest")
    public ResponseEntity<CommonResponse<ProcessAnalysisResponse>> getLatestAnalysis() {
        return ResponseEntity.ok(CommonResponse.ok(
                "최신 공정 분석 결과 조회 성공",
                processAnalysisService.getLatestAnalysis()));
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<CommonResponse<ProcessAnalysisResponse>> getAnalysis(
            @PathVariable Long analysisId) {
        return ResponseEntity.ok(CommonResponse.ok(
                "공정 분석 상세 조회 성공",
                processAnalysisService.getAnalysis(analysisId)));
    }

    @GetMapping("/history")
    public ResponseEntity<CommonResponse<ProcessAnalysisPageResponse>> getAnalysisHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(CommonResponse.ok(
                "공정 분석 이력 조회 성공",
                processAnalysisService.getAnalysisHistory(pageable)));
    }
}