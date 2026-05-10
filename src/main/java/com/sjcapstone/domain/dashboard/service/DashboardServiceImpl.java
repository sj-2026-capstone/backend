package com.sjcapstone.domain.dashboard.service;

import com.sjcapstone.domain.dashboard.dto.*;
import com.sjcapstone.domain.dashboard.dto.projection.DailyDefectStatsProjection;
import com.sjcapstone.domain.dashboard.dto.projection.LineDefectStatsProjection;
import com.sjcapstone.domain.inspection.repository.InspectionRepository;
import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final InspectionRepository inspectionRepository;
    private final LineRepository lineRepository;

    @Override
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        long totalInspectionCount = inspectionRepository.count();
        long totalDefectCount = inspectionRepository.countByHasDefectTrue();
        double defectRate = totalInspectionCount == 0 ? 0.0
                : Math.round((double) totalDefectCount / totalInspectionCount * 100 * 10) / 10.0;

        long todayInspectionCount = inspectionRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long todayDefectCount = inspectionRepository.countByCreatedAtBetweenAndHasDefectTrue(startOfToday, endOfToday);

        // 최근 7일 vs 이전 7일 비교
        LocalDateTime startOfCurrentPeriod = today.minusDays(6).atStartOfDay();
        LocalDateTime startOfPreviousPeriod = today.minusDays(13).atStartOfDay();

        long currentPeriodCount = inspectionRepository.countByCreatedAtBetween(startOfCurrentPeriod, endOfToday);
        long previousPeriodCount = inspectionRepository.countByCreatedAtBetween(startOfPreviousPeriod, startOfCurrentPeriod);

        double totalInspectionChangeRate = previousPeriodCount == 0 ? 0.0
                : Math.round((double) (currentPeriodCount - previousPeriodCount) / previousPeriodCount * 100 * 10) / 10.0;

        long currentPeriodDefects = inspectionRepository.countByCreatedAtBetweenAndHasDefectTrue(startOfCurrentPeriod, endOfToday);
        long previousPeriodDefects = inspectionRepository.countByCreatedAtBetweenAndHasDefectTrue(startOfPreviousPeriod, startOfCurrentPeriod);

        double currentDefectRate = currentPeriodCount == 0 ? 0.0 : (double) currentPeriodDefects / currentPeriodCount * 100;
        double previousDefectRate = previousPeriodCount == 0 ? 0.0 : (double) previousPeriodDefects / previousPeriodCount * 100;
        double defectRateChange = Math.round((currentDefectRate - previousDefectRate) * 10) / 10.0;

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalInspectionCount(totalInspectionCount)
                .totalInspectionChangeRate(totalInspectionChangeRate)
                .defectRate(defectRate)
                .defectRateChange(defectRateChange)
                .todayInspectionCount(todayInspectionCount)
                .todayDefectCount(todayDefectCount)
                .build();

        // 최근 7일 일별 불량률 추이
        List<DailyDefectStatsProjection> dailyStats =
                inspectionRepository.findDailyDefectStatsSince(startOfCurrentPeriod);

        Map<LocalDate, DailyDefectStatsProjection> dailyStatsMap = dailyStats.stream()
                .collect(Collectors.toMap(s -> s.getDate().toLocalDate(), s -> s));

        List<DefectRateTrendItemResponse> defectRateTrend = IntStream.range(0, 7)
                .mapToObj(i -> today.minusDays(6 - i))
                .map(date -> {
                    DailyDefectStatsProjection stats = dailyStatsMap.get(date);
                    long dayInspection = stats != null ? stats.getInspectionCount() : 0L;
                    long dayDefect = stats != null ? stats.getDefectCount() : 0L;
                    return DefectRateTrendItemResponse.of(date, dayInspection, dayDefect);
                })
                .collect(Collectors.toList());

        // 라인별 불량률
        List<LineDefectStatsProjection> lineStats = inspectionRepository.findLineDefectStats();
        Map<Long, LineDefectStatsProjection> lineStatsMap = lineStats.stream()
                .collect(Collectors.toMap(LineDefectStatsProjection::getLineId, s -> s));

        List<Line> lines = lineRepository.findAll();
        List<LineDefectRateResponse> lineDefectRates = lines.stream()
                .map(line -> {
                    LineDefectStatsProjection stats = lineStatsMap.get(line.getId());
                    long lineInspection = stats != null ? stats.getInspectionCount() : 0L;
                    long lineDefect = stats != null ? stats.getDefectCount() : 0L;
                    return LineDefectRateResponse.of(line, lineInspection, lineDefect);
                })
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .summary(summary)
                .defectRateTrend(defectRateTrend)
                .actionSummary(ActionSummaryResponse.empty())
                .lineDefectRates(lineDefectRates)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }
}