package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.exception.ResourceNotFoundException;
import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.SubmissionRepository;
import com.web.submission_portal.repository.UserRepository;
import com.web.submission_portal.service.AssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequestMapping("/cr")
@Controller
@PreAuthorize("hasAuthority('ROLE_CR')")
public class CrAssignmentController {

    private final UserRepository userRepository;
    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final SubmissionRepository submissionRepository;

    public CrAssignmentController(UserRepository userRepository,
                                 AssignmentService assignmentService, 
                                  StudentRepository studentRepository,
                                  AssignmentRepository assignmentRepository,
                                  SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
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


    @GetMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, Model model, Authentication authentication){
        getAssignmentForBoth(id, model, authentication, assignmentRepository, userRepository, studentRepository);

        return "cr/submit-assignment";
    }



    @GetMapping("assignments/{id}/extend")
    public String extendAssignment(@PathVariable Long id, Model model, Authentication authentication){
        getAssignmentForBoth(id, model, authentication, assignmentRepository, userRepository, studentRepository);
        return "cr/extend-deadline";
    }
    @PostMapping("assignments/{id}/extend")
    public String extendAssignmentInDataBase(@PathVariable Long id,
                                             @RequestParam("newEndTime") LocalDateTime newEndTime,
                                             Model model,
                                             Authentication authentication){
        Assignment assignment = assignmentRepository.findByAssignmentId(id).orElseThrow();

        if (newEndTime.isBefore(assignment.getEndTime())) {
            model.addAttribute("error", "End time must be before start time");
            model.addAttribute("assignment",assignment);
            getAssignmentForBoth(id,model,authentication,assignmentRepository,userRepository,studentRepository);
            return "cr/extend-deadline";
        }
        assignment.setEndTime(newEndTime);
        assignmentRepository.save(assignment);

        return "redirect:/cr/dashboard";
    }



    @GetMapping("/assignments/{id}/edit")
    public  String editAssignment(@PathVariable Long id, Model model, Authentication authentication){
        getAssignmentForBoth(id, model, authentication, assignmentRepository, userRepository, studentRepository);
        return "cr/edit-assignment";
    }
    @PostMapping("/assignments/{id}/edit")
    public String editAssignmentInDataBase(@PathVariable Long id,@ModelAttribute Assignment updatedAssignment){
        Assignment assignment = assignmentRepository.findByAssignmentId(id)
                .orElseThrow(()-> new ResourceNotFoundException("Assignment not found"));

        assignment.setAssignmentType(updatedAssignment.getAssignmentType());
        assignment.setDescription(updatedAssignment.getDescription());
        assignment.setSubjectCode(updatedAssignment.getSubjectCode());
        assignment.setSubjectTitle(updatedAssignment.getSubjectTitle());
        assignment.setCreatedBy(assignment.getCreatedBy());
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setAssignmentId(assignment.getAssignmentId());

        assignmentRepository.save(assignment);
        return "redirect:/cr/dashboard";
    }


    @GetMapping("/assignments/{id}/submissions")
    public String viewAssignments(@PathVariable Long id, Model model, Authentication authentication){
        getAssignmentForBoth(id, model, authentication, assignmentRepository, userRepository, studentRepository);

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();
        submissionDetails(model, user, student, assignmentService, submissionRepository, studentRepository);
        model.addAttribute("success","Successfully view assignments");
        return "cr/view-assignment";
    }

    static void submissionDetails(Model model, User user,
                                  Student student,
                                  AssignmentService assignmentService,
                                  SubmissionRepository submissionRepository,
                                  StudentRepository studentRepository) {

        List<Assignment> assignment = assignmentService.getAssignmentsByCreator(user);
        Map<String,Long> submissionCount = new HashMap<>();
        for (Assignment a : assignment) {
            long count = submissionRepository.countByAssignment(a);
            submissionCount.put(student.getRollNo(), count);
        }

        long totalStudents = studentRepository.count();
        model.addAttribute("createdAssignments", assignment);
        model.addAttribute("submissionCounts", submissionCount);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("crName",student.getName());
    }


    static void getAssignmentForBoth(@PathVariable Long id,
                                     Model model,
                                     Authentication authentication,
                                     AssignmentRepository assignmentRepository,
                                     UserRepository userRepository,
                                     StudentRepository studentRepository) {

        Assignment assignment = assignmentRepository.findByAssignmentId(id)
                .orElseThrow(()->new ResourceNotFoundException("Assignment not found"));

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUser(user).orElseThrow();

        model.addAttribute("assignment", assignment);
        model.addAttribute("studentName", student.getName());
        model.addAttribute("error",null);
    }

}
