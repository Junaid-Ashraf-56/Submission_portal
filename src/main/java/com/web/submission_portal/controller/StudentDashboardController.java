package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/student")
@Controller

@PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_CR')")
public class StudentDashboardController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    StudentDashboardController(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public String studentDashboard(Model model, Authentication authentication) {

        String email = authentication.getName();
        System.out.println(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        model.addAttribute("studentName", student.getName());
        model.addAttribute("studentRollNo", student.getRollNo());

        return "student/dashboard";
    }
}
