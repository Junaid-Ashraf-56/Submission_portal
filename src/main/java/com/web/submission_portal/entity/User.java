package com.web.submission_portal.entity;

import com.web.submission_portal.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean is_first_login;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 40)
    private Role role;

    @Column(nullable = false,updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    private Student student;
}
