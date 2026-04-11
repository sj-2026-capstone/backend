package com.sjcapstone.domain.admin.dto;

import com.sjcapstone.domain.auth.entity.Auth;
import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.entity.LineCode;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminAccountListItemResponse {

    private Long userId;
    private String userName;
    private String loginId;
    private UserRole role;
    private UserStatus status;
    private Long shiftId;
    private String shiftName;
    private Long lineId;
    private LineCode lineCode;
    private String lineName;

    public static AdminAccountListItemResponse from(Auth auth) {
        User user = auth.getUser();
        Shift shift = user.getShift();
        Line line = user.getLine();

        return AdminAccountListItemResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .loginId(auth.getLoginId())
                .role(user.getRole())
                .status(user.getStatus())
                .shiftId(shift != null ? shift.getId() : null)
                .shiftName(shift != null ? shift.getShiftName() : null)
                .lineId(line != null ? line.getId() : null)
                .lineCode(line != null ? line.getLineCode() : null)
                .lineName(line != null ? line.getLineName() : null)
                .build();
    }
}
