package com.sjcapstone.domain.notification.controller;

import com.sjcapstone.domain.notification.dto.NotificationResponse;
import com.sjcapstone.domain.notification.service.NotificationService;
import com.sjcapstone.global.response.CommonResponse;
import com.sjcapstone.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // SSE 구독 — ADMIN
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.subscribe(userDetails.getUserId());
    }

    // 알림 목록 조회 — ADMIN
    @GetMapping
    public ResponseEntity<CommonResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationResponse> response = notificationService.getNotifications(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("알림 목록 조회 성공", response));
    }

    // 알림 읽음 처리 — ADMIN
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<CommonResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NotificationResponse response = notificationService.markAsRead(notificationId, userDetails.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("알림을 읽음 처리했습니다.", response));
    }
}