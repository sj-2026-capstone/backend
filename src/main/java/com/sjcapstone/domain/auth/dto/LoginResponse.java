package com.sjcapstone.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private Long userId;
    private String userName;
    private String loginId;
    private String role;
    private boolean passwordChangeRequired;

    public static LoginResponse of(String accessToken,
                                   Long userId,
                                   String userName,
                                   String loginId,
                                   String role,
                                   boolean passwordChangeRequired) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .userId(userId)
                .userName(userName)
                .loginId(loginId)
                .role(role)
                .passwordChangeRequired(passwordChangeRequired)
                .build();
    }
}
