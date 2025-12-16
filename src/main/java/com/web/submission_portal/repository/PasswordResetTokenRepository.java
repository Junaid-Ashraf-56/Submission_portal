package com.web.submission_portal.repository;

import com.web.submission_portal.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUserUserIdAndUsedFalseAndExpiryTimeAfter(
            Long userId,
            LocalDateTime now
    );
    Optional<PasswordResetToken> findByOtpAndUserUserId(String otp, Long userId);

    void deleteByExpiryTimeBefore(LocalDateTime cutoffTime);
    void deleteByUsedTrue();

    List<PasswordResetToken> findAllByUserUserId(Long userId);
}
