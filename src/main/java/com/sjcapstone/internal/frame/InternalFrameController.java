package com.sjcapstone.internal.frame;

import com.sjcapstone.domain.inspection.dto.InspectionCreateRequest;
import com.sjcapstone.domain.inspection.dto.InspectionResponse;
import com.sjcapstone.domain.inspection.service.InspectionService;
import com.sjcapstone.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/frames")
@RequiredArgsConstructor
public class InternalFrameController {

    private final InspectionService inspectionService;

    // 프레임 수집 — 카메라/엣지 디바이스
    @PostMapping
    public ResponseEntity<CommonResponse<InspectionResponse>> uploadFrame(
            @RequestBody @Valid InspectionCreateRequest request) {

        InspectionResponse response = inspectionService.createInspectionAndStartAnalysis(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("프레임이 접수되어 분석이 시작되었습니다.", response));
    }
}
