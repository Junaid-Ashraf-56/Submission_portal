package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.SubmissionRepository;
import com.web.submission_portal.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/student")
@Controller

@PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_CR')")
public class StudentDashboardController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    StudentDashboardController(UserRepository userRepository,
                               StudentRepository studentRepository,
                               AssignmentRepository assignmentRepository,
                               SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/dashboard")
    public String studentDashboard(Model model, Authentication authentication) {

        //This is for the student name and others things
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();
        model.addAttribute("studentName", student.getName());
        model.addAttribute("studentRollNo", student.getRollNo());

        //These for the assignment submitted by the student
        List<Assignment> assignments = assignmentRepository.findAll();
        model.addAttribute("assignments", assignments);


        //This is for the submission of the student
        Map<Long,Boolean> submissionStatus = new HashMap<>();
        for (Assignment assignment : assignments) {
            boolean hasSubmitted = submissionRepository.existsByStudentAndAssignment(student,assignment);
            submissionStatus.put(assignment.getAssignmentId(), hasSubmitted);
        }

        model.addAttribute("assignments",assignments);
        model.addAttribute("submissionStatus",submissionStatus);
        model.addAttribute("studentName",student.getName());
        return "student/dashboard";
    }

    @GetMapping("/student-profile")
    public String studentProfile(Model model, Authentication authentication) {
        authenticateUser(model, authentication);
        return "student/student-profile";
    }

    @GetMapping("/profile-info-change")
    public String profileInfoChange(Model model, Authentication authentication) {
        authenticateUser(model, authentication);
        return "student/profile-info-change";
    }

    @PostMapping("/profile-info-change")
    public String updateProfile(Student updatedStudent,Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        student.setRollNo(updatedStudent.getRollNo());
        student.setName(updatedStudent.getName());
        student.setPhoneNumber(updatedStudent.getPhoneNumber());
        student.setGender(updatedStudent.getGender());

        studentRepository.save(student);

        return "redirect:/student/profile-info-change?updated=true";
    }

    private void authenticateUser(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        model.addAttribute("studentName", student.getName());
        model.addAttribute("studentRollNo", student.getRollNo());
        model.addAttribute("phoneNumber", student.getPhoneNumber());
        model.addAttribute("email", email);
        model.addAttribute("gender", student.getGender());
    }
}
