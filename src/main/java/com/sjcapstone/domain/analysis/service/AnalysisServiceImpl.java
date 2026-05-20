package com.sjcapstone.domain.analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjcapstone.domain.analysis.dto.*;
import com.sjcapstone.domain.analysis.entity.AnalysisStatus;
import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import com.sjcapstone.domain.analysis.exception.AnalysisNotFoundException;
import com.sjcapstone.domain.analysis.repository.AnalysisRepository;
import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import com.sjcapstone.domain.inspection.repository.InspectionRepository;
import com.sjcapstone.global.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final InspectionRepository inspectionRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AnalysisStartResponse startAnalysis() {
        // 1. 분석 레코드 생성 및 저장
        Long[] analysisIdRef = {null};
        new TransactionTemplate(transactionManager).execute(status -> {
            ProcessAnalysis a = ProcessAnalysis.create();
            a.startProcessing();
            analysisRepository.save(a);
            analysisIdRef[0] = a.getId();
            return null;
        });

        // 2. 최근 30일 검사 데이터 수집 및 프롬프트 생성
        String[] promptRef = {null};
        new TransactionTemplate(transactionManager).execute(status -> {
            List<Inspection> inspections = inspectionRepository.findDoneInspectionsForAnalysis(
                    InspectionStatus.DONE, LocalDateTime.now().minusDays(30));
            promptRef[0] = buildAnalysisPrompt(inspections);
            return null;
        });

        // 3. OpenAI 호출 (트랜잭션 외부 — DB 커넥션 미점유)
        try {
            String rawJson = openAiClient.analyze(promptRef[0]);
            JsonNode node = objectMapper.readTree(rawJson);
            List<PatternDto> patterns = objectMapper.convertValue(
                    node.get("patterns"), new TypeReference<>() {});
            List<RecommendationDto> recommendations = objectMapper.convertValue(
                    node.get("recommendations"), new TypeReference<>() {});

            // 4. 결과 저장
            new TransactionTemplate(transactionManager).execute(status -> {
                ProcessAnalysis a = analysisRepository.findById(analysisIdRef[0]).orElseThrow();
                try {
                    a.complete(
                            objectMapper.writeValueAsString(patterns),
                            objectMapper.writeValueAsString(recommendations)
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });

        } catch (Exception e) {
            log.error("OpenAI 공정 분석 실패 — analysisId={}: {}", analysisIdRef[0], e.getMessage());
            new TransactionTemplate(transactionManager).execute(status -> {
                analysisRepository.findById(analysisIdRef[0])
                        .ifPresent(a -> a.fail(e.getMessage()));
                return null;
            });
        }

        // 5. 최종 상태 반환
        ProcessAnalysis[] finalRef = {null};
        new TransactionTemplate(transactionManager).execute(status -> {
            finalRef[0] = analysisRepository.findById(analysisIdRef[0]).orElseThrow();
            return null;
        });
        return AnalysisStartResponse.from(finalRef[0]);
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

    private String buildAnalysisPrompt(List<Inspection> inspections) {
        long total = inspections.size();
        long defects = inspections.stream().filter(i -> Boolean.TRUE.equals(i.getHasDefect())).count();
        double defectRate = total > 0 ? (double) defects / total * 100 : 0;

        Map<String, Long> totalByLine = inspections.stream()
                .collect(Collectors.groupingBy(i -> i.getLine().getLineName(), Collectors.counting()));

        Map<String, Long> defectsByLine = inspections.stream()
                .filter(i -> Boolean.TRUE.equals(i.getHasDefect()))
                .collect(Collectors.groupingBy(i -> i.getLine().getLineName(), Collectors.counting()));

        Map<String, Long> defectsByType = inspections.stream()
                .filter(i -> Boolean.TRUE.equals(i.getHasDefect()) && i.getDefectType() != null)
                .collect(Collectors.groupingBy(i -> i.getDefectType().getDisplayName(), Collectors.counting()));

        Map<String, Long> defectsByShift = inspections.stream()
                .filter(i -> Boolean.TRUE.equals(i.getHasDefect()) && i.getShift() != null)
                .collect(Collectors.groupingBy(i -> i.getShift().getShiftName(), Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("최근 30일 생산라인 검사 데이터 분석 요청\n\n");

        sb.append("[전체 현황]\n");
        sb.append(String.format("- 총 검사 건수: %d건%n", total));
        sb.append(String.format("- 불량 발생 건수: %d건%n", defects));
        sb.append(String.format("- 불량률: %.1f%%%n%n", defectRate));

        sb.append("[라인별 불량 현황]\n");
        totalByLine.forEach((line, cnt) -> {
            long defectCnt = defectsByLine.getOrDefault(line, 0L);
            double rate = cnt > 0 ? (double) defectCnt / cnt * 100 : 0;
            sb.append(String.format("- %s: %d건 중 %d건 불량 (%.1f%%)%n", line, cnt, defectCnt, rate));
        });

        sb.append("\n[불량 유형별 현황]\n");
        if (defectsByType.isEmpty()) {
            sb.append("- 불량 없음\n");
        } else {
            defectsByType.forEach((type, cnt) ->
                    sb.append(String.format("- %s: %d건%n", type, cnt)));
        }

        sb.append("\n[교대조별 불량 현황]\n");
        if (defectsByShift.isEmpty()) {
            sb.append("- 데이터 없음\n");
        } else {
            defectsByShift.forEach((shift, cnt) ->
                    sb.append(String.format("- %s: %d건 불량%n", shift, cnt)));
        }

        return sb.toString();
    }
}