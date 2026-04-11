package com.sjcapstone.domain.notification.service;

import com.sjcapstone.domain.notification.dto.NotificationPageResponse;
import com.sjcapstone.domain.notification.dto.NotificationResponse;
import com.sjcapstone.domain.notification.dto.UnreadCountResponse;
import com.sjcapstone.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    SseEmitter subscribe(Long userId);

    /**
     * 특정 사용자에게 알림을 생성하고 SSE로 즉시 전송한다.
     */
    void send(Long userId, NotificationType type, String title, String message);

    /**
     * 현재 ADMIN 권한을 가진 모든 사용자(soft delete 제외)에게 알림을 생성하고 SSE로 전송한다.
     */
    void sendToAdmins(NotificationType type, String title, String message);

    /**
     * 불량 감지 알림을 모든 관리자에게 발송하는 convenience 메서드.
     *
     * @param lineName    라인명 (예: "A라인")
     * @param defectType  불량 유형 (예: "도어 스크래치")
     * @param handlerName 담당자 이름. null 이면 담당자 정보 생략
     */
    void sendDefectDetected(String lineName, String defectType, String handlerName);

    /**
     * 알림 목록을 페이징/필터링하여 조회한다.
     *
     * @param isRead null = 전체, false = 미확인, true = 확인완료
     */
    NotificationPageResponse getNotifications(Long userId, Boolean isRead, Pageable pageable);

    /**
     * 미확인 알림 개수를 반환한다.
     */
    UnreadCountResponse getUnreadCount(Long userId);

    /**
     * 단건 읽음 처리 후 갱신된 알림을 반환한다.
     */
    NotificationResponse markAsRead(Long notificationId, Long userId);

    /**
     * 해당 사용자의 미확인 알림을 전부 읽음 처리한다.
     */
    void markAllAsRead(Long userId);
}