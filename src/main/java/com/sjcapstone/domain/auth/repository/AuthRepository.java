package com.sjcapstone.domain.auth.repository;

import com.sjcapstone.domain.auth.entity.Auth;
import com.sjcapstone.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByLoginId(String loginId);

    Optional<Auth> findByUserId(Long userId);

    boolean existsByLoginId(String loginId);

    boolean existsByUserId(Long userId);

    @Query(value = """
            select a from Auth a
            join a.user u
            where u.deletedAt is null
              and (:status is null or u.status = :status)
              and (
                    :keyword is null
                    or lower(u.userName) like lower(concat('%', :keyword, '%'))
                    or lower(a.loginId) like lower(concat('%', :keyword, '%'))
                  )
            """,
            countQuery = """
            select count(a) from Auth a
            join a.user u
            where u.deletedAt is null
              and (:status is null or u.status = :status)
              and (
                    :keyword is null
                    or lower(u.userName) like lower(concat('%', :keyword, '%'))
                    or lower(a.loginId) like lower(concat('%', :keyword, '%'))
                  )
            """)
    Page<Auth> searchAccounts(@Param("keyword") String keyword,
                              @Param("status") UserStatus status,
                              Pageable pageable);
}
