package com.sjcapstone.domain.admin.dto;

import com.sjcapstone.domain.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminAccountStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private UserStatus status;
}
