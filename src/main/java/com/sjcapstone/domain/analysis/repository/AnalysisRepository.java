package com.sjcapstone.domain.analysis.repository;

import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<ProcessAnalysis, Long> {

    // 기존 단순 분석용
    Optional<ProcessAnalysis> findTopByOrderByCreatedAtDesc();

    // RAG 공정 분석 전용
    Optional<ProcessAnalysis> findByIdAndRagUsedTrue(Long id);

    Optional<ProcessAnalysis> findTopByRagUsedTrueOrderByCreatedAtDesc();

    Page<ProcessAnalysis> findByRagUsedTrueOrderByCreatedAtDesc(Pageable pageable);
}