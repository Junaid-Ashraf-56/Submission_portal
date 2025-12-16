package com.web.submission_portal.service;

import com.web.submission_portal.entity.PasswordResetToken;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.PasswordResetTokenRepository;
import com.web.submission_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final OTPGeneratorService otpGenerator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${otp.expiry.minutes:1}")
    private int otpExpiryMinutes;

    /**
     * Step 1: Generate OTP and send to user's email
     * FIXED: Now deletes ALL old tokens for this user before creating new one
     */
    @Transactional
    public void sendOTP(String email) {
        log.info("OTP request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // ← FIXED: Delete ALL old tokens for this user (both used and unused)
        deleteAllTokensForUser(user.getUserId());
        log.info("Deleted all old OTP tokens for user: {}", email);

        String otp = otpGenerator.generateOTP();
        log.debug("Generated OTP: {} for user: {}", otp, email);

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setOtp(otp);
        token.setExpiryTime(expiryTime);
        token.setUsed(false);
        tokenRepository.save(token);

        try {
            emailService.sendOTPEmail(email, otp);
            log.info("OTP sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    /**
     * Step 2: Verify OTP entered by user
     * This is now ONLY used for verification, not for password reset
     */
    public boolean verifyOTP(String email, String otp) {
        log.info("Verifying OTP for email: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", email);
            return false;
        }

        User user = userOpt.get();

        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findByOtpAndUserUserId(otp, user.getUserId());

        if (tokenOpt.isEmpty()) {
            log.warn("Invalid OTP for user: {}", email);
            return false;
        }

        PasswordResetToken token = tokenOpt.get();

        if (token.getUsed()) {
            log.warn("OTP already used for user: {}", email);
            return false;
        }

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for user: {}", email);
            return false;
        }

        log.info("OTP verified successfully for user: {}", email);
        return true;
    }

    /**
     * Step 3: Reset password after OTP verification
     * FIXED: Removed the verifyOTP call since we already verified in the controller
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        log.info("Password reset request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ← FIXED: Find the token but don't verify again (already verified in controller)
        PasswordResetToken token = tokenRepository
                .findByOtpAndUserUserId(otp, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        // Check if already used
        if (token.getUsed()) {
            throw new RuntimeException("This OTP has already been used");
        }

        // Check if expired
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated in database for user: {}", email);

        // Mark OTP as used
        token.setUsed(true);
        tokenRepository.save(token);
        log.info("OTP marked as used");

        // Send confirmation email
        try {
            emailService.sendPasswordResetConfirmation(email);
        } catch (Exception e) {
            log.error("Failed to send confirmation email: {}", e.getMessage());
        }

        log.info("Password reset successful for user: {}", email);
    }

    /**
     * Check if user has valid unused OTP
     */
    public boolean hasValidOTP(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findByUserUserIdAndUsedFalseAndExpiryTimeAfter(
                        user.getUserId(),
                        LocalDateTime.now()
                );

        return tokenOpt.isPresent();
    }

    /**
     * ← FIXED: Delete ALL tokens (used and unused) for a specific user
     */
    @Transactional
    public void deleteAllTokensForUser(Long userId) {
        List<PasswordResetToken> tokens = tokenRepository.findAllByUserUserId(userId);
        if (!tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
            log.info("Deleted {} old tokens for user ID: {}", tokens.size(), userId);
        }
    }

    /**
     * Scheduled cleanup of expired tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now();
        tokenRepository.deleteByExpiryTimeBefore(cutoffTime);
        log.info("Cleaned up expired tokens");
    }

    /**
     * Scheduled cleanup of used tokens
     */
    @Transactional
    public void cleanupUsedTokens() {
        tokenRepository.deleteByUsedTrue();
        log.info("Cleaned up used tokens");
    }
}