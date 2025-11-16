package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findByRoll_no(String roll_no);
    List<Student> findByUser_id(Long user_id);
    List<Student> getAll();
}
