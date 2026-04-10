package com.sjcapstone.domain.shift.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ShiftUpdateRequest {

    @NotBlank(message = "교대조 이름은 필수입니다.")
    @Size(max = 50, message = "교대조 이름은 50자 이하여야 합니다.")
    private String shiftName;

    @NotNull(message = "시작 시각은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "종료 시각은 필수입니다.")
    private LocalTime endTime;

    @NotNull(message = "순서는 필수입니다.")
    private Integer shiftOrder;
}