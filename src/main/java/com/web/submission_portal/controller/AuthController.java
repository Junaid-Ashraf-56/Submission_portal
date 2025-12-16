package com.web.submission_portal.controller;

import com.web.submission_portal.service.OTPValidationService;
import com.web.submission_portal.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final PasswordResetService passwordResetService;
    private final OTPValidationService otpValidationService;


    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/ forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("Processing forgot password for email: {}", email);

            passwordResetService.sendOTP(email);

            session.setAttribute("resetEmail", email);
            session.setAttribute("otpSentTime", System.currentTimeMillis());

            log.info("OTP sent successfully, redirecting to verify-otp");

            return "redirect:/auth/verify-otp";

        } catch (RuntimeException e) {
            log.error("Error sending OTP: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password?error";
        }
    }


    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            log.warn("No email in session, redirecting to forgot-password");
            redirectAttributes.addFlashAttribute("error", "Please request OTP first");
            return "redirect:/auth/forgot-password";
        }

        return "auth/ verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOTP(
            @RequestParam("digit1") String d1,
            @RequestParam("digit2") String d2,
            @RequestParam("digit3") String d3,
            @RequestParam("digit4") String d4,
            @RequestParam("digit5") String d5,
            @RequestParam("digit6") String d6,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            String email = (String) session.getAttribute("resetEmail");

            if (email == null) {
                throw new RuntimeException("Session expired. Please request OTP again.");
            }

            String otp = d1 + d2 + d3 + d4 + d5 + d6;
            otp = otpValidationService.sanitizeOTP(otp);

            log.info("Verifying OTP for email: {}", email);

            OTPValidationService.OTPValidationResult result =
                    otpValidationService.validateOTP(email, otp);

            if (!result.valid()) {
                throw new RuntimeException(result.message());
            }

            Long otpSentTime = (Long) session.getAttribute("otpSentTime");
            if (otpSentTime != null) {
                long elapsedSeconds = (System.currentTimeMillis() - otpSentTime) / 1000;
                if (elapsedSeconds > 60) {
                    session.removeAttribute("resetEmail");
                    session.removeAttribute("otpSentTime");
                    throw new RuntimeException("OTP expired. Please request a new one.");
                }
            }

            session.setAttribute("verifiedOTP", otp);
            log.info("OTP verified successfully for: {}", email);

            return "redirect:/auth/reset-password";

        } catch (RuntimeException e) {
            log.error("OTP verification failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/verify-otp?error";
        }
    }

    @PostMapping("/resend-otp")
    @ResponseBody
    public String resendOTP(HttpSession session) {
        try {
            String email = (String) session.getAttribute("resetEmail");

            if (email == null) {
                return "error:Session expired";
            }

            passwordResetService.sendOTP(email);

            session.setAttribute("otpSentTime", System.currentTimeMillis());

            log.info("OTP resent successfully to: {}", email);
            return "success";

        } catch (Exception e) {
            log.error("Failed to resend OTP: {}", e.getMessage());
            return "error:" + e.getMessage();
        }
    }


    @GetMapping("/reset-password")
    public String showResetPasswordPage(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        String verifiedOTP = (String) session.getAttribute("verifiedOTP");

        if (email == null || verifiedOTP == null) {
            log.warn("No verified OTP in session");
            redirectAttributes.addFlashAttribute("error", "Please verify OTP first");
            return "redirect:/auth/forgot-password";
        }

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            String email = (String) session.getAttribute("resetEmail");
            String otp = (String) session.getAttribute("verifiedOTP");

            if (email == null || otp == null) {
                throw new RuntimeException("Session expired. Please start over.");
            }

            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Passwords do not match");
            }

            if (newPassword.length() < 8) {
                throw new RuntimeException("Password must be at least 8 characters long");
            }

            log.info("Resetting password for email: {}", email);

            passwordResetService.resetPassword(email, otp, newPassword);

            session.removeAttribute("resetEmail");
            session.removeAttribute("verifiedOTP");
            session.removeAttribute("otpSentTime");

            log.info("Password reset successful");
            redirectAttributes.addFlashAttribute("success", "Password updated successfully! Please login.");

            return "redirect:/login?success";

        } catch (RuntimeException e) {
            log.error("Password reset failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?error";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }
}