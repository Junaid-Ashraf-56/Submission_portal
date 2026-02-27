package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    Optional<Assignment> findByAssignmentId(Long assignmentId);
    Assignment findFirstByAssignmentId(Long assignmentId);
    List<Assignment> findByCreatedBy(User user);
    @Query("SELECT a FROM Assignment a " +
            "JOIN Student s ON a.createdBy.userId = s.user.userId " +
            "WHERE s.section = :section AND s.user.role = 'ROLE_CR'")
    List<Assignment> findAssignmentsForSection(@Param("section") String section);
}