package com.web.submission_portal.controller;

import com.web.submission_portal.entity.*;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Gender;
import com.web.submission_portal.enums.Role;
import com.web.submission_portal.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cr")
@RequiredArgsConstructor
public class CRController {

    private final StudentService studentService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final SubmissionService submissionService;
    private final PasswordEncoder passwordEncoder;
    private final ChatService chatService;

    @GetMapping("/manage-students")
    public String manageStudentsPage(Model model, Authentication auth) {
        User crUser = userService.findByEmail(auth.getName());
        if (crUser==null){
            return "auth/login";
        }
        Student student = studentService.getByUserId(crUser.getUserId());
        List<Student> students = studentService.getStudentBySection(student.getSection())
                .stream()
                .filter(s -> s.getUser().getRole() == Role.ROLE_STUDENT)
                .collect(Collectors.toList());

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
            student.setProgram(crStudent.getProgram());
            student.setSemester(crStudent.getSemester());
            student.setUniversity(crStudent.getUniversity());
            student.setAdmission(crStudent.getAdmission());
            student.setGender(gender);
            student.setProgram(crStudent.getProgram());
            student.setSemester(crStudent.getSemester());
            student.setAdmission(crStudent.getAdmission());

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
            assignmentService.deleteByCreatedBy(userService.findByUserId(userId));
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

    @PostMapping("/delete-account")
    public String deleteCRAccount(Authentication auth,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        try {
            User crUser = userService.findByEmail(auth.getName());
            Student crStudent = studentService.findByUserId(crUser.getUserId());

            if (crStudent == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "CR profile not found!");
                return "redirect:/cr/profile?error";
            }

            // 1. Get all ROLE_STUDENT students in this CR's section
            List<Student> students = studentService.getStudentBySection(crStudent.getSection())
                    .stream()
                    .filter(s -> s.getUser().getRole() == Role.ROLE_STUDENT)
                    .collect(Collectors.toList());

            // 2. Delete each student's submissions → student record → user account
            for (Student student : students) {
                List<Submission> subs = submissionService.findByStudent(student);
                subs.forEach(s -> submissionService.deleteById(s.getSubmissionId()));
                Long studentId = student.getStudentId();
                Long userId = student.getUser().getUserId();
                studentService.deleteById(studentId);
                userService.deleteById(userId);
            }

            // 3. Delete submissions for all CR's assignments, then the assignments
            List<Assignment> assignments = assignmentService.getAssignmentsByCreator(crUser);
            for (Assignment assignment : assignments) {
                submissionService.deleteByAssignment(assignment);
                assignmentService.deleteById(assignment.getAssignmentId());
            }

            // 4. Delete chat messages for this class room
            String roomId = crStudent.getAdmission() + "-" +
                    crStudent.getProgram() + "-" +
                    crStudent.getSection() + "-" +
                    crStudent.getSemester();
            chatService.deleteByRoomId(roomId);

            // 5. Delete CR student record and user account
            Long crStudentId = crStudent.getStudentId();
            Long crUserId = crUser.getUserId();
            studentService.deleteById(crStudentId);
            userService.deleteById(crUserId);

            // 6. Invalidate session so they are logged out
            session.invalidate();

            return "redirect:/auth/login?accountDeleted";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete account: " + e.getMessage());
            return "redirect:/cr/profile?error";
        }
    }
}
