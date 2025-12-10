package com.web.submission_portal.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String login(){
        return  "auth/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/ forgot-password";
    }

    /**
     * Displays the OTP verification page (Part of Day 18).
     * Maps to: GET /auth/verify-otp
     */
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm() {
        return "auth/verify-otp"; // Corresponds to templates/auth/verify-otp.html
    }

    /**
     * Displays the Reset Password page (Part of Day 18).
     * Maps to: GET /auth/reset-password
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "auth/reset-password"; // Corresponds to templates/auth/reset-password.html
    }
}
