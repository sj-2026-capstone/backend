package com.sjcapstone.domain.line.entity;

import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "production_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Line extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_code", nullable = false, unique = true, length = 10)
    private LineCode lineCode;

    @Column(name = "line_name", nullable = false, length = 100)
    private String lineName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public Line(LineCode lineCode, String lineName, Boolean isActive) {
        this.lineCode = lineCode;
        this.lineName = lineName;
        this.isActive = isActive;
    }
}
