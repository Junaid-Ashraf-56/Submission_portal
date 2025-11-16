package com.web.submission_portal.repository;

import com.web.submission_portal.entity.Password_Reset_T;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenRepository, Long> {
    Optional<Password_Reset_T> findByUserUserIdAndUsedFalseAndExpiryTimeAfter(
            Long userId,
            LocalDateTime now
    );
    Optional<Password_Reset_T> findByOtpAndUserUserId(String otp, Long userId);

    void deleteByExpiryTimeBefore(LocalDateTime cutoffTime);
    void deleteByUsedTrue();
}
