package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(unique = true, nullable = false, length = 20)
    private String rollNo;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 15)
    private String phoneNumber;

    @Column(nullable = false,length = 10)
    private String section;

    @Column(nullable = false,length = 100)
    private String university;

    @Column(nullable = false,length = 100)
    private String program;

    @Column(nullable = false,length = 50)
    private String semester;

    @Column(nullable = false,length = 100)
    private String admission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;



    @Transient
    private Integer studentCount;

    @Transient
    private String crName;

    @Transient
    private List<Student> students;
}