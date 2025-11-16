package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Email_Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long email_id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false,length = 500)
    private String subject;

    @Column(nullable = false)
    private LocalDateTime sent_at;

    @Column(nullable = false,length = 50)
    private String status;


    @PrePersist
    protected void onCreate() {
        sent_at = LocalDateTime.now();
    }
}
