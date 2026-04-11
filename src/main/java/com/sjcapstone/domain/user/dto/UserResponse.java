package com.sjcapstone.domain.user.dto;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.entity.LineCode;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private UUID employeeId;
    private String userName;
    private String email;
    private String phone;
    private UserRole role;
    private Long shiftId;
    private String shiftName;
    private Long lineId;
    private LineCode lineCode;
    private String lineName;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        Line line = user.getLine();

        return UserResponse.builder()
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .shiftId(user.getShift() != null ? user.getShift().getId() : null)
                .shiftName(user.getShift() != null ? user.getShift().getShiftName() : null)
                .lineId(line != null ? line.getId() : null)
                .lineCode(line != null ? line.getLineCode() : null)
                .lineName(line != null ? line.getLineName() : null)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
