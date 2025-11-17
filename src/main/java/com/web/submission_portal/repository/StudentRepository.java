package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findByRollNo(String roll_no);
    List<Student> findByUserUserId(Long user_id);
}
