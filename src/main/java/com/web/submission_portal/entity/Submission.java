package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "assignment_id"}))
@AllArgsConstructor
@NoArgsConstructor


public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long submission_id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false,length = 500)
    private String file_path;

    @Column(nullable = false)
    private LocalDateTime submitted_at;

    @Column(nullable = false)
    private String file_name;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private int file_size;

    @PrePersist
    protected void onCreate() {
        submitted_at = LocalDateTime.now();
    }

}
