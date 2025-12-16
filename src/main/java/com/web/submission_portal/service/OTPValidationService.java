package com.web.submission_portal.service;

import com.web.submission_portal.entity.PasswordResetToken;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.repository.PasswordResetTokenRepository;
import com.web.submission_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPValidationService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_SECONDS = 60;
    private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");


    public OTPValidationResult validateOTP(String email, String otp) {
        log.info("Starting OTP validation for email: {}", email);

        OTPValidationResult formatCheck = validateFormat(otp);
        if (!formatCheck.valid()) {  // ← FIXED: Changed from valid() to !valid()
            log.warn("OTP format validation failed: {}", formatCheck.message());
            return formatCheck;
        }

        OTPValidationResult userCheck = validateUserExists(email);
        if (!userCheck.valid()) {  // ← FIXED
            log.warn("User validation failed: {}", userCheck.message());
            return userCheck;
        }

        User user = userCheck.user();

        OTPValidationResult tokenCheck = validateTokenExists(user, otp);
        if (!tokenCheck.valid()) {  // ← FIXED
            log.warn("Token validation failed: {}", tokenCheck.message());
            return tokenCheck;
        }

        PasswordResetToken token = tokenCheck.token();

        OTPValidationResult usedCheck = validateNotUsed(token);
        if (!usedCheck.valid()) {  // ← FIXED
            log.warn("Token already used: {}", usedCheck.message());
            return usedCheck;
        }

        OTPValidationResult expiryCheck = validateNotExpired(token);
        if (!expiryCheck.valid()) {  // ← FIXED
            log.warn("Token expired: {}", expiryCheck.message());
            return expiryCheck;
        }

        log.info("OTP validation successful for email: {}", email);
        return OTPValidationResult.success("OTP is valid", user, token);
    }

    private OTPValidationResult validateFormat(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            return OTPValidationResult.failure("OTP cannot be empty");
        }

        if (otp.length() != OTP_LENGTH) {
            return OTPValidationResult.failure(
                    String.format("OTP must be exactly %d digits", OTP_LENGTH)
            );
        }

        if (!OTP_PATTERN.matcher(otp).matches()) {
            return OTPValidationResult.failure("OTP must contain only digits");
        }

        return OTPValidationResult.success("Format valid");
    }

    private OTPValidationResult validateUserExists(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        return userOpt.map(user -> OTPValidationResult.success("User exists", user, null))
                .orElseGet(() -> OTPValidationResult.failure("User not found with this email"));
    }

    private OTPValidationResult validateTokenExists(User user, String otp) {
        Optional<PasswordResetToken> tokenOpt =
                tokenRepository.findByOtpAndUserUserId(otp, user.getUserId());

        return tokenOpt.map(passwordResetToken -> OTPValidationResult.success("Token exists", user, passwordResetToken))
                .orElseGet(() -> OTPValidationResult.failure(
                        "Invalid OTP. Please check and try again."
                ));
    }

    private OTPValidationResult validateNotUsed(PasswordResetToken token) {
        if (Boolean.TRUE.equals(token.getUsed())) {
            return OTPValidationResult.failure(
                    "This OTP has already been used. Please request a new one."
            );
        }

        return OTPValidationResult.success("Token not used");
    }

    private OTPValidationResult validateNotExpired(PasswordResetToken token) {
        LocalDateTime now = LocalDateTime.now();

        if (token.getExpiryTime().isBefore(now)) {
            long secondsExpired = ChronoUnit.SECONDS.between(token.getExpiryTime(), now);
            return OTPValidationResult.failure(
                    String.format("OTP expired %d seconds ago. Please request a new one.",
                            secondsExpired)
            );
        }

        LocalDateTime createdAt = token.getCreatedAt();
        long secondsSinceCreation = ChronoUnit.SECONDS.between(createdAt, now);

        if (secondsSinceCreation > OTP_EXPIRY_SECONDS) {
            return OTPValidationResult.failure(
                    "OTP expired. It's only valid for 60 seconds."
            );
        }

        long remainingSeconds = OTP_EXPIRY_SECONDS - secondsSinceCreation;
        log.info("OTP still valid. {} seconds remaining", remainingSeconds);

        return OTPValidationResult.success("Token not expired");
    }





    public String sanitizeOTP(String otp) {
        if (otp == null) {
            return "";
        }
        return otp.replaceAll("\\D", "");
    }


    public record OTPValidationResult(
            boolean valid,
            String message,
            User user,
            PasswordResetToken token
    ) {
        public static OTPValidationResult success(String message) {
            return new OTPValidationResult(true, message, null, null);
        }

        public static OTPValidationResult success(String message, User user, PasswordResetToken token) {
            return new OTPValidationResult(true, message, user, token);
        }

        public static OTPValidationResult failure(String message) {
            return new OTPValidationResult(false, message, null, null);
        }

        // ← FIXED: Removed the inverted @Override method
        // Just use the record's built-in valid() accessor

        @Override
        public String toString() {
            return String.format("OTPValidationResult{valid=%s, message='%s'}", valid, message);
        }
    }
}