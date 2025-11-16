package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignment_Subject_code(String subject_code);
    Optional<Submission> findByStudent_Roll_noAndAssignment_Subject_code(String roll_no, String  subject_code);
    boolean existsByStudent_Roll_noAndAssignment_Subject_Code(Long studentId, Long assignmentId);
    long countByAssignment_Subject_code(String subject_code);
    List<Submission> findByStudent_Roll_no(String roll_no);
}
