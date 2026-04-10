package com.sjcapstone.domain.shift.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class ShiftAlreadyAssignedException extends CustomException {
    public ShiftAlreadyAssignedException() {
        super(ErrorCode.SHIFT_ALREADY_ASSIGNED);
    }
}