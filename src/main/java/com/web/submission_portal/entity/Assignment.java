package com.web.submission_portal.entity;

import com.web.submission_portal.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 50)
    private String subjectCode;

    @Column(nullable = false, length = 200)
    private String subjectTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentType assignmentType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "assignment",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Submission> submissions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}