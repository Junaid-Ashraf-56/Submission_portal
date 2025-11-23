package com.web.submission_portal.service;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentService {


    private final AssignmentRepository assignmentRepository;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public void createAssignment(Assignment assignment){
        assignmentRepository.save(assignment);
    }

    public List<Assignment> getAssignmentsByCreator(User user) {
        return assignmentRepository.findByCreatedBy(user);
    }
}
