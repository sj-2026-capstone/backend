package com.sjcapstone.global.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String SYSTEM_PROMPT = """
            당신은 스마트 제조 공장의 품질관리 전문가입니다.
            제공된 최근 30일 검사 데이터를 분석하여 불량 패턴과 공정 개선 방안을 도출하세요.
            반드시 아래 JSON 형식으로만 응답하세요:
            {
              "patterns": [
                {"title": "패턴 제목", "description": "상세 설명", "severity": "HIGH"}
              ],
              "recommendations": [
                {"title": "조치 제목", "description": "상세 설명"}
              ]
            }
            severity는 반드시 HIGH, MEDIUM, LOW 중 하나여야 합니다.
            patterns는 1개 이상 5개 이하, recommendations는 1개 이상 5개 이하로 작성하세요.
            """;

    private final RestTemplate restTemplate;

    @Value("${openai.api-key}")
    private String apiKey;

    @SuppressWarnings("unchecked")
    public String analyze(String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("OpenAI 공정 분석 요청 시작");
        Map<String, Object> response = restTemplate.postForObject(API_URL, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        log.info("OpenAI 공정 분석 응답 수신 완료");
        return content;
    }
}