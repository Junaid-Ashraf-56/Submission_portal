package com.web.submission_portal.service;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.Submission;
import com.web.submission_portal.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public List<Submission> findByStudent(Student student) {
        return submissionRepository.findByStudent(student);
    }



    @Transactional
    public Submission save(Submission submission) {
        return submissionRepository.save(submission);
    }

    @Transactional
    public void deleteById(Long id) {
        submissionRepository.deleteById(id);
    }

    public void deleteByAssignment(Assignment assignment) {
        submissionRepository.deleteByAssignment(assignment);
    }
}