package com.web.submission_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "submission")
@AllArgsConstructor
@NoArgsConstructor

public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int submission_id;

    @Column(nullable = false)
    private String file_path;

    @Column(nullable = false)
    private Timestamp submitted_at;

    @Column(nullable = false)
    private String file_name;

    @Column(nullable = false)
    private String fileUrl;


    private int file_size;

}
