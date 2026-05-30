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

    private static final String PROCESS_ANALYSIS_SYSTEM_PROMPT = """
            당신은 스마트 제조 공장의 품질관리 AI 전문가입니다.
            제공된 검사 데이터 통계 청크를 분석하여 불량 패턴과 공정 개선 방안을 도출하세요.
            patternId는 1부터 순번을 부여하고, recommendations의 relatedPatternIds는 해당 패턴 번호를 참조하세요.

            반드시 아래 JSON 형식으로만 응답하세요:
            {
              "patterns": [
                {
                  "patternId": 1,
                  "title": "패턴 제목 (20자 이내)",
                  "description": "구체적인 수치를 포함한 상세 설명",
                  "severity": "HIGH",
                  "relatedLine": "A라인",
                  "relatedTimeRange": "22:00~02:00",
                  "metric": "주간 대비 3.2배 집중 발생",
                  "evidenceSummary": "A라인 22~02시: 19건 / 전체 평균: 5.9건"
                }
              ],
              "recommendations": [
                {
                  "priority": 1,
                  "title": "조치 제목 (20자 이내)",
                  "description": "구체적인 조치 방법",
                  "targetLine": "A라인",
                  "expectedEffect": "야간 스크래치 불량률 40% 감소 예상",
                  "relatedPatternIds": [1]
                }
              ]
            }
            severity는 반드시 HIGH, MEDIUM, LOW 중 하나여야 합니다.
            patterns는 1개 이상 5개 이하, recommendations는 1개 이상 5개 이하로 작성하세요.
            데이터가 부족하면 LOW 심각도로 관찰 수준 패턴을 작성하세요.
            """;

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

    @SuppressWarnings("unchecked")
    public String analyzeProcess(String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(
                Map.of("role", "system", "content", PROCESS_ANALYSIS_SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.5);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("OpenAI RAG 공정 분석 요청 시작");
        Map<String, Object> response = restTemplate.postForObject(API_URL, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        log.info("OpenAI RAG 공정 분석 응답 수신 완료");
        return content;
    }
}