package com.sjcapstone.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginIdAvailabilityResponse {

    private String loginId;
    private boolean available;

    public static LoginIdAvailabilityResponse of(String loginId, boolean available) {
        return LoginIdAvailabilityResponse.builder()
                .loginId(loginId)
                .available(available)
                .build();
    }
}
