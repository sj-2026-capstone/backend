package com.sjcapstone.domain.analysis.entity;

import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static ProcessAnalysis create() {
        ProcessAnalysis analysis = new ProcessAnalysis();
        analysis.status = AnalysisStatus.PENDING;
        return analysis;
    }

    public void startProcessing() {
        this.status = AnalysisStatus.PROCESSING;
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