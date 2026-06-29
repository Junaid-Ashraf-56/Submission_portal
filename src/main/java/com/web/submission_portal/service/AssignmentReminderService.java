package com.web.submission_portal.service;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.SubmissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentReminderService {

    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final SubmissionRepository submissionRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    @Transactional
    public void sendThirtyMinuteDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusMinutes(29);
        LocalDateTime to = now.plusMinutes(30);

        List<Assignment> assignments = assignmentRepository.findAssignmentsNeedingDeadlineReminder(from, to);

        for (Assignment assignment : assignments) {
            try {
                Student crStudent = studentRepository.findByUser(assignment.getCreatedBy()).orElse(null);
                if (crStudent == null) {
                    log.warn("Skipping reminder for assignment {} because CR profile was not found",
                            assignment.getAssignmentId());
                    assignment.setReminderEmailSent(true);
                    assignmentRepository.save(assignment);
                    continue;
                }

                List<Student> students = studentRepository.findApprovedStudentsInClass(
                        crStudent.getUniversity(),
                        crStudent.getAdmission(),
                        crStudent.getProgram(),
                        crStudent.getSection(),
                        crStudent.getSemester()
                );

                students.stream()
                        .filter(student -> !submissionRepository.existsByStudentAndAssignment(student, assignment))
                        .forEach(student -> emailService.sendAssignmentDeadlineReminderEmail(student, crStudent, assignment));

                assignment.setReminderEmailSent(true);
                assignmentRepository.save(assignment);
            } catch (Exception e) {
                log.error("Failed to process deadline reminders for assignment {}: {}",
                        assignment.getAssignmentId(), e.getMessage());
            }
        }
    }
}
