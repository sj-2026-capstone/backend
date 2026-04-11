package com.sjcapstone.domain.notification.controller;

import com.sjcapstone.domain.notification.dto.NotificationPageResponse;
import com.sjcapstone.domain.notification.dto.NotificationResponse;
import com.sjcapstone.domain.notification.dto.UnreadCountResponse;
import com.sjcapstone.domain.notification.service.NotificationService;
import com.sjcapstone.global.response.CommonResponse;
import com.sjcapstone.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    // 알림 목록 조회 (필터/페이징) — ADMIN
    // read: null=전체, false=미확인, true=확인완료
    @GetMapping
    public ResponseEntity<CommonResponse<NotificationPageResponse>> getNotifications(
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size);
        NotificationPageResponse response = notificationService.getNotifications(
                userDetails.getUserId(), read, pageable);
        return ResponseEntity.ok(CommonResponse.ok("알림 목록 조회 성공", response));
    }

    // 미확인 알림 개수 조회 — ADMIN
    @GetMapping("/unread-count")
    public ResponseEntity<CommonResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UnreadCountResponse response = notificationService.getUnreadCount(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("미확인 알림 개수 조회 성공", response));
    }

    // 단건 읽음 처리 — ADMIN
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<CommonResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NotificationResponse response = notificationService.markAsRead(notificationId, userDetails.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("알림을 읽음 처리했습니다.", response));
    }

    // 전체 읽음 처리 — ADMIN
    @PatchMapping("/read-all")
    public ResponseEntity<CommonResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("모든 알림을 읽음 처리했습니다."));
    }
}