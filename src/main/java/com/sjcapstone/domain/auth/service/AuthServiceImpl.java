package com.sjcapstone.domain.auth.service;

import com.sjcapstone.domain.auth.dto.ChangePasswordRequest;
import com.sjcapstone.domain.auth.dto.LoginRequest;
import com.sjcapstone.domain.auth.dto.LoginResponse;
import com.sjcapstone.domain.auth.dto.MeResponse;
import com.sjcapstone.domain.auth.entity.Auth;
import com.sjcapstone.domain.auth.exception.AuthNotFoundException;
import com.sjcapstone.domain.auth.exception.InvalidPasswordException;
import com.sjcapstone.domain.auth.exception.PasswordConfirmMismatchException;
import com.sjcapstone.domain.auth.repository.AuthRepository;
import com.sjcapstone.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Auth auth = authRepository.findByLoginId(request.getLoginId())
                .orElseThrow(AuthNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
            throw new InvalidPasswordException();
        }

        String token = jwtProvider.createToken(
                auth.getUser().getId(),
                auth.getLoginId(),
                auth.getUser().getRole()
        );

        return LoginResponse.of(
                token,
                auth.getUser().getId(),
                auth.getUser().getUserName(),
                auth.getLoginId(),
                auth.getUser().getRole().name(),
                auth.isPasswordChangeRequired()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponse getMe(Long userId) {
        Auth auth = authRepository.findByUserId(userId)
                .orElseThrow(AuthNotFoundException::new);

        return MeResponse.from(auth);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Auth auth = authRepository.findByUserId(userId)
                .orElseThrow(AuthNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), auth.getPassword())) {
            throw new InvalidPasswordException();
        }

        validatePasswordConfirmation(request.getNewPassword(), request.getConfirmPassword());

        auth.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordConfirmMismatchException();
        }
    }
}
