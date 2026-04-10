package com.sjcapstone.domain.shift.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class InvalidShiftTimeException extends CustomException {
    public InvalidShiftTimeException() {
        super(ErrorCode.INVALID_SHIFT_TIME);
    }
}