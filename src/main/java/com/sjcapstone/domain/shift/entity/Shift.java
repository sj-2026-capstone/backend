package com.sjcapstone.domain.shift.entity;

import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "shifts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 20)
    private ShiftType shiftType;

    @Column(name = "shift_name", nullable = false, length = 50)
    private String shiftName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "shift_order", nullable = false)
    private Integer shiftOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public Shift(ShiftType shiftType, String shiftName, LocalTime startTime, LocalTime endTime, Integer shiftOrder) {
        this.shiftType = shiftType;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftOrder = shiftOrder;
        this.isActive = true;
    }

    public void update(String shiftName, LocalTime startTime, LocalTime endTime, Integer shiftOrder) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftOrder = shiftOrder;
    }

    public void deactivate() {
        this.isActive = false;
    }
}