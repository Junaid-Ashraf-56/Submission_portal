package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "password_reset_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Password_Reset_T {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int token_id;

    @Column(nullable = false,length = 6)
    private int otp;

    @Column(nullable = false)
    private LocalDateTime expire_time;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }
}
