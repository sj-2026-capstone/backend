package com.sjcapstone.domain.user.entity;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "employee_Id", nullable = false, unique = true, length = 50)
    private UUID employeeId;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(String userName,
                UUID employeeId,
                String email,
                String phone,
                UserRole role,
                Shift shift,
                Line line,
                UserStatus status) {
        this.userName = userName;
        this.employeeId = employeeId;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.shift = shift;
        this.line = line;
        this.status = status;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String userName,
                       String email,
                       String phone,
                       UserRole role,
                       Shift shift,
                       Line line,
                       UserStatus status) {
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.shift = shift;
        this.line = line;
        this.status = status;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = UserStatus.INACTIVE;
    }
}
