package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "students")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder

public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int student_id;

    @Column(unique = true,nullable = false,length = 100)
    private String roll_no;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(unique = true,length = 50)
    private String phone_number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 50)
    private Gender gender;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Submission> submissions;
}
