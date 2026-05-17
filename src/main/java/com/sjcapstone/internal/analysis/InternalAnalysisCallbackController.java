package com.sjcapstone.internal.analysis;

import com.sjcapstone.domain.analysis.dto.ProcessAnalysisCallbackRequest;
import com.sjcapstone.domain.analysis.service.AnalysisService;
import com.sjcapstone.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/analysis-callbacks")
@RequiredArgsConstructor
public class InternalAnalysisCallbackController {

    private final AnalysisService analysisService;

    @PostMapping("/{analysisId}")
    public ResponseEntity<CommonResponse<Void>> receiveCallback(
            @PathVariable Long analysisId,
            @RequestBody @Valid ProcessAnalysisCallbackRequest request) {

        analysisService.processCallback(analysisId, request);
        return ResponseEntity.ok(CommonResponse.<Void>ok("공정 분석 결과가 처리되었습니다."));
    }
}