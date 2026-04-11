package com.sjcapstone.domain.user.controller;

import com.sjcapstone.domain.user.dto.UserListResponse;
import com.sjcapstone.domain.user.dto.UserResponse;
import com.sjcapstone.domain.user.dto.UserUpdateRequest;
import com.sjcapstone.domain.user.service.UserService;
import com.sjcapstone.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(CommonResponse.ok("사용자 조회 성공", response));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<UserListResponse>>> getUsers() {
        List<UserListResponse> response = userService.getUsers();
        return ResponseEntity.ok(CommonResponse.ok("사용자 목록 조회 성공", response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> updateUser(@PathVariable Long userId,
                                                                   @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(CommonResponse.ok("사용자 정보가 수정되었습니다.", response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(CommonResponse.ok("사용자가 삭제되었습니다."));
    }
}
