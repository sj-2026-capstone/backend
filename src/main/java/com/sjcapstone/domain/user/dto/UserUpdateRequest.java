package com.sjcapstone.domain.user.dto;

import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    private String userName;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;

    @NotNull(message = "역할은 필수입니다.")
    private UserRole role;

    private Long shiftId;

    private Long lineId;

    @NotNull(message = "상태는 필수입니다.")
    private UserStatus status;
}
