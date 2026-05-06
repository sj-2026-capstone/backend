package com.sjcapstone.internal.callback.dto;

import com.sjcapstone.domain.inspection.entity.DefectType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalysisCallbackRequest {

    @NotNull(message = "불량 여부는 필수입니다.")
    private Boolean hasDefect;

    private DefectType defectType;

    private String resultNote;
}