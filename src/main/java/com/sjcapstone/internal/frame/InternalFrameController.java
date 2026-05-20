package com.sjcapstone.internal.frame;

import com.sjcapstone.domain.inspection.dto.InspectionCreateRequest;
import com.sjcapstone.domain.inspection.dto.InspectionResponse;
import com.sjcapstone.domain.inspection.service.InspectionService;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.repository.ShiftRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import com.sjcapstone.domain.user.repository.UserRepository;
import com.sjcapstone.global.file.FileStorageService;
import com.sjcapstone.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;

@RestController
@RequestMapping("/internal/frames")
@RequiredArgsConstructor
public class InternalFrameController {

    private final InspectionService inspectionService;
    private final FileStorageService fileStorageService;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<InspectionResponse>> uploadFrame(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long lineId) {

        String imageUrl = fileStorageService.store(file);

        Long shiftId = shiftRepository.findCurrentShift(LocalTime.now())
                .map(Shift::getId)
                .orElse(null);

        Long workerId = userRepository
                .findFirstByLine_IdAndRoleAndStatusAndDeletedAtIsNull(lineId, UserRole.WORKER, UserStatus.ACTIVE)
                .map(User::getId)
                .orElse(null);

        InspectionCreateRequest request = new InspectionCreateRequest(lineId, workerId, shiftId, imageUrl);
        InspectionResponse response = inspectionService.createInspectionAndStartAnalysis(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("프레임이 접수되어 분석이 시작되었습니다.", response));
    }
}
