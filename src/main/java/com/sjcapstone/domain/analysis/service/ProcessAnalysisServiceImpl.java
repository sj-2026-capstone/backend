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
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import com.sjcapstone.domain.inspection.repository.InspectionRepository;
import com.sjcapstone.global.client.OpenAiClient;
import com.sjcapstone.global.rag.RagChunk;
import com.sjcapstone.global.rag.RagChunkRetriever;
import com.sjcapstone.global.rag.RagQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProcessAnalysisServiceImpl implements ProcessAnalysisService {

    private static final String MODEL_NAME = "gpt-4o-mini";
    private static final int DEFAULT_PERIOD_DAYS = 30;
    private static final int RAG_TOP_N = 5;

    private final AnalysisRepository analysisRepository;
    private final InspectionRepository inspectionRepository;
    private final OpenAiClient openAiClient;
    private final RagChunkRetriever ragChunkRetriever;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ProcessAnalysisResponse startAnalysis(ProcessAnalysisStartRequest request) {
        LocalDate fromDate = request.getFromDate() != null
                ? request.getFromDate()
                : LocalDate.now().minusDays(DEFAULT_PERIOD_DAYS);
        LocalDate toDate = request.getToDate() != null
                ? request.getToDate()
                : LocalDate.now();
        Long lineId = request.getLineId();

        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(LocalTime.MAX);

        // 1. 분석 레코드 생성
        Long[] idRef = {null};
        new TransactionTemplate(transactionManager).execute(status -> {
            ProcessAnalysis a = ProcessAnalysis.createForProcess(fromDate, toDate, lineId, MODEL_NAME);
            a.startProcessing();
            analysisRepository.save(a);
            idRef[0] = a.getId();
            return null;
        });

        // 2. 분석 대상 건수 조회 + 통계 업데이트
        new TransactionTemplate(transactionManager).execute(status -> {
            int totalCount = (int) inspectionRepository.countByStatusAndCreatedAtBetween(
                    InspectionStatus.DONE, from, to);
            int defectCount = (int) inspectionRepository.countDefectsByStatusAndRange(
                    InspectionStatus.DONE, from, to);
            analysisRepository.findById(idRef[0]).ifPresent(a -> a.updateStats(totalCount, defectCount));
            return null;
        });

        // 3. RAG 청크 검색 (트랜잭션 내에서 조회 후 트랜잭션 외부로 반환)
        @SuppressWarnings("unchecked")
        List<RagChunk>[] chunksRef = new List[1];
        chunksRef[0] = Collections.emptyList();
        new TransactionTemplate(transactionManager).execute(status -> {
            RagQuery ragQuery = RagQuery.builder()
                    .from(from)
                    .to(to)
                    .lineId(lineId)
                    .topN(RAG_TOP_N)
                    .build();
            chunksRef[0] = ragChunkRetriever.retrieve(ragQuery);
            return null;
        });

        // 4. OpenAI 호출 (트랜잭션 외부 — DB 커넥션 미점유)
        String userPrompt = buildUserPrompt(fromDate, toDate, chunksRef[0]);
        try {
            String rawJson = openAiClient.analyzeProcess(userPrompt);
            JsonNode node = objectMapper.readTree(rawJson);

            List<ProcessPatternDto> patterns = objectMapper.convertValue(
                    node.get("patterns"), new TypeReference<>() {});
            List<ProcessRecommendationDto> recommendations = objectMapper.convertValue(
                    node.get("recommendations"), new TypeReference<>() {});

            // 5. 결과 저장
            new TransactionTemplate(transactionManager).execute(status -> {
                ProcessAnalysis a = analysisRepository.findById(idRef[0]).orElseThrow();
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
            log.error("RAG 공정 분석 실패 — analysisId={}: {}", idRef[0], e.getMessage());
            new TransactionTemplate(transactionManager).execute(status -> {
                analysisRepository.findById(idRef[0]).ifPresent(a -> a.fail(e.getMessage()));
                return null;
            });
        }

        // 6. 최종 상태 반환
        ProcessAnalysis[] finalRef = {null};
        new TransactionTemplate(transactionManager).execute(status -> {
            finalRef[0] = analysisRepository.findById(idRef[0]).orElseThrow();
            return null;
        });
        return toResponse(finalRef[0]);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessAnalysisResponse getAnalysis(Long analysisId) {
        ProcessAnalysis analysis = analysisRepository.findByIdAndRagUsedTrue(analysisId)
                .orElseThrow(AnalysisNotFoundException::new);
        return toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessAnalysisResponse getLatestAnalysis() {
        ProcessAnalysis analysis = analysisRepository.findTopByRagUsedTrueOrderByCreatedAtDesc()
                .orElseThrow(AnalysisNotFoundException::new);
        return toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessAnalysisPageResponse getAnalysisHistory(Pageable pageable) {
        Page<ProcessAnalysisListItemResponse> page = analysisRepository
                .findByRagUsedTrueOrderByCreatedAtDesc(pageable)
                .map(ProcessAnalysisListItemResponse::from);
        return ProcessAnalysisPageResponse.from(page);
    }

    private ProcessAnalysisResponse toResponse(ProcessAnalysis analysis) {
        List<ProcessPatternDto> patterns = parseJson(analysis.getPatterns(),
                new TypeReference<List<ProcessPatternDto>>() {});
        List<ProcessRecommendationDto> recommendations = parseJson(analysis.getRecommendations(),
                new TypeReference<List<ProcessRecommendationDto>>() {});
        return ProcessAnalysisResponse.from(analysis, patterns, recommendations);
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

    private String buildUserPrompt(LocalDate fromDate, LocalDate toDate, List<RagChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[분석 기간]: %s ~ %s%n", fromDate, toDate));
        sb.append(String.format("[분석 기준 시각]: %s%n%n", LocalDateTime.now()));

        if (chunks.isEmpty()) {
            sb.append("분석 대상 검사 데이터가 없습니다. 데이터 부족을 명시하는 패턴을 작성해주세요.\n");
        } else {
            sb.append("[관련 데이터 청크 — 관련성 높은 순]\n");
            int i = 1;
            for (RagChunk chunk : chunks) {
                sb.append(String.format("%n--- 청크 %d (%s) ---%n", i++, chunk.getType()));
                sb.append(chunk.getContent());
            }
            sb.append("\n위 데이터를 기반으로 공정 이상 패턴과 개선 방안을 분석해주세요.");
        }
        return sb.toString();
    }
}