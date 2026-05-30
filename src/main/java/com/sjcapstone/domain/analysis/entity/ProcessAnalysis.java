package com.sjcapstone.domain.analysis.entity;

import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "process_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    @Column(columnDefinition = "TEXT")
    private String patterns;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "error_message")
    private String errorMessage;

    // 공정 분석 메타데이터 (RAG 기반 분석 시 사용)
    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "filter_line_id")
    private Long filterLineId;

    @Column(name = "total_inspection_count")
    private int totalInspectionCount;

    @Column(name = "total_defect_count")
    private int totalDefectCount;

    @Column(name = "model_name", length = 50)
    private String modelName;

    @Column(name = "rag_used")
    private Boolean ragUsed;

    public static ProcessAnalysis create() {
        ProcessAnalysis analysis = new ProcessAnalysis();
        analysis.status = AnalysisStatus.PENDING;
        return analysis;
    }

    public static ProcessAnalysis createForProcess(LocalDate fromDate, LocalDate toDate,
                                                   Long filterLineId, String modelName) {
        ProcessAnalysis analysis = new ProcessAnalysis();
        analysis.status = AnalysisStatus.PENDING;
        analysis.fromDate = fromDate;
        analysis.toDate = toDate;
        analysis.filterLineId = filterLineId;
        analysis.modelName = modelName;
        analysis.ragUsed = true;
        return analysis;
    }

    public void startProcessing() {
        this.status = AnalysisStatus.PROCESSING;
    }

    public void updateStats(int totalInspectionCount, int totalDefectCount) {
        this.totalInspectionCount = totalInspectionCount;
        this.totalDefectCount = totalDefectCount;
    }

    public void complete(String patterns, String recommendations) {
        this.status = AnalysisStatus.DONE;
        this.patterns = patterns;
        this.recommendations = recommendations;
        this.analyzedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}