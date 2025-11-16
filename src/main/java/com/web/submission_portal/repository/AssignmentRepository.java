package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    List<Assignment> findBySubject_code(String subject_code);
    List<Assignment> findByCreatedByUserId(Long userId);
    List<Assignment> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);
    List<Assignment> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    boolean existsBySubjectCode(String subjectCode);
}