package com.sjcapstone.domain.inspection.repository;

import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    Page<Inspection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Inspection> findAllByLineIdOrderByCreatedAtDesc(Long lineId, Pageable pageable);

    Page<Inspection> findAllByStatusOrderByCreatedAtDesc(InspectionStatus status, Pageable pageable);

    Page<Inspection> findAllByLineIdAndStatusOrderByCreatedAtDesc(Long lineId, InspectionStatus status, Pageable pageable);
}