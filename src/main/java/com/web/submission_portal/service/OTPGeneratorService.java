package com.web.submission_portal.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;


@Service
public class OTPGeneratorService {

    private static final SecureRandom random = new SecureRandom();

    public String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}