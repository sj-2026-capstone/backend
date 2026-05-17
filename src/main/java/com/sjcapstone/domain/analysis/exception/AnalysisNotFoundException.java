package com.sjcapstone.domain.analysis.exception;

import com.sjcapstone.global.exception.CustomException;
import com.sjcapstone.global.exception.ErrorCode;

public class AnalysisNotFoundException extends CustomException {
    public AnalysisNotFoundException() {
        super(ErrorCode.ANALYSIS_NOT_FOUND);
    }
}