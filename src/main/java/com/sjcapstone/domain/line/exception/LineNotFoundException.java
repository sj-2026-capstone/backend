package com.sjcapstone.domain.line.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class LineNotFoundException extends CustomException {

    public LineNotFoundException() {
        super(ErrorCode.LINE_NOT_FOUND);
    }
}
