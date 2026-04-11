package com.sjcapstone.domain.auth.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class AuthNotFoundException extends CustomException {

    public AuthNotFoundException() {
        super(ErrorCode.AUTH_NOT_FOUND);
    }
}
