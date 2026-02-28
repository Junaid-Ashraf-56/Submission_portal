package com.web.submission_portal.controller;

import com.web.submission_portal.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LandingController {

    private final EmailService emailService;

    public LandingController(EmailService emailService) {
        this.emailService = emailService;
    }

    //    Landing page
    @GetMapping("/")
    public String landingPage(){
        return "landing/landing";
    }

//    handle contact page

    @PostMapping("/contact")
    public String handleContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes
    ){
        try{
            //implementation for email
            emailService.sendContactFormInfo(name,email,subject,message);
            redirectAttributes.addFlashAttribute("success","Your message is send");
            return "redirect:/#contact";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error","Failed to send message");
            return "redirect:/#contact";
        }
    }

}
