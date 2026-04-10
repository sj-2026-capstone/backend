package com.sjcapstone.domain.notification.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class NotificationNotFoundException extends CustomException {
    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}