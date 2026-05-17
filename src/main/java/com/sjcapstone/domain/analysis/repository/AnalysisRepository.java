package com.sjcapstone.domain.analysis.repository;

import com.sjcapstone.domain.analysis.entity.ProcessAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<ProcessAnalysis, Long> {

    Optional<ProcessAnalysis> findTopByOrderByCreatedAtDesc();
}