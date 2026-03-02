package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findBySection(String section);
    Student findByUserUserId(Long user_id);
    Optional<Student> findByUser(User user);
    boolean existsByRollNo(String rollNo);
    Student findByRollNo(String rollNo);
    List<Student> findBySectionAndUniversity(String section,String university);
    List<Student> findAll();
    Integer countBySectionAndUniversity(String section,String university);

    Student getStudentsBySemesterAndSectionAndProgramAndAdmission(String semester,String section,String program,String Admission);
    List<Student> getStudentsBySemesterAndSectionAndAdmissionAndProgram(String semester,String section,String Admission,String program);


    @Query("SELECT s FROM Student s " +
            "JOIN s.user u " +
            "WHERE u.role = :role AND u.status = :status")
    List<Student> findStudentsByUserRoleAndStatus(@Param("role") Role role,
                                                  @Param("status") AccountStatus status);

    @Query("""
       SELECT s FROM Student s
       JOIN s.user u
       WHERE s.section = :section
       AND s.university = :university
       AND u.role = :role
       """)
    Student findCRBySectionAndUniversity(@Param("section") String section,
                                         @Param("university") String university,
                                         @Param("role") Role role);

}

