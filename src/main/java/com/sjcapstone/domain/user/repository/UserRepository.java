package com.sjcapstone.domain.user.repository;


import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByEmployeeIdAndDeletedAtIsNull(UUID employeeId);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmployeeIdAndDeletedAtIsNull(UUID employeeId);

    List<User> findAllByDeletedAtIsNull();

    List<User> findAllByStatusAndDeletedAtIsNull(UserStatus status);
}
