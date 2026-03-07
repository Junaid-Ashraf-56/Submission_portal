package com.web.submission_portal.controller;


import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.Submission;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Role;
import com.web.submission_portal.repository.ChatMessageRepository;
import com.web.submission_portal.service.AssignmentService;
import com.web.submission_portal.service.StudentService;
import com.web.submission_portal.service.SubmissionService;
import com.web.submission_portal.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final StudentService studentService;
    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/admin-panel")
    public String adminPanel(Model model){

        List<Student> pendingRequests =  studentService.findByRoleAndStatus(Role.ROLE_CR, AccountStatus.PENDING);

        List<Student> crs = studentService.findByRoleAndStatus(Role.ROLE_CR,AccountStatus.APPROVED);

        crs.forEach(cr -> {
            int count = studentService.countStudentsBySectionAndUniversity(
                    cr.getSection(), cr.getUniversity());
            cr.setStudentCount(count);

            List<Student> crStudents = studentService.findBySectionAndUniversity(
                    cr.getSection(), cr.getUniversity());
            cr.setStudents(crStudents);
        });

        List<Student> allStudents = studentService.findAllStudents();
        allStudents.forEach(stu -> {
            List<Student> crList = studentService.findCRBySectionAndUniversity(
                    stu.getSection(), stu.getUniversity(), Role.ROLE_CR);
            stu.setCrName(!crList.isEmpty() ? crList.get(0).getCrName() : null);
        });

        model.addAttribute("pendingRequests",  pendingRequests);
        model.addAttribute("crs",              crs);
        model.addAttribute("allStudents",      allStudents);
        model.addAttribute("totalCRs",         crs.size());
        model.addAttribute("totalStudents",    allStudents.size());
        model.addAttribute("pendingCount",     pendingRequests.size());
        model.addAttribute("totalAssignments", 0);


        return "admin/admin-panel";
    }


    @PostMapping("/cr/approve")
    public String approveCR(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(email);

            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No user found with email: " + email);
                return "redirect:/admin/admin-panel";
            }

            if (user.getStatus() == AccountStatus.APPROVED) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "This CR is already active.");
                return "redirect:/admin/admin-panel";
            }

            user.setStatus(AccountStatus.APPROVED);
            userService.save(user);

            log.info("CR approved: email={}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "CR account approved: " + email);

        } catch (Exception e) {
            log.error("Failed to approve CR email={}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to approve CR. Please try again.");
        }

        return "redirect:/admin/admin-panel";
    }

    @PostMapping("/cr/reject")
    public String rejectCR(@RequestParam String email,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No user found with email: " + email);
                return "redirect:/admin/admin-panel";
            }

            Student student = studentService.findByUserId(user.getUserId());
            if (student != null) {
                studentService.deleteById(student.getStudentId());
            }
            userService.deleteById(user.getUserId());

            log.info("CR rejected and removed: email={}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "CR request rejected and removed.");

        } catch (Exception e) {
            log.error("Failed to reject CR email={}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to reject CR. Please try again.");
        }

        return "redirect:/admin/admin-panel";
    }

    @Transactional
    @PostMapping("/cr/delete")
    public String deleteCR(@RequestParam String email,
                           RedirectAttributes redirectAttributes) {
        try {
            User crUser = userService.findByEmail(email);
            if (crUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No user found with email: " + email);
                return "redirect:/admin/admin-panel";
            }

            Student crStudent = studentService.findByUserId(crUser.getUserId());
            if (crStudent == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No CR profile found for email: " + email);
                return "redirect:/admin/admin-panel";
            }

            List<Student> students = studentService.getStudentBySection(crStudent.getSection())
                    .stream()
                    .filter(s -> s.getUser().getRole() == Role.ROLE_STUDENT)
                    .toList();

            for (Student student : students) {
                List<Submission> subs = submissionService.findByStudent(student);
                subs.forEach(s -> submissionService.deleteById(s.getSubmissionId()));
                studentService.deleteById(student.getStudentId());
                userService.deleteById(student.getUser().getUserId());
            }

            List<Assignment> assignments = assignmentService.getAssignmentsByCreator(crUser);
            for (Assignment assignment : assignments) {
                submissionService.deleteByAssignment(assignment);
                assignmentService.deleteById(assignment.getAssignmentId());
            }

            String roomId = crStudent.getAdmission() + "-" +
                    crStudent.getProgram() + "-" +
                    crStudent.getSection() + "-" +
                    crStudent.getSemester();
            chatMessageRepository.deleteByRoomId(roomId);

            List<Submission> crSubs = submissionService.findByStudent(crStudent);
            crSubs.forEach(s -> submissionService.deleteById(s.getSubmissionId()));

            studentService.deleteById(crStudent.getStudentId());
            userService.deleteById(crUser.getUserId());

            log.info("CR fully deleted: email={}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "CR and all associated data deleted successfully.");

        } catch (Exception e) {
            log.error("Failed to delete CR email={}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete CR: " + e.getMessage());
        }

        return "redirect:/admin/admin-panel";
    }

    @Transactional
    @PostMapping("/student/delete")
    public String deleteStudent(@RequestParam Long userId,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUserId(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student not found!");
                return "redirect:/admin/admin-panel";
            }

            Student student = studentService.findByUserId(userId);
            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student profile not found!");
                return "redirect:/admin/admin-panel";
            }

            List<Submission> submissions = submissionService.findByStudent(student);
            submissions.forEach(s -> submissionService.deleteById(s.getSubmissionId()));

            if (user.getRole() == Role.ROLE_CR) {
                List<Assignment> assignments = assignmentService.getAssignmentsByCreator(user);
                for (Assignment assignment : assignments) {
                    submissionService.deleteByAssignment(assignment);
                    assignmentService.deleteById(assignment.getAssignmentId());
                }
            }

            studentService.deleteById(student.getStudentId());
            userService.deleteById(userId);

            log.info("Student deleted by admin: userId={}", userId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student deleted successfully.");

        } catch (Exception e) {
            log.error("Failed to delete student userId={}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete student: " + e.getMessage());
        }

        return "redirect:/admin/admin-panel";
    }
}
