package com.sjcapstone.domain.auth.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class PasswordConfirmMismatchException extends CustomException {

    public PasswordConfirmMismatchException() {
        super(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
    }
}
