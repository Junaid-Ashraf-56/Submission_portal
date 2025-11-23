package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentSubjectCode(String subject_code);
    Optional<Submission> findByStudentRollNoAndAssignmentSubjectCode(String roll_no, String  subject_code);
    boolean existsByStudentAndAssignment(Student student, Assignment assignment);
    long countByAssignment(Assignment assignment);
    List<Submission> findByStudentRollNo(String roll_no);
}
