package com.web.submission_portal.Repository;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.enums.AssignmentType;
import com.web.submission_portal.enums.Role;
import com.web.submission_portal.repository.AssignmentRepository;
import com.web.submission_portal.repository.StudentRepository;
import com.web.submission_portal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AssignmentRepositoryTest {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;
    private StudentRepository studentRepository;

    @Test
    public void testFindById() {
        // 1. Create a new Assignment entity
        User user = User.builder()
                        .email("junaid@example.com")
                        .password("secret")
                        .isFirstLogin(true)
                        .role(Role.ROLE_STUDENT)
                        .createdAt(LocalDateTime.now())
                        .build();

        userRepository.save(user);

        Assignment assignment = Assignment.builder()
                .createdBy(user)
                .subjectCode("CSC201")
                .subjectTitle("Test Assignment")
                .description("Testing repo")
                .startTime(LocalDateTime.now())       // ✅ set startTime
                .endTime(LocalDateTime.now().plusDays(7)) // ✅ set endTime
                .assignmentType(AssignmentType.LAB)
                .createdAt(LocalDateTime.now())
                .build();

        assignmentRepository.save(assignment);


        // 2. Save it → Hibernate will insert into DB and generate ID
        Assignment saved = assignmentRepository.save(assignment);

        // 3. Get the generated ID
        Long assignmentId = saved.getAssignmentId();

        // 4. Use the ID in your test
        Optional<Assignment> found = assignmentRepository.findById(assignmentId);

        assertTrue(found.isPresent());
        assertEquals("CSC201", found.get().getSubjectCode());
    }
}

