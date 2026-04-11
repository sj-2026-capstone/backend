package com.sjcapstone.domain.notification.service;

import com.sjcapstone.domain.notification.dto.NotificationPageResponse;
import com.sjcapstone.domain.notification.dto.NotificationResponse;
import com.sjcapstone.domain.notification.dto.UnreadCountResponse;
import com.sjcapstone.domain.notification.entity.Notification;
import com.sjcapstone.domain.notification.entity.NotificationType;
import com.sjcapstone.domain.notification.exception.NotificationNotFoundException;
import com.sjcapstone.domain.notification.repository.NotificationRepository;
import com.sjcapstone.domain.notification.repository.SseEmitterRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final UserRepository userRepository;

    // ──────────────────────────────────────────────────────────────────────
    // SSE 구독
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        sseEmitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> sseEmitterRepository.deleteByUserId(userId));
        emitter.onTimeout(() -> sseEmitterRepository.deleteByUserId(userId));
        emitter.onError(e -> sseEmitterRepository.deleteByUserId(userId));

        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE 연결 완료"));
        } catch (IOException e) {
            sseEmitterRepository.deleteByUserId(userId);
        }

        return emitter;
    }

    // ──────────────────────────────────────────────────────────────────────
    // 알림 발송
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public void send(Long userId, NotificationType type, String title, String message) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        Notification notification = saveNotification(user, type, title, message);
        pushSse(userId, notification);
    }

    @Override
    public void sendToAdmins(NotificationType type, String title, String message) {
        List<User> admins = userRepository.findAllByRoleAndDeletedAtIsNull(UserRole.ADMIN);

        for (User admin : admins) {
            Notification notification = saveNotification(admin, type, title, message);
            pushSse(admin.getId(), notification);
        }
    }

    @Override
    public void sendDefectDetected(String lineName, String defectType, String handlerName) {
        String title = "불량 부품 감지";
        String message = buildDefectMessage(lineName, defectType, handlerName);
        sendToAdmins(NotificationType.DEFECT_DETECTED, title, message);
    }

    // ──────────────────────────────────────────────────────────────────────
    // 알림 조회
    // ──────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(Long userId, Boolean isRead, Pageable pageable) {
        Page<NotificationResponse> page;

        if (isRead == null) {
            page = notificationRepository
                    .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                    .map(NotificationResponse::from);
        } else {
            page = notificationRepository
                    .findAllByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable)
                    .map(NotificationResponse::from);
        }

        return NotificationPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder().count(count).build();
    }

    // ──────────────────────────────────────────────────────────────────────
    // 읽음 처리
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(NotificationNotFoundException::new);

        notification.markAsRead();

        return NotificationResponse.from(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // ──────────────────────────────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────────────────────────────

    private Notification saveNotification(User user, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .build();
        return notificationRepository.save(notification);
    }

    private void pushSse(Long userId, Notification notification) {
        sseEmitterRepository.findByUserId(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(NotificationResponse.from(notification)));
            } catch (IOException e) {
                sseEmitterRepository.deleteByUserId(userId);
                log.warn("SSE 전송 실패 — userId: {}", userId);
            }
        });
    }

    private String buildDefectMessage(String lineName, String defectType, String handlerName) {
        StringBuilder sb = new StringBuilder();
        sb.append(lineName).append(" - ").append(defectType);
        if (handlerName != null && !handlerName.isBlank()) {
            sb.append(" | 담당자: ").append(handlerName);
        }
        return sb.toString();
    }
}