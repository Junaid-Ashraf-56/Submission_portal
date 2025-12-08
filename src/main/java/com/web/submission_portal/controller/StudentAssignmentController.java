package com.web.submission_portal.controller;

import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/student")
@Controller
@PreAuthorize("hasAnyAuthority('ROLE_CR','ROLE_STUDENT')")
public class StudentAssignmentController {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public StudentAssignmentController(StudentRepository studentRepository,
                                       UserRepository userRepository,
                                       AssignmentRepository assignmentRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, Model model, Authentication authentication){
        CrAssignmentController.getAssignmentForBoth(id, model, authentication, assignmentRepository, userRepository, studentRepository);

        return "student/submit-assignment";
    }
}
