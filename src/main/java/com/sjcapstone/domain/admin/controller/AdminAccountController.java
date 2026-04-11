package com.sjcapstone.domain.admin.controller;

import com.sjcapstone.domain.admin.dto.AdminAccountCreateRequest;
import com.sjcapstone.domain.admin.dto.AdminAccountPageResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountStatusUpdateRequest;
import com.sjcapstone.domain.admin.dto.AdminAccountSummaryResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountUpdateRequest;
import com.sjcapstone.domain.admin.dto.LoginIdAvailabilityResponse;
import com.sjcapstone.domain.admin.service.AdminAccountService;
import com.sjcapstone.domain.user.entity.UserStatus;
import com.sjcapstone.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @PostMapping
    public ResponseEntity<CommonResponse<AdminAccountResponse>> createAccount(
            @Valid @RequestBody AdminAccountCreateRequest request) {
        AdminAccountResponse response = adminAccountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("계정이 생성되었습니다.", response));
    }

    @GetMapping("/login-id/availability")
    public ResponseEntity<CommonResponse<LoginIdAvailabilityResponse>> checkLoginIdAvailability(
            @RequestParam String loginId) {
        LoginIdAvailabilityResponse response = adminAccountService.checkLoginIdAvailability(loginId);
        return ResponseEntity.ok(CommonResponse.ok("로그인 ID 사용 가능 여부 조회 성공", response));
    }

    @GetMapping("/summary")
    public ResponseEntity<CommonResponse<AdminAccountSummaryResponse>> getAccountSummary() {
        AdminAccountSummaryResponse response = adminAccountService.getAccountSummary();
        return ResponseEntity.ok(CommonResponse.ok("계정 요약 조회 성공", response));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<AdminAccountPageResponse>> getAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AdminAccountPageResponse response = adminAccountService.getAccounts(keyword, status, page, size);
        return ResponseEntity.ok(CommonResponse.ok("계정 목록 조회 성공", response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<AdminAccountResponse>> getAccount(@PathVariable Long userId) {
        AdminAccountResponse response = adminAccountService.getAccount(userId);
        return ResponseEntity.ok(CommonResponse.ok("계정 상세 조회 성공", response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<CommonResponse<AdminAccountResponse>> updateAccount(
            @PathVariable Long userId,
            @Valid @RequestBody AdminAccountUpdateRequest request) {
        AdminAccountResponse response = adminAccountService.updateAccount(userId, request);
        return ResponseEntity.ok(CommonResponse.ok("계정 정보가 수정되었습니다.", response));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<CommonResponse<AdminAccountResponse>> updateAccountStatus(
            @PathVariable Long userId,
            @Valid @RequestBody AdminAccountStatusUpdateRequest request) {
        AdminAccountResponse response = adminAccountService.updateAccountStatus(userId, request);
        return ResponseEntity.ok(CommonResponse.ok("계정 상태가 변경되었습니다.", response));
    }
}
