package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "password_reset_t")
@NoArgsConstructor
@AllArgsConstructor

public class Password_Reset_T {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int token_id;

    @Column(nullable = false)
    private int otp;

    @Column(nullable = false)
    private Timestamp expire_time;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private Timestamp created_at;
}
