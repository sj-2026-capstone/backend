package com.sjcapstone.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_EMPLOYEE_NUMBER(HttpStatus.CONFLICT, "이미 사용 중인 사번입니다."),

    // Shift
    SHIFT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 교대조입니다."),
    SHIFT_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "해당 날짜에 이미 배정된 사용자입니다."),
    SHIFT_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 교대조에는 배정할 수 없습니다."),
    INVALID_SHIFT_TIME(HttpStatus.BAD_REQUEST, "유효하지 않은 교대 시간입니다."),

    // Auth
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    AUTH_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 인증 정보가 존재합니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}