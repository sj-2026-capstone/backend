package com.sjcapstone.domain.user.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class DuplicateEmployeeNumberException extends CustomException {
    public DuplicateEmployeeNumberException() {
        super(ErrorCode.DUPLICATE_EMPLOYEE_NUMBER);
    }
}