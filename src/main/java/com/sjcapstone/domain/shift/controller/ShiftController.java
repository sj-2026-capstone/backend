package com.sjcapstone.domain.shift.controller;

import com.sjcapstone.domain.shift.dto.ShiftAssignmentRequest;
import com.sjcapstone.domain.shift.dto.ShiftAssignmentResponse;
import com.sjcapstone.domain.shift.dto.ShiftCreateRequest;
import com.sjcapstone.domain.shift.dto.ShiftResponse;
import com.sjcapstone.domain.shift.dto.ShiftUpdateRequest;
import com.sjcapstone.domain.shift.service.ShiftService;
import com.sjcapstone.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    // 교대조 생성 — ADMIN
    @PostMapping
    public ResponseEntity<CommonResponse<ShiftResponse>> createShift(@Valid @RequestBody ShiftCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("교대조가 생성되었습니다.", shiftService.createShift(request)));
    }

    // 활성 교대조 목록 조회 — ADMIN, WORKER
    @GetMapping
    public ResponseEntity<CommonResponse<List<ShiftResponse>>> getAllShifts() {
        return ResponseEntity.ok(CommonResponse.ok("교대조 목록 조회 성공", shiftService.getAllShifts()));
    }

    // 교대조 단건 조회 — ADMIN, WORKER
    @GetMapping("/{shiftId}")
    public ResponseEntity<CommonResponse<ShiftResponse>> getShift(@PathVariable Long shiftId) {
        return ResponseEntity.ok(CommonResponse.ok("교대조 조회 성공", shiftService.getShift(shiftId)));
    }

    // 교대조 수정 — ADMIN
    @PutMapping("/{shiftId}")
    public ResponseEntity<CommonResponse<ShiftResponse>> updateShift(
            @PathVariable Long shiftId,
            @Valid @RequestBody ShiftUpdateRequest request) {
        return ResponseEntity.ok(CommonResponse.ok("교대조가 수정되었습니다.", shiftService.updateShift(shiftId, request)));
    }

    // 교대조 비활성화 — ADMIN
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<CommonResponse<Void>> deactivateShift(@PathVariable Long shiftId) {
        shiftService.deactivateShift(shiftId);
        return ResponseEntity.ok(CommonResponse.ok("교대조가 비활성화되었습니다."));
    }

    // 날짜별 교대 배정 — ADMIN
    @PostMapping("/assignments")
    public ResponseEntity<CommonResponse<ShiftAssignmentResponse>> assignUserToShift(
            @Valid @RequestBody ShiftAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("교대 배정이 완료되었습니다.", shiftService.assignUserToShift(request)));
    }

    // 날짜별 교대표 조회 — ADMIN, WORKER
    @GetMapping("/assignments")
    public ResponseEntity<CommonResponse<List<ShiftAssignmentResponse>>> getAssignmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(CommonResponse.ok("날짜별 교대 배정 조회 성공", shiftService.getAssignmentsByDate(date)));
    }

    // 특정 사용자 배정 이력 조회 — ADMIN
    @GetMapping("/assignments/users/{userId}")
    public ResponseEntity<CommonResponse<List<ShiftAssignmentResponse>>> getAssignmentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(CommonResponse.ok("사용자 배정 이력 조회 성공", shiftService.getAssignmentsByUser(userId)));
    }
}