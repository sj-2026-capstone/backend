package com.sjcapstone.domain.inspection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InspectionCreateRequest {

    @NotNull(message = "라인 ID는 필수입니다.")
    private Long lineId;

    private Long workerId;

    private Long shiftId;

    private String imageUrl;
}