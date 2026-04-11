package com.sjcapstone.domain.auth.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class DuplicateLoginIdException extends CustomException {

    public DuplicateLoginIdException() {
        super(ErrorCode.DUPLICATE_LOGIN_ID);
    }
}
