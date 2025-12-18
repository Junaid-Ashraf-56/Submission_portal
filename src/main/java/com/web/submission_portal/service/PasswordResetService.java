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


    @Transactional
    public void deleteAllTokensForUser(Long userId) {
        List<PasswordResetToken> tokens = tokenRepository.findAllByUserUserId(userId);
        if (!tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
            log.info("Deleted {} old tokens for user ID: {}", tokens.size(), userId);
        }
    }
}