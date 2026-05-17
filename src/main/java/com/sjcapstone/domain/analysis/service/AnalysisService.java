package com.sjcapstone.domain.analysis.service;

import com.sjcapstone.domain.analysis.dto.*;
import org.springframework.data.domain.Pageable;

public interface AnalysisService {

    AnalysisStartResponse startAnalysis();

    AnalysisPageResponse getAnalysisList(Pageable pageable);

    AnalysisResponse getAnalysis(Long analysisId);

    AnalysisResponse getLatestAnalysis();

    void processCallback(Long analysisId, ProcessAnalysisCallbackRequest request);
}