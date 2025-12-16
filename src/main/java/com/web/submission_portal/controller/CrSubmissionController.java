package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.Submission;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.exception.ResourceNotFoundException;
import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.SubmissionRepository;
import com.web.submission_portal.repository.UserRepository;
import com.web.submission_portal.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/cr")
@PreAuthorize("hasAuthority('ROLE_CR')")
@Slf4j
public class CrSubmissionController {

    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final StorageService storageService;

    public CrSubmissionController(AssignmentRepository assignmentRepository,
                                  StudentRepository studentRepository,
                                  UserRepository userRepository,
                                  SubmissionRepository submissionRepository,
                                  StorageService storageService) {
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.storageService = storageService;
    }

    @GetMapping("/assignments/{id}/submit")
    public String showSubmissionPage(@PathVariable("id") Long assignmentId,
                                     Model model,
                                     Authentication authentication) {
        try {
            Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();
            Student student = studentRepository.findByUser(user).orElseThrow();

            model.addAttribute("assignment", assignment);
            model.addAttribute("studentName", student.getName());

            return "cr/submit-assignment";
        } catch (Exception e) {
            log.error("Error loading submission page: {}", e.getMessage());
            return "redirect:/cr/dashboard";
        }
    }


    @PostMapping("/assignments/{id}/submit")
    public String submitAssignmentAsCr(@PathVariable("id") Long assignmentId,
                                       @RequestParam("file") MultipartFile file,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            // 1. Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/cr/assignments/" + assignmentId + "/submit";
            }

            // 2. Validate file size (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "File size exceeds 10MB limit");
                return "redirect:/cr/assignments/" + assignmentId + "/submit";
            }

            // 3. Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedFileType(contentType)) {
                redirectAttributes.addFlashAttribute("error", "Invalid file type. Please upload PDF, DOC, DOCX, XLS, or XLSX files");
                return "redirect:/cr/assignments/" + assignmentId + "/submit";
            }

            // 4. Get assignment
            Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

            // 5. Check deadline
            if (LocalDateTime.now().isAfter(assignment.getEndTime())) {
                redirectAttributes.addFlashAttribute("error", "Submission deadline has passed");
                return "redirect:/cr/dashboard";
            }

            // 6. Get student (CR is also a student)
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Student student = studentRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            // 7. Check for existing submission
            Submission existingSubmission = submissionRepository
                    .findByStudentAndAssignment(student, assignment)
                    .orElse(null);

            // 8. Delete old file if resubmitting
            if (existingSubmission != null && existingSubmission.getFilePath() != null) {
                try {
                    storageService.deleteFile(existingSubmission.getFilePath());
                    log.info("Deleted old file for CR: {}", existingSubmission.getFilePath());
                } catch (Exception e) {
                    log.warn("Failed to delete old file: {}", e.getMessage());
                }
            }

            // 9. Upload to Supabase
            Map<String, String> fileInfo = storageService.uploadFile(file, assignmentId, student.getStudentId());

            // 10. Calculate if submission is late
            boolean isLate = LocalDateTime.now().isAfter(assignment.getEndTime());

            // 11. Save or update submission
            if (existingSubmission != null) {
                // Update existing submission
                existingSubmission.setFileUrl(fileInfo.get("fileUrl"));
                existingSubmission.setFilePath(fileInfo.get("filePath"));
                existingSubmission.setOriginalFileName(fileInfo.get("originalFileName"));
                existingSubmission.setStoredFileName(fileInfo.get("storedFileName"));
                existingSubmission.setFileSize(Long.parseLong(fileInfo.get("fileSize")));
                existingSubmission.setFileType(fileInfo.get("fileType"));
                existingSubmission.setIsLate(isLate);
                existingSubmission.setUpdatedAt(LocalDateTime.now());

                submissionRepository.save(existingSubmission);
                log.info("Updated submission for CR: {} on assignment: {}", student.getRollNo(), assignmentId);

                redirectAttributes.addFlashAttribute("success", "Assignment resubmitted successfully!");
            } else {
                // Create new submission
                Submission submission = new Submission();
                submission.setAssignment(assignment);
                submission.setStudent(student);
                submission.setFileUrl(fileInfo.get("fileUrl"));
                submission.setFilePath(fileInfo.get("filePath"));
                submission.setOriginalFileName(fileInfo.get("originalFileName"));
                submission.setStoredFileName(fileInfo.get("storedFileName"));
                submission.setFileSize(Long.parseLong(fileInfo.get("fileSize")));
                submission.setFileType(fileInfo.get("fileType"));
                submission.setIsLate(isLate);

                submissionRepository.save(submission);
                log.info("Created new submission for CR: {} on assignment: {}", student.getRollNo(), assignmentId);

                redirectAttributes.addFlashAttribute("success", "Assignment submitted successfully!");
            }

            return "redirect:/cr/dashboard";

        } catch (Exception e) {
            log.error("CR submission failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
            return "redirect:/cr/assignments/" + assignmentId + "/submit";
        }
    }


    private boolean isAllowedFileType(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}