package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int user_id;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean is_first_login;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 40)
    private Role role;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    private Student student;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Password_Reset_T> passwordResetTokens;

}
