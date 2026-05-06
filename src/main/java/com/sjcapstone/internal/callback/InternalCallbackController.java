package com.sjcapstone.internal.callback;

import com.sjcapstone.domain.inspection.service.InspectionService;
import com.sjcapstone.global.response.CommonResponse;
import com.sjcapstone.internal.callback.dto.AnalysisCallbackRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/callbacks")
@RequiredArgsConstructor
public class InternalCallbackController {

    private final InspectionService inspectionService;

    // 분석 완료 콜백 — AI 분석 서버
    @PostMapping("/{inspectionId}")
    public ResponseEntity<CommonResponse<Void>> receiveCallback(
            @PathVariable Long inspectionId,
            @RequestBody @Valid AnalysisCallbackRequest request) {

        inspectionService.processAnalysisCallback(
                inspectionId,
                request.getHasDefect(),
                request.getDefectType(),
                request.getResultNote()
        );
        return ResponseEntity.ok(CommonResponse.ok("분석 결과가 처리되었습니다."));
    }
}