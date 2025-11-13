package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Assignment_Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "assignment")
@NoArgsConstructor
@AllArgsConstructor

public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int assignment_id;

    @Column(nullable = false)
    private String subject_code;

    @Column(nullable = false)
    private String subject_title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Timestamp start_time;

    @Column(nullable = false)
    private Timestamp end_time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Assignment_Type assignment_type;
}
