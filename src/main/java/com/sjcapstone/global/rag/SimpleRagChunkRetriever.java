package com.sjcapstone.global.rag;

import com.sjcapstone.domain.analysis.dto.projection.*;
import com.sjcapstone.domain.inspection.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SQL 집계 통계를 RAG 청크로 변환하는 1차 구현체.
 * 벡터 임베딩 없이 관련성 점수(heuristic)로 상위 N개 청크 선택.
 * 추후 EmbeddingRagChunkRetriever로 교체 시 이 클래스만 대체하면 됨.
 */
@Component
@RequiredArgsConstructor
public class SimpleRagChunkRetriever implements RagChunkRetriever {

    private final InspectionRepository inspectionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RagChunk> retrieve(RagQuery query) {
        List<RagChunk> chunks = new ArrayList<>();

        chunks.add(buildLineStatsChunk(query));
        chunks.add(buildShiftStatsChunk(query));
        chunks.add(buildHourStatsChunk(query));
        chunks.add(buildDefectTypeChunk(query));
        chunks.add(buildWeeklyTrendChunk(query));

        return chunks.stream()
                .filter(c -> !c.getContent().isBlank())
                .sorted(Comparator.comparingDouble(RagChunk::getRelevanceScore).reversed())
                .limit(query.getTopN())
                .toList();
    }

    private RagChunk buildLineStatsChunk(RagQuery query) {
        List<LineDefectRangeProjection> rows = inspectionRepository.findLineDefectStatsByRange(
                query.getFrom(), query.getTo(), query.getLineId());

        if (rows.isEmpty()) {
            return emptyChunk("LINE_STATS", RagChunk.ChunkType.LINE_STATS);
        }

        StringBuilder sb = new StringBuilder("[라인별 불량 현황]\n");
        double maxRate = 0;
        for (LineDefectRangeProjection row : rows) {
            long total = row.getInspectionCount();
            long defects = row.getDefectCount();
            double rate = total > 0 ? (double) defects / total * 100 : 0;
            maxRate = Math.max(maxRate, rate);
            sb.append(String.format("- %s: 검사 %d건, 불량 %d건, 불량률 %.1f%%%n",
                    row.getLineName(), total, defects, rate));
        }

        double score = 5.0 + (query.getLineId() != null ? 8.0 : 0) + (maxRate > 15 ? 5.0 : 0);
        return RagChunk.builder()
                .chunkId("LINE_STATS")
                .type(RagChunk.ChunkType.LINE_STATS)
                .content(sb.toString())
                .relevanceScore(score)
                .build();
    }

    private RagChunk buildShiftStatsChunk(RagQuery query) {
        List<ShiftDefectRangeProjection> rows = inspectionRepository.findShiftDefectStatsByRange(
                query.getFrom(), query.getTo(), query.getLineId());

        if (rows.isEmpty()) {
            return emptyChunk("SHIFT_STATS", RagChunk.ChunkType.SHIFT_STATS);
        }

        StringBuilder sb = new StringBuilder("[교대조별 불량 현황]\n");
        double maxRate = 0;
        for (ShiftDefectRangeProjection row : rows) {
            long total = row.getInspectionCount();
            long defects = row.getDefectCount();
            double rate = total > 0 ? (double) defects / total * 100 : 0;
            maxRate = Math.max(maxRate, rate);
            sb.append(String.format("- %s (%s~%s): 검사 %d건, 불량 %d건, 불량률 %.1f%%%n",
                    row.getShiftName(), row.getStartTime(), row.getEndTime(), total, defects, rate));
        }

        double score = 4.0 + (maxRate > 15 ? 5.0 : 0);
        return RagChunk.builder()
                .chunkId("SHIFT_STATS")
                .type(RagChunk.ChunkType.SHIFT_STATS)
                .content(sb.toString())
                .relevanceScore(score)
                .build();
    }

    private RagChunk buildHourStatsChunk(RagQuery query) {
        List<HourlyDefectProjection> rows = inspectionRepository.findHourlyDefectStatsByRange(
                query.getFrom(), query.getTo(), query.getLineId());

        if (rows.isEmpty()) {
            return emptyChunk("HOUR_STATS", RagChunk.ChunkType.HOUR_STATS);
        }

        // 불량 집중 시간대 Top5
        List<HourlyDefectProjection> top5 = rows.stream()
                .filter(r -> r.getDefectCount() > 0)
                .sorted(Comparator.comparingLong(HourlyDefectProjection::getDefectCount).reversed())
                .limit(5)
                .toList();

        if (top5.isEmpty()) {
            return emptyChunk("HOUR_STATS", RagChunk.ChunkType.HOUR_STATS);
        }

        StringBuilder sb = new StringBuilder("[시간대별 불량 집중 현황 (상위 5)]\n");
        for (HourlyDefectProjection row : top5) {
            double rate = row.getInspectionCount() > 0
                    ? (double) row.getDefectCount() / row.getInspectionCount() * 100 : 0;
            sb.append(String.format("- %02d시: 불량 %d건 (불량률 %.1f%%)%n",
                    row.getHour(), row.getDefectCount(), rate));
        }

        long maxDefects = top5.get(0).getDefectCount();
        double score = 6.0 + (maxDefects > 10 ? 4.0 : 0) + (query.getLineId() != null ? 3.0 : 0);
        return RagChunk.builder()
                .chunkId("HOUR_STATS")
                .type(RagChunk.ChunkType.HOUR_STATS)
                .content(sb.toString())
                .relevanceScore(score)
                .build();
    }

    private RagChunk buildDefectTypeChunk(RagQuery query) {
        List<DefectTypeRangeProjection> rows = inspectionRepository.findDefectTypeStatsByRange(
                query.getFrom(), query.getTo(), query.getLineId());

        if (rows.isEmpty()) {
            return emptyChunk("DEFECT_TYPE_STATS", RagChunk.ChunkType.DEFECT_TYPE_STATS);
        }

        long totalDefects = rows.stream().mapToLong(DefectTypeRangeProjection::getDefectCount).sum();
        StringBuilder sb = new StringBuilder("[불량 유형별 현황]\n");
        for (DefectTypeRangeProjection row : rows) {
            double pct = totalDefects > 0 ? (double) row.getDefectCount() / totalDefects * 100 : 0;
            sb.append(String.format("- %s: %d건 (전체 불량 중 %.1f%%)%n",
                    row.getDefectType(), row.getDefectCount(), pct));
        }

        double score = 5.0 + (rows.size() > 2 ? 3.0 : 0);
        return RagChunk.builder()
                .chunkId("DEFECT_TYPE_STATS")
                .type(RagChunk.ChunkType.DEFECT_TYPE_STATS)
                .content(sb.toString())
                .relevanceScore(score)
                .build();
    }

    private RagChunk buildWeeklyTrendChunk(RagQuery query) {
        List<WeeklyDefectProjection> rows = inspectionRepository.findWeeklyDefectStatsByRange(
                query.getFrom(), query.getTo(), query.getLineId());

        if (rows.size() < 2) {
            return emptyChunk("WEEKLY_TREND", RagChunk.ChunkType.WEEKLY_TREND);
        }

        StringBuilder sb = new StringBuilder("[주별 불량률 추이]\n");
        double prevRate = -1;
        double maxIncrease = 0;
        int weekNum = 1;
        for (WeeklyDefectProjection row : rows) {
            long total = row.getInspectionCount();
            long defects = row.getDefectCount();
            double rate = total > 0 ? (double) defects / total * 100 : 0;
            String trend = "";
            if (prevRate >= 0) {
                double change = rate - prevRate;
                trend = change > 0
                        ? String.format(" (▲%.1f%p)", change)
                        : String.format(" (▼%.1f%p)", Math.abs(change));
                maxIncrease = Math.max(maxIncrease, change);
            }
            sb.append(String.format("- %d주차 (YEARWEEK=%s): 불량률 %.1f%%%s%n",
                    weekNum++, row.getWeek(), rate, trend));
            prevRate = rate;
        }

        double score = 4.0 + (maxIncrease > 5 ? 5.0 : 0);
        return RagChunk.builder()
                .chunkId("WEEKLY_TREND")
                .type(RagChunk.ChunkType.WEEKLY_TREND)
                .content(sb.toString())
                .relevanceScore(score)
                .build();
    }

    private RagChunk emptyChunk(String chunkId, RagChunk.ChunkType type) {
        return RagChunk.builder()
                .chunkId(chunkId)
                .type(type)
                .content("")
                .relevanceScore(0)
                .build();
    }
}