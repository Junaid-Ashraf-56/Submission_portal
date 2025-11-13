package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Email_Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int email_id;

    @Column(nullable = false)
    private String recepient;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Column(nullable = false)
    private String status;


}
