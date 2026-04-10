package com.sjcapstone.domain.shift.entity;

import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "shift_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_work_date",
                columnNames = {"user_id", "work_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShiftAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Builder
    public ShiftAssignment(User user, Shift shift, LocalDate workDate) {
        this.user = user;
        this.shift = shift;
        this.workDate = workDate;
    }
}