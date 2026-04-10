package com.sjcapstone.domain.auth.service;

import com.sjcapstone.domain.auth.dto.LoginRequest;
import com.sjcapstone.domain.auth.dto.LoginResponse;
import com.sjcapstone.domain.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}