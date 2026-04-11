package com.sjcapstone.domain.auth.entity;

import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean passwordChangeRequired;

    @Builder
    public Auth(User user, String loginId, String password, boolean passwordChangeRequired) {
        this.user = user;
        this.loginId = loginId;
        this.password = password;
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public void changePassword(String password) {
        this.password = password;
        this.passwordChangeRequired = false;
    }

    public void updateLoginId(String loginId) {
        this.loginId = loginId;
    }
}
