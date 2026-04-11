package com.sjcapstone.domain.line.repository;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.entity.LineCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, Long> {

    boolean existsByLineCode(LineCode lineCode);

    List<Line> findAllByIsActiveTrueOrderByIdAsc();

    Optional<Line> findByIdAndIsActiveTrue(Long id);
}
