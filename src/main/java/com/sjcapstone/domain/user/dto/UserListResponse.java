package com.sjcapstone.domain.user.dto;

import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserListResponse {

    private Long userId;
    private UUID employeeId;
    private String userName;
    private String email;
    private UserRole role;
    private Long shiftId;
    private String shiftName;
    private UserStatus status;

    public static UserListResponse from(User user) {
        return UserListResponse.builder()
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .role(user.getRole())
                .shiftId(user.getShift() != null ? user.getShift().getId() : null)
                .shiftName(user.getShift() != null ? user.getShift().getShiftName() : null)
                .status(user.getStatus())
                .build();
    }
}
