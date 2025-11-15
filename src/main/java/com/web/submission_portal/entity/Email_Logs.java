package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class Email_Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int email_id;

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
