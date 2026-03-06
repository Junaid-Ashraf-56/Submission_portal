package com.web.submission_portal.repository;

import com.web.submission_portal.entity.User;
import com.web.submission_portal.enums.AccountStatus;
import com.web.submission_portal.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByRoleAndStatus(Role role, AccountStatus status);
    User findByUserId(Long userId);
}
