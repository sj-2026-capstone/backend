package com.sjcapstone.domain.notification.service;

import com.sjcapstone.domain.notification.dto.NotificationResponse;
import com.sjcapstone.domain.notification.entity.NotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {

    SseEmitter subscribe(Long userId);

    void send(Long userId, NotificationType type, String title, String message);

    List<NotificationResponse> getNotifications(Long userId);

    NotificationResponse markAsRead(Long notificationId, Long userId);
}