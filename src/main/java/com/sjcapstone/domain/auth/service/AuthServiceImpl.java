package com.sjcapstone.domain.auth.service;

import com.sjcapstone.domain.auth.dto.LoginRequest;
import com.sjcapstone.domain.auth.dto.LoginResponse;
import com.sjcapstone.domain.auth.dto.RegisterRequest;
import com.sjcapstone.domain.auth.entity.Auth;
import com.sjcapstone.domain.auth.exception.InvalidPasswordException;
import com.sjcapstone.domain.auth.repository.AuthRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserStatus;
import com.sjcapstone.domain.user.exception.DuplicateEmailException;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.repository.UserRepository;
import com.sjcapstone.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        User user = User.builder()
                .employeeId(UUID.randomUUID())
                .userName(request.getUserName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.PENDING)
                .build();

        userRepository.save(user);

        Auth auth = Auth.builder()
                .user(user)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        authRepository.save(auth);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Auth auth = authRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
            throw new InvalidPasswordException();
        }

        String token = jwtProvider.createToken(
                auth.getUser().getId(),
                auth.getEmail(),
                auth.getUser().getRole()
        );

        return LoginResponse.of(token);
    }
}