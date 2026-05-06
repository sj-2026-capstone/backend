package com.sjcapstone.domain.inspection.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class InspectionNotFoundException extends CustomException {
    public InspectionNotFoundException() {
        super(ErrorCode.INSPECTION_NOT_FOUND);
    }
}