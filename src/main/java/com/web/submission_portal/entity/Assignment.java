package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Assignment_Type;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "assignments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long assignment_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User created_by;

    @Column(nullable = false,length = 50)
    private String subject_code;

    @Column(nullable = false,length = 100)
    private String subject_title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime start_time;

    @Column(nullable = false)
    private LocalDateTime end_time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 50)
    private Assignment_Type assignment_type;

    @Column( nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Submission> submissions;
}
