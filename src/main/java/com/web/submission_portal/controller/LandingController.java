package com.web.submission_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

//    Landing page
    @GetMapping("/")
    public String landingPage(){
        return "landing/landing";
    }

//    handle contact page


}
