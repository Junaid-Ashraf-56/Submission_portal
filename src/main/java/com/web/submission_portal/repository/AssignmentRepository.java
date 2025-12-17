package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    Optional<Assignment> findByAssignmentId(Long assignmentId);
    Assignment findFirstByAssignmentId(Long assignmentId);
    List<Assignment> findByCreatedBy(User user);

}