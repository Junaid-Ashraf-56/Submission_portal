package com.web.submission_portal.service;

import com.web.submission_portal.entity.Student;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Role;
import com.web.submission_portal.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> getStudentBySection(String section) {
        return studentRepository.findBySection(section);
    }

    public Student findByUserId(Long userId) {
        return studentRepository.findByUserUserId(userId);
    }

    public boolean existsByRollNo(String rollNo) {
        return studentRepository.existsByRollNo(rollNo);
    }

    @Transactional
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    public Student getByUserId(long userId){
        return studentRepository.findByUserUserId(userId);
    }

    public Student getByRollNo(String rollNo){
        return studentRepository.findByRollNo(rollNo);
    }
    public List<Student> findAllStudents(){
        return studentRepository.findAll();
    }
    public int countStudentsBySectionAndUniversity(String section,String university){
        return studentRepository.countBySectionAndUniversity(section,university);
    }

    public List<Student> findBySectionAndUniversity(String section,String university){
        return studentRepository.findBySectionAndUniversity(section,university);
    }
    public List<Student> findByRoleAndStatus(Role role, AccountStatus accountStatus){
        return studentRepository.findStudentsByUserRoleAndStatus(role,accountStatus);
    }

    public Student findCRBySectionAndUniversity(String section,String university,Role role){
        return studentRepository.findCRBySectionAndUniversity(section,university,role);
    }

    public Student findBySemesterAndSectionAndProgramAndAdmission(String semester,String section,String program,String admission){
        return studentRepository.getStudentsBySemesterAndSectionAndProgramAndAdmission(semester,section,program,admission);
    }
}