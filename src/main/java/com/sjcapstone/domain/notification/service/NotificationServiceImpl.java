package com.sjcapstone.domain.notification.service;

import com.sjcapstone.domain.notification.dto.NotificationResponse;
import com.sjcapstone.domain.notification.entity.Notification;
import com.sjcapstone.domain.notification.entity.NotificationType;
import com.sjcapstone.domain.notification.exception.NotificationNotFoundException;
import com.sjcapstone.domain.notification.repository.NotificationRepository;
import com.sjcapstone.domain.notification.repository.SseEmitterRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void send(Long userId, NotificationType type, String title, String message) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(notification);

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

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(NotificationNotFoundException::new);

        notification.markAsRead();

        return NotificationResponse.from(notification);
    }
}