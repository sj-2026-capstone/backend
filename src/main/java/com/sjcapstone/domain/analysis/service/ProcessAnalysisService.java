package com.sjcapstone.domain.analysis.service;

import com.sjcapstone.domain.analysis.dto.ProcessAnalysisPageResponse;
import com.sjcapstone.domain.analysis.dto.ProcessAnalysisResponse;
import com.sjcapstone.domain.analysis.dto.ProcessAnalysisStartRequest;
import org.springframework.data.domain.Pageable;

public interface ProcessAnalysisService {

    ProcessAnalysisResponse startAnalysis(ProcessAnalysisStartRequest request);

    ProcessAnalysisResponse getAnalysis(Long analysisId);

    ProcessAnalysisResponse getLatestAnalysis();

    ProcessAnalysisPageResponse getAnalysisHistory(Pageable pageable);
}