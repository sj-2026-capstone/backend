package com.sjcapstone.domain.shift.repository;

import com.sjcapstone.domain.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findAllByIsActiveTrue();

    Optional<Shift> findByIdAndIsActiveTrue(Long id);
}