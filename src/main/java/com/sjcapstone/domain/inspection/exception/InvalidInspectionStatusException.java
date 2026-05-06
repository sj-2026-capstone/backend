package com.sjcapstone.domain.inspection.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class InvalidInspectionStatusException extends CustomException {
    public InvalidInspectionStatusException() {
        super(ErrorCode.INVALID_INSPECTION_STATUS);
    }
}