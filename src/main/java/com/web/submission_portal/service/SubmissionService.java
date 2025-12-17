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

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public List<Submission> findByAssignment(Assignment assignment) {
        return submissionRepository.findByAssignment(assignment);
    }

    public List<Submission> findByStudent(Student student) {
        return submissionRepository.findByStudent(student);
    }

    public Submission findByAssignmentAndStudent(Assignment assignment, Student student) {
        return submissionRepository.findByStudentAndAssignment(student,assignment).orElse(null);
    }

    public Submission findById(Long id) {
        return submissionRepository.findById(id).orElse(null);
    }

    public boolean existsByAssignmentAndStudent(Assignment assignment, Student student) {
        return submissionRepository.existsByStudentAndAssignment(student,assignment);
    }

    public long countByAssignment(Assignment assignment) {
        return submissionRepository.countByAssignment(assignment);
    }

    @Transactional
    public Submission save(Submission submission) {
        return submissionRepository.save(submission);
    }

    @Transactional
    public void deleteById(Long id) {
        submissionRepository.deleteById(id);
    }

    @Transactional
    public void deleteByStudent(Student student) {
        List<Submission> submissions = findByStudent(student);
        submissionRepository.deleteAll(submissions);
    }
}