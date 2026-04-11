package com.sjcapstone.domain.auth.service;

import com.sjcapstone.domain.auth.dto.ChangePasswordRequest;
import com.sjcapstone.domain.auth.dto.LoginRequest;
import com.sjcapstone.domain.auth.dto.LoginResponse;
import com.sjcapstone.domain.auth.dto.MeResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    MeResponse getMe(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);
}
