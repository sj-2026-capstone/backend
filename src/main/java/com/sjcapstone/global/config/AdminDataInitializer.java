package com.sjcapstone.global.config;

import com.sjcapstone.domain.auth.entity.Auth;
import com.sjcapstone.domain.auth.repository.AuthRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class AdminDataInitializer {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initializeAdminAccount() {
        return args -> {
            if (authRepository.existsByLoginId("admin01")) {
                return;
            }

            User admin = userRepository.save(User.builder()
                    .employeeId(UUID.randomUUID())
                    .userName("테스트 관리자")
                    .email("admin01@example.com")
                    .phone("010-0000-0000")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());

            authRepository.save(Auth.builder()
                    .user(admin)
                    .loginId("admin01")
                    .password(passwordEncoder.encode("password123"))
                    .passwordChangeRequired(false)
                    .build());
        };
    }
}
