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

    List<Submission> findByAssignmentAssignmentId(Long assignmentId);
    List<Submission> findByAssignment(Assignment assignment);
    List<Submission> findByStudent(Student student);
    Optional<Submission> findByStudentAndAssignment(Student student, Assignment assignment);
    boolean existsByStudentAndAssignment(Student student, Assignment assignment);
    long countByAssignment(Assignment assignment);
    long countByAssignmentAssignmentId(Long assignmentId);
    boolean existsByAssignmentAssignmentId(Long assignmentId);


}
