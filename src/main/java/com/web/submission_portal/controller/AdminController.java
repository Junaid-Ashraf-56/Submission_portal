package com.web.submission_portal.controller;


import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Role;
import com.web.submission_portal.service.StudentService;
import com.web.submission_portal.service.UserService;
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
            Student cr = studentService.findCRBySectionAndUniversity(
                    stu.getSection(), stu.getUniversity(),Role.ROLE_CR);
            stu.setCrName(cr != null ? cr.getName() : null);
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
    public String rejectCR(@RequestParam String email, RedirectAttributes redirectAttributes) {

        try {
            if (DeleteUser(email, redirectAttributes)) return "redirect:/admin/admin-panel";

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

    @PostMapping("/cr/delete")
    public String deleteCR(@RequestParam String email, RedirectAttributes redirectAttributes) {

        try {
            if (DeleteUser(email, redirectAttributes)) return "redirect:/admin";

            log.info("CR deleted: email={}", email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "CR deleted successfully.");

        } catch (Exception e) {
            log.error("Failed to delete CR email={}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete CR. Please try again.");
        }

        return "redirect:/admin/admin-panel";
    }

    private boolean DeleteUser(@RequestParam String email, RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(email);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No user found with email: " + email);
            return true;
        }

        Student student = studentService.findByUserId(user.getUserId());
        if (student != null) {
            studentService.deleteById(student.getStudentId());
        }

        userService.deleteById(user.getUserId());
        return false;
    }
}
