package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "student")
@NoArgsConstructor
@Data
@AllArgsConstructor

public class Student {

    @Id
    @Column(name = "student_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int student_Id;

    @Column(unique = true,nullable = false)
    private String rollno;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String phone_number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
}
