package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    Assignment findAssignmentById(long id);
}