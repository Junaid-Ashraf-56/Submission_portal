package com.web.submission_portal.controller;

import com.web.submission_portal.entity.*;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Gender;
import com.web.submission_portal.enums.Role;
import com.web.submission_portal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cr")
@RequiredArgsConstructor
public class CRController {

    private final StudentService studentService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final SubmissionService submissionService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/manage-students")
    public String manageStudentsPage(Model model, Authentication auth) {
        User crUser = userService.findByEmail(auth.getName());
        Student student = studentService.getByUserId(crUser.getUserId());
        List<Student> students = studentService.getStudentBySection(student.getSection());

        model.addAttribute("crName", student.getName());
        model.addAttribute("students", students);

        return "cr/manage-students";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam String name,
                             @RequestParam String rollNo,
                             @RequestParam String email,
                             @RequestParam(required = false) String phoneNumber,
                             @RequestParam(required = false) String section,
                             @RequestParam Gender gender,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            // Check if email or roll number already exists
            if (userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
                return "redirect:/cr/manage-students?error";
            }

            if (studentService.existsByRollNo(rollNo)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Roll number already exists!");
                return "redirect:/cr/manage-students?error";
            }

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("student123")) // Default password
                    .role(Role.ROLE_STUDENT)
                    .status(AccountStatus.APPROVED)
                    .build();

            User savedUser = userService.save(user);

            User crUser = userService.findByEmail(authentication.getName());
            Student crStudent = studentService.findByUserId(crUser.getUserId());


            Student student = new Student();
            student.setUser(savedUser);
            student.setName(name);
            student.setRollNo(rollNo);
            student.setPhoneNumber(phoneNumber);
            student.setSection(section);
            student.setUniversity(crStudent.getUniversity());
            student.setGender(gender);

            studentService.save(student);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Student added successfully! Default password: student123");
            return "redirect:/cr/manage-students?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to add student: " + e.getMessage());
            return "redirect:/cr/manage-students?error";
        }
    }

    @PostMapping("/students/update")
    public String updateStudent(@RequestParam Long userId,
                                @RequestParam String name,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam Gender gender,
                                @RequestParam(required = false) String section,
                                RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.findByUserId(userId);

            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student not found!");
                return "redirect:/cr/manage-students?error";
            }

            student.setName(name);
            student.setPhoneNumber(phoneNumber);
            student.setGender(gender);
            student.setSection(section);

            studentService.save(student);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Student updated successfully!");
            return "redirect:/cr/manage-students?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update student: " + e.getMessage());
            return "redirect:/cr/manage-students?error";
        }
    }

    @PostMapping("/students/delete")
    public String deleteStudent(@RequestParam Long userId,
                                RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.findByUserId(userId);

            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student not found!");
                return "redirect:/cr/manage-students?error";
            }

            List<Submission> submissions = submissionService.findByStudent(student);
            for (Submission submission : submissions) {
                submissionService.deleteById(submission.getSubmissionId());
            }
            Long studentId = student.getStudentId();
            studentService.deleteById(studentId);

            userService.deleteById(userId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Student deleted successfully!");
            return "redirect:/cr/manage-students?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete student: " + e.getMessage());
            return "redirect:/cr/manage-students?error";
        }
    }

    //From here assignment is managed by the student

    @GetMapping("/manage-assignments")
    public String manageAssignmentsPage(Model model, Authentication auth) {
        User crUser = userService.findByEmail(auth.getName());
        Student student = studentService.findByUserId(crUser.getUserId());
        List<Assignment> assignments = assignmentService.getAssignmentsByCreator(crUser);

        model.addAttribute("crName", student.getName());
        model.addAttribute("assignments", assignments);

        return "cr/manage-assignments";
    }

    @PostMapping("/assignments/delete")
    public String deleteAssignment(@RequestParam Long assignmentId,
                                   RedirectAttributes redirectAttributes) {
        try {
            Assignment assignment = assignmentService.findById(assignmentId);

            if (assignment == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Assignment not found!");
                return "redirect:/cr/dashboard?error";
            }

            assignmentService.deleteById(assignmentId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Assignment deleted successfully!");
            return "redirect:/cr/dashboard?deleted";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete assignment: " + e.getMessage());
            return "redirect:/cr/dashboard?error";
        }
    }

    @PostMapping("/assignments/update")
    public String updateAssignment(@RequestParam Long assignmentId,
                                   @RequestParam String subjectCode,
                                   @RequestParam String subjectTitle,
                                   @RequestParam String description,
                                   @RequestParam String startTime,
                                   @RequestParam String endTime,
                                   RedirectAttributes redirectAttributes) {
        try {
            Assignment assignment = assignmentService.findById(assignmentId);

            if (assignment == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Assignment not found!");
                return "redirect:/cr/manage-assignments?error";
            }

            assignment.setSubjectCode(subjectCode);
            assignment.setSubjectTitle(subjectTitle);
            assignment.setDescription(description);
            assignment.setStartTime(LocalDateTime.parse(startTime));
            assignment.setEndTime(LocalDateTime.parse(endTime));

            assignmentService.save(assignment);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Assignment updated successfully!");
            return "redirect:/cr/manage-assignments?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update assignment: " + e.getMessage());
            return "redirect:/cr/manage-assignments?error";
        }
    }
}