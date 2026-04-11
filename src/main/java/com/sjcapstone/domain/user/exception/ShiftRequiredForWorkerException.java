package com.sjcapstone.domain.user.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class ShiftRequiredForWorkerException extends CustomException {

    public ShiftRequiredForWorkerException() {
        super(ErrorCode.SHIFT_REQUIRED_FOR_WORKER);
    }
}
