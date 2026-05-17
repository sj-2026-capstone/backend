package com.sjcapstone.global.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiProcessAnalysisRequest {

    private String analysisId;
    private String callbackUrl;
}