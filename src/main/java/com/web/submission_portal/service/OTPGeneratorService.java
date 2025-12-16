package com.web.submission_portal.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

/**
 * Simple OTP Generator Service
 * Generates secure 6-digit OTP codes
 */
@Service
public class OTPGeneratorService {

    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    /**
     * Generate a random 6-digit OTP
     * Example: 123456, 987654, 456789
     */
    public String generateOTP() {
        // Generate random number between 100000 and 999999
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Generate OTP with custom length (if needed)
     * @param length - Number of digits (e.g., 4, 6, 8)
     */
    public String generateOTP(int length) {
        if (length < 4 || length > 8) {
            throw new IllegalArgumentException("OTP length must be between 4 and 8");
        }

        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int otp = min + random.nextInt(max - min + 1);

        return String.valueOf(otp);
    }

    /**
     * Validate OTP format (just checks if it's 6 digits)
     */
    public boolean isValidOTPFormat(String otp) {
        if (otp == null || otp.length() != OTP_LENGTH) {
            return false;
        }

        // Check if all characters are digits
        return otp.matches("\\d{6}");
    }
}