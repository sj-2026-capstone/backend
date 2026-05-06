package com.sjcapstone.domain.inspection.controller;

import com.sjcapstone.domain.inspection.dto.*;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import com.sjcapstone.domain.inspection.service.InspectionService;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;
import com.sjcapstone.global.response.CommonResponse;
import com.sjcapstone.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    // 검사 생성 — ADMIN
    @PostMapping
    public ResponseEntity<CommonResponse<InspectionResponse>> createInspection(
            @RequestBody @Valid InspectionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role = extractRole(userDetails);
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        InspectionResponse response = inspectionService.createInspection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("검사가 생성되었습니다.", response));
    }

    // 검사 이력 목록 조회 — WORKER(자신의 라인), ADMIN(전체)
    @GetMapping
    public ResponseEntity<CommonResponse<InspectionPageResponse>> getInspections(
            @RequestParam(required = false) Long lineId,
            @RequestParam(required = false) InspectionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role = extractRole(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        InspectionPageResponse response = inspectionService.getInspections(
                userDetails.getUserId(), role, lineId, status, pageable);
        return ResponseEntity.ok(CommonResponse.ok("검사 목록 조회 성공", response));
    }

    // 검사 상세 조회 — WORKER(자신의 라인), ADMIN(전체)
    @GetMapping("/{inspectionId}")
    public ResponseEntity<CommonResponse<InspectionResponse>> getInspection(
            @PathVariable Long inspectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role = extractRole(userDetails);
        InspectionResponse response = inspectionService.getInspection(
                inspectionId, userDetails.getUserId(), role);
        return ResponseEntity.ok(CommonResponse.ok("검사 상세 조회 성공", response));
    }

    // 검사 상태 조회 — WORKER(자신의 라인), ADMIN(전체)
    @GetMapping("/{inspectionId}/status")
    public ResponseEntity<CommonResponse<InspectionStatusResponse>> getInspectionStatus(
            @PathVariable Long inspectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role = extractRole(userDetails);
        InspectionStatusResponse response = inspectionService.getInspectionStatus(
                inspectionId, userDetails.getUserId(), role);
        return ResponseEntity.ok(CommonResponse.ok("검사 상태 조회 성공", response));
    }

    // 분석 시작 — ADMIN
    @PostMapping("/{inspectionId}/analyze")
    public ResponseEntity<CommonResponse<InspectionResponse>> startAnalysis(
            @PathVariable Long inspectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role = extractRole(userDetails);
        InspectionResponse response = inspectionService.startAnalysis(
                inspectionId, userDetails.getUserId(), role);
        return ResponseEntity.ok(CommonResponse.ok("검사 분석이 시작되었습니다.", response));
    }

    private UserRole extractRole(CustomUserDetails userDetails) {
        return UserRole.valueOf(userDetails.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", ""));
    }
}