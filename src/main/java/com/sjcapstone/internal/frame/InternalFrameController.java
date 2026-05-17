package com.sjcapstone.internal.frame;

import com.sjcapstone.domain.inspection.dto.InspectionCreateRequest;
import com.sjcapstone.domain.inspection.dto.InspectionResponse;
import com.sjcapstone.domain.inspection.service.InspectionService;
import com.sjcapstone.global.file.FileStorageService;
import com.sjcapstone.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/internal/frames")
@RequiredArgsConstructor
public class InternalFrameController {

    private final InspectionService inspectionService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<InspectionResponse>> uploadFrame(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long lineId,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Long shiftId) {

        String imageUrl = fileStorageService.store(file);
        InspectionCreateRequest request = new InspectionCreateRequest(lineId, workerId, shiftId, imageUrl);
        InspectionResponse response = inspectionService.createInspectionAndStartAnalysis(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("프레임이 접수되어 분석이 시작되었습니다.", response));
    }
}
