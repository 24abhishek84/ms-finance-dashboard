package com.finance.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempt", indexes = {
    @Index(name = "idx_login_email_time", columnList = "email, attempt_time"),
    @Index(name = "idx_login_ip_time", columnList = "ip_address, attempt_time"),
    @Index(name = "idx_login_success", columnList = "success")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private LocalDateTime attemptTime;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String geoLocation;

    @Column(length = 255)
    private String failureReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (attemptTime == null) {
            attemptTime = LocalDateTime.now();
        }
    }
}
