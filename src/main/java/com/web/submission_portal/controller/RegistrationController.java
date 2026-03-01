package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Student;
import com.web.submission_portal.entity.User;
import com.web.submission_portal.enums.Gender;
import com.web.submission_portal.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;
    private final StudentService studentService;
    private final OTPGeneratorService otpGeneratorService;
    private final OTPValidationService otpValidationService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/register")
    public String showRegisterPage(){
        return "auth/register";
    }

    @PostMapping("/register")
    public String getCrInfo(
            @RequestParam String name,
            @RequestParam String rollNumber,
            @RequestParam Gender gender,
            @RequestParam String phoneNumber,
            @RequestParam String section,
            @RequestParam String university,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            HttpSession session){

        try{
            User checkByEmail = userService.findByEmail(email);
            if(checkByEmail!=null){
                redirectAttributes.addFlashAttribute("errorMessage","User with this email already exist");
                return "redirect:/auth/register";
            }

            Student checkByRollNo = studentService.getByRollNo(rollNumber);
            if(checkByRollNo!=null){
                redirectAttributes.addFlashAttribute("errorMessage","Student with this roll number already exist");
                return "redirect:/auth/register";
            }



            //Otp send to email for the verification of the email that it belongs to the user
            String otp = otpGeneratorService.generateOTP();
            emailService.sendOTPEmail(email,otp);
            session.setAttribute("pendingOTP", otp);

            session.setAttribute("pendingEmail",email);
            session.setAttribute("pendingPassword",passwordEncoder.encode(password));

            session.setAttribute("pendingName",name);
            session.setAttribute("pendingRollNo",rollNumber);
            session.setAttribute("pendingPhoneNumber",phoneNumber);
            session.setAttribute("pendingUniversity",university);
            session.setAttribute("pendingSection",section);
            session.setAttribute("pendingGender",gender);


            session.setAttribute("resetEmail",email);
            session.setAttribute("otpSentTime",System.currentTimeMillis());
            session.setAttribute("flow","register");

            return "redirect:/auth/verify-otp";

        }catch (Exception e) {
            log.error("Registration failed for email {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to create account. Please try again.");
        }
        return "redirect:/auth/register";
    }

    @GetMapping("/pending")
    public String showPendingPage(){
        return "/auth/pending";
    }
}
