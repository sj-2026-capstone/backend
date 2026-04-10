package com.sjcapstone.domain.shift.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ShiftAssignmentRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "교대조 ID는 필수입니다.")
    private Long shiftId;

    @NotNull(message = "근무 날짜는 필수입니다.")
    private LocalDate workDate;
}