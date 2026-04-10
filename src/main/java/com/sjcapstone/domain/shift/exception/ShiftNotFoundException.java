package com.sjcapstone.domain.shift.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class ShiftNotFoundException extends CustomException {
    public ShiftNotFoundException() {
        super(ErrorCode.SHIFT_NOT_FOUND);
    }
}