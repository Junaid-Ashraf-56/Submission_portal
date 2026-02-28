package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findBySection(String section);
    Student findByUserUserId(Long user_id);
    Optional<Student> findByUser(User user);
    boolean existsByRollNo(String rollNo);

}
