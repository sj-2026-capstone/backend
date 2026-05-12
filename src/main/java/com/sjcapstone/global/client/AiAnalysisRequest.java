package com.sjcapstone.global.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiAnalysisRequest {

    private Long inspectionId;
    private String imageUrl;
    private String callbackUrl; // AI 서버가 결과를 보낼 백엔드 콜백 URL
}