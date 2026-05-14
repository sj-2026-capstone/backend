package com.sjcapstone.domain.inspection.entity;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private User worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InspectionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "defect_type", length = 30)
    private DefectType defectType;

    @Column(name = "has_defect")
    private Boolean hasDefect;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "grad_cam_image_url")
    private String gradCamImageUrl;

    @Column(name = "result_note", columnDefinition = "TEXT")
    private String resultNote;

    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;

    @Builder
    public Inspection(Line line, Shift shift, User worker, String imageUrl) {
        this.line = line;
        this.shift = shift;
        this.worker = worker;
        this.imageUrl = imageUrl;
        this.status = InspectionStatus.PENDING;
        this.hasDefect = false;
    }

    public void startProcessing() {
        this.status = InspectionStatus.PROCESSING;
    }

    public void complete(boolean hasDefect, DefectType defectType, String resultNote, String gradCamImageUrl) {
        this.status = InspectionStatus.DONE;
        this.hasDefect = hasDefect;
        this.defectType = defectType;
        this.resultNote = resultNote;
        this.gradCamImageUrl = gradCamImageUrl;
        this.inspectedAt = LocalDateTime.now();
    }

    public void fail(String resultNote) {
        this.status = InspectionStatus.FAILED;
        this.resultNote = resultNote;
        this.inspectedAt = LocalDateTime.now();
    }
}