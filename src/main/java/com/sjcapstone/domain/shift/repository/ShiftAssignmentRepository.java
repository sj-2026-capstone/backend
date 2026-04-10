package com.sjcapstone.domain.shift.repository;

import com.sjcapstone.domain.shift.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    boolean existsByUserIdAndWorkDate(Long userId, LocalDate workDate);

    Optional<ShiftAssignment> findByUserIdAndWorkDate(Long userId, LocalDate workDate);

    List<ShiftAssignment> findAllByWorkDate(LocalDate workDate);

    List<ShiftAssignment> findAllByUserId(Long userId);
}