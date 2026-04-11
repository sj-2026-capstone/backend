package com.sjcapstone.domain.auth.controller;

import com.sjcapstone.domain.auth.dto.ChangePasswordRequest;
import com.sjcapstone.domain.auth.dto.LoginRequest;
import com.sjcapstone.domain.auth.dto.LoginResponse;
import com.sjcapstone.domain.auth.dto.MeResponse;
import com.sjcapstone.domain.auth.service.AuthService;
import com.sjcapstone.global.response.CommonResponse;
import com.sjcapstone.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(CommonResponse.ok("로그인 성공", response));
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<MeResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MeResponse response = authService.getMe(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("현재 사용자 조회 성공", response));
    }

    @PatchMapping("/password")
    public ResponseEntity<CommonResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(CommonResponse.ok("비밀번호가 변경되었습니다."));
    }
}
