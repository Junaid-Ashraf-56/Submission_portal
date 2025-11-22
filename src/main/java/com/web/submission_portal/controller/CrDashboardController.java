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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/cr")
@Controller
@PreAuthorize("hasAuthority('ROLE_CR')")
public class CrDashboardController {
    private final StudentRepository studentRepository;
    private final UserRepository  userRepository;

    public CrDashboardController(StudentRepository studentRepository,
                                 UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String crDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        model.addAttribute("crName",student.getName());

        return "cr/dashboard";
    }

    @GetMapping("profile")
    public String crProfile(Model model, Authentication authentication) {
        authenticateUser(model, authentication);
        return "cr/profile";
    }

    @PostMapping("profile-info-change")
    public String profileInfoChange(Student updatedStudent, Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        student.setName(updatedStudent.getName());
        student.setRollNo(updatedStudent.getRollNo());
        student.setPhoneNumber(updatedStudent.getPhoneNumber());
        student.setGender(updatedStudent.getGender());

        studentRepository.save(student);

        return "redirect:/cr/profile?updated=true";
    }

    @GetMapping("/profile-info-change")
    public String handleGetOnProfileChange(Model model, Authentication authentication) {
        authenticateUser(model, authentication);
        return "cr/profile-info-change";
    }

    private void authenticateUser(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        model.addAttribute("crName",student.getName());
        model.addAttribute("crRollNo",student.getRollNo());
        model.addAttribute("crPhoneNumber",student.getPhoneNumber());
        model.addAttribute("crGender",student.getGender());
        model.addAttribute("crEmail",email);
    }
}
