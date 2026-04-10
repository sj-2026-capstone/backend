package com.sjcapstone.domain.user.exception;

public class DuplicateEmployeeIdException extends RuntimeException {
    public DuplicateEmployeeIdException() {
        super("이미 사용 중인 사원 ID입니다.");
    }
}
