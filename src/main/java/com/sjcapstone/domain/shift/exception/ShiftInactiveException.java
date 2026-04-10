package com.sjcapstone.domain.shift.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class ShiftInactiveException extends CustomException {
    public ShiftInactiveException() {
        super(ErrorCode.SHIFT_INACTIVE);
    }
}