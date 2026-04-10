package com.sjcapstone.domain.shift.dto;

import com.sjcapstone.domain.shift.entity.ShiftAssignment;
import com.sjcapstone.domain.shift.entity.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ShiftAssignmentResponse {

    private Long assignmentId;
    private Long userId;
    private String userName;
    private Long shiftId;
    private String shiftName;
    private ShiftType shiftType;
    private LocalDate workDate;

    public static ShiftAssignmentResponse from(ShiftAssignment assignment) {
        return ShiftAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .userId(assignment.getUser().getId())
                .userName(assignment.getUser().getUserName())
                .shiftId(assignment.getShift().getId())
                .shiftName(assignment.getShift().getShiftName())
                .shiftType(assignment.getShift().getShiftType())
                .workDate(assignment.getWorkDate())
                .build();
    }
}