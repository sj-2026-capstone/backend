package com.sjcapstone.domain.user.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class LineRequiredForWorkerException extends CustomException {

    public LineRequiredForWorkerException() {
        super(ErrorCode.LINE_REQUIRED_FOR_WORKER);
    }
}
