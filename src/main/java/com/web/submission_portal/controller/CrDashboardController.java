package com.web.submission_portal.controller;


import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.SubmissionRepository;
import com.web.submission_portal.repository.UserRepository;
import com.web.submission_portal.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@RequestMapping("/cr")
@Controller
@PreAuthorize("hasAuthority('ROLE_CR')")
@RequiredArgsConstructor
public class CrDashboardController {
    private final StudentRepository studentRepository;
    private final UserRepository  userRepository;
    private final AssignmentService assignmentService;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;


    //Dashboard and profile for the cr
    @GetMapping("/dashboard")
    public String crDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();

        //These all for user
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();
        model.addAttribute("crName",student.getName());

        //These for the assignment which will created by th cr
        CrAssignmentController.submissionDetails(model, user, student, assignmentService, submissionRepository, studentRepository);

        //These for the assignment submitted by the cr as student
        List<Assignment> assignments = assignmentRepository.findAssignmentsForSection(student.getSection());
        model.addAttribute("assignments", assignments);


        return "cr/dashboard";
    }
    @GetMapping("profile")
    public String crProfile(Model model, Authentication authentication) {
        authenticateUser(model, authentication);
        return "cr/profile";
    }


    //Profile editing with get and post methods
    @PostMapping("profile-info-change")
    public String profileInfoChange(
            Student updatedStudent,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        student.setName(updatedStudent.getName());
        student.setRollNo(updatedStudent.getRollNo());
        student.setPhoneNumber(updatedStudent.getPhoneNumber());
        student.setGender(updatedStudent.getGender());

        boolean classChanged =
                !student.getSection().equals(updatedStudent.getSection()) ||
                        !student.getSemester().equals(updatedStudent.getSemester());

        if (classChanged) {
            List<Student> myStudents = studentRepository
                    .getStudentsBySemesterAndSectionAndAdmissionAndProgram(
                            student.getSection(),
                            student.getSemester(),
                            student.getAdmission(),
                            student.getProgram());

            myStudents.forEach(stu -> {
                stu.setSection(updatedStudent.getSection());
                stu.setSemester(updatedStudent.getSemester());
                studentRepository.save(stu);
            });

            redirectAttributes.addFlashAttribute("successMessage",
                    "Class updated. " + myStudents.size() + " student(s) moved with you.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        }

        // Update CR
        student.setSection(updatedStudent.getSection());
        student.setSemester(updatedStudent.getSemester());
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
