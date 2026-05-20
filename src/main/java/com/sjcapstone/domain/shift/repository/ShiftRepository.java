package com.sjcapstone.domain.shift.repository;

import com.sjcapstone.domain.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findAllByIsActiveTrue();

    Optional<Shift> findByIdAndIsActiveTrue(Long id);

    // 야간 교대조(자정 넘김) 포함하여 현재 시간에 해당하는 교대조 조회
    @Query("SELECT s FROM Shift s WHERE s.isActive = true AND (" +
           "  (s.startTime <= s.endTime AND s.startTime <= :now AND :now < s.endTime) OR" +
           "  (s.startTime > s.endTime AND (s.startTime <= :now OR :now < s.endTime))" +
           ")")
    Optional<Shift> findCurrentShift(@Param("now") LocalTime now);
}