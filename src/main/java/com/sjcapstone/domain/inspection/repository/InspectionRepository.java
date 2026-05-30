package com.sjcapstone.domain.inspection.repository;

import com.sjcapstone.domain.analysis.dto.projection.DefectTypeRangeProjection;
import com.sjcapstone.domain.analysis.dto.projection.HourlyDefectProjection;
import com.sjcapstone.domain.analysis.dto.projection.LineDefectRangeProjection;
import com.sjcapstone.domain.analysis.dto.projection.ShiftDefectRangeProjection;
import com.sjcapstone.domain.analysis.dto.projection.WeeklyDefectProjection;
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

    // RAG 분석용 집계 쿼리

    @Query(value = """
            SELECT i.line_id AS lineId, l.line_name AS lineName,
                   COUNT(*) AS inspectionCount,
                   SUM(CASE WHEN i.has_defect = 1 THEN 1 ELSE 0 END) AS defectCount
            FROM inspections i
            JOIN production_lines l ON i.line_id = l.line_id
            WHERE i.status = 'DONE'
              AND i.created_at BETWEEN :from AND :to
              AND (:lineId IS NULL OR i.line_id = :lineId)
            GROUP BY i.line_id, l.line_name
            ORDER BY defectCount DESC
            """, nativeQuery = true)
    List<LineDefectRangeProjection> findLineDefectStatsByRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lineId") Long lineId);

    @Query(value = """
            SELECT s.shift_name AS shiftName,
                   TIME_FORMAT(s.start_time, '%H:%i') AS startTime,
                   TIME_FORMAT(s.end_time, '%H:%i') AS endTime,
                   COUNT(*) AS inspectionCount,
                   SUM(CASE WHEN i.has_defect = 1 THEN 1 ELSE 0 END) AS defectCount
            FROM inspections i
            JOIN shifts s ON i.shift_id = s.shift_id
            WHERE i.status = 'DONE'
              AND i.created_at BETWEEN :from AND :to
              AND (:lineId IS NULL OR i.line_id = :lineId)
            GROUP BY i.shift_id, s.shift_name, s.start_time, s.end_time
            ORDER BY defectCount DESC
            """, nativeQuery = true)
    List<ShiftDefectRangeProjection> findShiftDefectStatsByRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lineId") Long lineId);

    @Query(value = """
            SELECT HOUR(i.created_at) AS hour,
                   COUNT(*) AS inspectionCount,
                   SUM(CASE WHEN i.has_defect = 1 THEN 1 ELSE 0 END) AS defectCount
            FROM inspections i
            WHERE i.status = 'DONE'
              AND i.created_at BETWEEN :from AND :to
              AND (:lineId IS NULL OR i.line_id = :lineId)
            GROUP BY HOUR(i.created_at)
            ORDER BY HOUR(i.created_at)
            """, nativeQuery = true)
    List<HourlyDefectProjection> findHourlyDefectStatsByRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lineId") Long lineId);

    @Query(value = """
            SELECT i.defect_type AS defectType, COUNT(*) AS defectCount
            FROM inspections i
            WHERE i.status = 'DONE'
              AND i.has_defect = 1
              AND i.created_at BETWEEN :from AND :to
              AND (:lineId IS NULL OR i.line_id = :lineId)
            GROUP BY i.defect_type
            ORDER BY defectCount DESC
            """, nativeQuery = true)
    List<DefectTypeRangeProjection> findDefectTypeStatsByRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lineId") Long lineId);

    @Query(value = """
            SELECT YEARWEEK(i.created_at, 1) AS week,
                   COUNT(*) AS inspectionCount,
                   SUM(CASE WHEN i.has_defect = 1 THEN 1 ELSE 0 END) AS defectCount
            FROM inspections i
            WHERE i.status = 'DONE'
              AND i.created_at BETWEEN :from AND :to
              AND (:lineId IS NULL OR i.line_id = :lineId)
            GROUP BY YEARWEEK(i.created_at, 1)
            ORDER BY week
            """, nativeQuery = true)
    List<WeeklyDefectProjection> findWeeklyDefectStatsByRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lineId") Long lineId);

    long countByStatusAndCreatedAtBetween(InspectionStatus status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.status = :status AND i.hasDefect = true AND i.createdAt BETWEEN :from AND :to")
    long countDefectsByStatusAndRange(@Param("status") InspectionStatus status,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);
}