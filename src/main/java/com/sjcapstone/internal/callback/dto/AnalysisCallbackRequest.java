package com.sjcapstone.internal.callback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalysisCallbackRequest {

    @NotNull(message = "예측 결과는 필수입니다.")
    private String prediction;  // "NORMAL" or "DEFECT"

    private String gradCamImageUrl;

    public boolean isDefect() {
        return "DEFECT".equalsIgnoreCase(prediction);
    }

    public String getNormalizedGradCamImageUrl() {
        if (gradCamImageUrl == null) return null;
        return gradCamImageUrl.replace("grad_cam-images/", "grad_cam_images/");
    }
}