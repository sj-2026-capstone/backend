package com.sjcapstone.domain.auth.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class AuthAlreadyExistsException extends CustomException {
    public AuthAlreadyExistsException() {
        super(ErrorCode.AUTH_ALREADY_EXISTS);
    }
}