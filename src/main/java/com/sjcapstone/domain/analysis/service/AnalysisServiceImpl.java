package com.sjcapstone.domain.analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjcapstone.domain.analysis.dto.*;
import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import com.sjcapstone.domain.analysis.exception.AnalysisNotFoundException;
import com.sjcapstone.domain.analysis.repository.AnalysisRepository;
import com.sjcapstone.global.client.AiAnalysisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AiAnalysisClient aiAnalysisClient;
    private final ObjectMapper objectMapper;

    @Override
    public AnalysisStartResponse startAnalysis() {
        ProcessAnalysis analysis = ProcessAnalysis.create();
        analysisRepository.save(analysis);

        try {
            analysis.startProcessing();
            aiAnalysisClient.requestProcessAnalysis(analysis.getId());
        } catch (Exception e) {
            log.error("AI 공정 분석 요청 실패 — analysisId={}, error={}", analysis.getId(), e.getMessage());
            analysis.fail("AI 분석 요청 실패: " + e.getMessage());
        }

        return AnalysisStartResponse.from(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisPageResponse getAnalysisList(Pageable pageable) {
        Page<AnalysisListItemResponse> page = analysisRepository.findAll(pageable)
                .map(AnalysisListItemResponse::from);
        return AnalysisPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(Long analysisId) {
        ProcessAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(AnalysisNotFoundException::new);
        return toAnalysisResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResponse getLatestAnalysis() {
        ProcessAnalysis analysis = analysisRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(AnalysisNotFoundException::new);
        return toAnalysisResponse(analysis);
    }

    @Override
    public void processCallback(Long analysisId, ProcessAnalysisCallbackRequest request) {
        ProcessAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(AnalysisNotFoundException::new);

        if (analysis.getStatus() != AnalysisStatus.PROCESSING) {
            log.warn("공정 분석 콜백 수신 — 유효하지 않은 상태: analysisId={}, status={}", analysisId, analysis.getStatus());
            return;
        }

        try {
            String patternsJson = objectMapper.writeValueAsString(request.getPatterns());
            String recommendationsJson = objectMapper.writeValueAsString(request.getRecommendations());
            analysis.complete(patternsJson, recommendationsJson);
        } catch (JsonProcessingException e) {
            log.error("공정 분석 결과 직렬화 실패 — analysisId={}", analysisId, e);
            analysis.fail("결과 저장 실패: " + e.getMessage());
        }
    }

    private AnalysisResponse toAnalysisResponse(ProcessAnalysis analysis) {
        List<PatternDto> patterns = parseJson(analysis.getPatterns(), new TypeReference<>() {});
        List<RecommendationDto> recommendations = parseJson(analysis.getRecommendations(), new TypeReference<>() {});
        return AnalysisResponse.from(analysis, patterns, recommendations);
    }

    private <T> List<T> parseJson(String json, TypeReference<List<T>> typeRef) {
        if (json == null) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}