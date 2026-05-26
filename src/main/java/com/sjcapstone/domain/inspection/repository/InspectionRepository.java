package com.sjcapstone.domain.inspection.repository;

import com.sjcapstone.domain.dashboard.dto.projection.DailyDefectStatsProjection;
import com.sjcapstone.domain.dashboard.dto.projection.LineDefectStatsProjection;
import com.sjcapstone.domain.inspection.entity.ActionStatus;
import com.sjcapstone.domain.inspection.entity.Inspection;
import com.sjcapstone.domain.inspection.entity.InspectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    Page<Inspection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Inspection> findAllByLineIdOrderByCreatedAtDesc(Long lineId, Pageable pageable);

    Page<Inspection> findAllByStatusOrderByCreatedAtDesc(InspectionStatus status, Pageable pageable);

    Page<Inspection> findAllByLineIdAndStatusOrderByCreatedAtDesc(Long lineId, InspectionStatus status, Pageable pageable);

    List<Inspection> findTop5ByHasDefectTrueAndStatusOrderByInspectedAtDesc(InspectionStatus status);

    List<Inspection> findTop5ByLineIdAndHasDefectTrueAndStatusOrderByInspectedAtDesc(Long lineId, InspectionStatus status);

    boolean existsByLineIdAndHasDefectTrueAndActionStatus(Long lineId, ActionStatus actionStatus);

    Optional<Inspection> findTopByLineIdAndStatusOrderByInspectedAtDesc(Long lineId, InspectionStatus status);

    @Query("SELECT i FROM Inspection i LEFT JOIN FETCH i.line LEFT JOIN FETCH i.shift " +
           "WHERE i.status = :status AND i.createdAt >= :after ORDER BY i.createdAt DESC")
    List<Inspection> findDoneInspectionsForAnalysis(@Param("status") InspectionStatus status,
                                                    @Param("after") LocalDateTime after);

    // 대시보드 집계 쿼리

    long countByHasDefectTrue();

    long countByActionStatus(ActionStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.createdAt >= :start AND i.createdAt < :end AND i.hasDefect = true")
    long countByCreatedAtBetweenAndHasDefectTrue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT DATE(created_at) AS date,
                   COUNT(*) AS inspectionCount,
                   SUM(CASE WHEN has_defect = 1 THEN 1 ELSE 0 END) AS defectCount
            FROM inspections
            WHERE created_at >= :startDate
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) ASC
            """, nativeQuery = true)
    List<DailyDefectStatsProjection> findDailyDefectStatsSince(@Param("startDate") LocalDateTime startDate);

    @Query(value = """
            SELECT line_id AS lineId,
                   COUNT(*) AS inspectionCount,
                   SUM(CASE WHEN has_defect = 1 THEN 1 ELSE 0 END) AS defectCount
            FROM inspections
            GROUP BY line_id
            """, nativeQuery = true)
    List<LineDefectStatsProjection> findLineDefectStats();
}