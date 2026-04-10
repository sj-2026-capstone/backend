package com.sjcapstone.domain.auth.repository;

import com.sjcapstone.domain.auth.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByEmail(String email);

    boolean existsByUserId(Long userId);
}