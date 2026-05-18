package com.sjcapstone.global.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalysisClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public void requestAnalysis(Long inspectionId, String imageUrl) {
        String callbackUrl = appBaseUrl + "/internal/callbacks/" + inspectionId;

        AiAnalysisRequest request = new AiAnalysisRequest(String.valueOf(inspectionId), appBaseUrl + imageUrl, callbackUrl);

        try {
            restTemplate.postForObject(aiServerUrl + "/analyze", request, Void.class);
            log.info("AI 분석 요청 전송 완료 — inspectionId={}", inspectionId);
        } catch (Exception e) {
            log.error("AI 분석 요청 실패 — inspectionId={}, error={}", inspectionId, e.getMessage());
            throw e;
        }
    }

    // AI 서버 엔드포인트 확정 후 URL 업데이트 필요
    public void requestProcessAnalysis(Long analysisId) {
        String callbackUrl = appBaseUrl + "/internal/analysis-callbacks/" + analysisId;

        AiProcessAnalysisRequest request = new AiProcessAnalysisRequest(String.valueOf(analysisId), callbackUrl);

        try {
            restTemplate.postForObject(aiServerUrl + "/process-analyze", request, Void.class);
            log.info("AI 공정 분석 요청 전송 완료 — analysisId={}", analysisId);
        } catch (Exception e) {
            log.error("AI 공정 분석 요청 실패 — analysisId={}, error={}", analysisId, e.getMessage());
            throw e;
        }
    }
}