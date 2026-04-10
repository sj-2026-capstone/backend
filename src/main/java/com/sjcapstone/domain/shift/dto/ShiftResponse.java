package com.sjcapstone.domain.shift.dto;

import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.entity.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class ShiftResponse {

    private Long shiftId;
    private ShiftType shiftType;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer shiftOrder;
    private Boolean isActive;

    public static ShiftResponse from(Shift shift) {
        return ShiftResponse.builder()
                .shiftId(shift.getId())
                .shiftType(shift.getShiftType())
                .shiftName(shift.getShiftName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .shiftOrder(shift.getShiftOrder())
                .isActive(shift.getIsActive())
                .build();
    }
}