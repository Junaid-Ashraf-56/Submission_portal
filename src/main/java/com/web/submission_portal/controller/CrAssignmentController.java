package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.UserRepository;
import com.web.submission_portal.service.AssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;


@RequestMapping("/cr")
@Controller
@PreAuthorize("hasAuthority('ROLE_CR')")
public class CrAssignmentController {

    private final UserRepository userRepository;
    private final AssignmentService assignmentService;
    public CrAssignmentController(UserRepository userRepository,
                                 AssignmentService assignmentService) {
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/create-assignment")
    public String showCreateForm(Model model) {
        model.addAttribute("assignment", new Assignment());
        return "cr/create-assignment";
    }

    @PostMapping("/create-assignment")
    public String createAssignment(@ModelAttribute Assignment assignment,
                                   Authentication authentication,
                                   Model model) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        assignment.setCreatedBy(user);
        assignment.setCreatedAt(LocalDateTime.now());

        if (assignment.getStartTime().isAfter(assignment.getEndTime())) {
            model.addAttribute("error", "Start time must be before end time");
            return "cr/create-assignment";
        }
        assignmentService.createAssignment(assignment);
        return "redirect:/cr/dashboard";
    }

}
