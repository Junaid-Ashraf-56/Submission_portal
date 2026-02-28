package com.web.submission_portal.service;

import com.web.submission_portal.entity.EmailLog;
import com.web.submission_portal.enums.EmailStatus;
import com.web.submission_portal.repository.EmailLogsRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogsRepository emailLogsRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int EMAIL_TIMEOUT_SECONDS = 20;

    public void sendOTPEmail(String toEmail, String otp) {
        String subject = "Password Reset OTP - Assignment Portal";
        String htmlContent = buildOTPEmailTemplate(otp);

        try {
            sendEmailWithTimeout(toEmail, subject, htmlContent);
        } catch (TimeoutException e) {
            log.error("Email sending timed out after {} seconds for: {}", EMAIL_TIMEOUT_SECONDS, toEmail);
            throw new RuntimeException("Email sending timed out. Please try again.");
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendPasswordResetConfirmation(String toEmail) {
        String subject = "Password Reset Successful - Assignment Portal";
        String htmlContent = buildPasswordResetConfirmationTemplate();

        try {
            sendEmailWithTimeout(toEmail, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to send confirmation email: {}", e.getMessage());
        }
    }

    public void sendContactFormInfo(String name,String email,String topic,String message){
        String subject = "Problem face by Student - Assignment Portal";
        String htmlContent = buildContactFormTemplate(name,email,topic,message);

        try {
            sendEmailWithTimeout(fromEmail,subject,htmlContent);
        }catch (Exception e){
            log.error("Check your internet connection: {}",e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    private void sendEmailWithTimeout(String toEmail, String subject, String htmlContent)
            throws Exception {

        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(toEmail);
        emailLog.setSubject(subject);
        emailLog.setStatus(EmailStatus.PENDING);

        try {
            CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(() -> {
                try {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                    helper.setFrom(fromEmail);
                    helper.setTo(toEmail);
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true);

                    mailSender.send(message);
                    log.info("Email sent successfully to: {}", toEmail);
                } catch (MessagingException e) {
                    throw new RuntimeException("Email sending failed: " + e.getMessage());
                }
            });

            emailFuture.get(EMAIL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            emailLog.setStatus(EmailStatus.SENT);

        } catch (TimeoutException e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLogsRepository.save(emailLog);
            throw e;
        } catch (Exception e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLogsRepository.save(emailLog);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        } finally {
            emailLogsRepository.save(emailLog);
        }
    }
    private String buildContactFormTemplate(String name, String email, String topic, String message) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 3px solid #667eea; }
                    .logo { font-size: 24px; font-weight: bold; color: #667eea; }
                    .content { color: #333; line-height: 1.6; }
                    .info-box { background-color: #f7fafc; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #667eea; }
                    .label { font-weight: bold; color: #667eea; display: block; margin-bottom: 5px; }
                    .value { color: #2d3748; }
                    .message-box { background-color: #fff; border: 2px solid #e2e8f0; padding: 15px; border-radius: 8px; margin: 20px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🎓 Assignment Portal</div>
                        <p style="color: #718096; margin-top: 10px;">New Contact Form Submission</p>
                    </div>
                    
                    <div class="content">
                        <p>You have received a new message from the contact form:</p>
                        
                        <div class="info-box">
                            <span class="label">Name:</span>
                            <span class="value">%s</span>
                        </div>
                        
                        <div class="info-box">
                            <span class="label">Email:</span>
                            <span class="value">%s</span>
                        </div>
                        
                        <div class="info-box">
                            <span class="label">Subject:</span>
                            <span class="value">%s</span>
                        </div>
                        
                        <div class="message-box">
                            <span class="label">Message:</span>
                            <p class="value">%s</p>
                        </div>
                        
                        <p style="color: #718096; font-size: 14px; margin-top: 20px;">
                            <strong>Note:</strong> Please respond to this inquiry at the sender's email address.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>This is an automated email from Assignment Portal contact form.</p>
                        <p>&copy; 2024 Assignment Portal. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, email, topic, message);
    }
    private String buildOTPEmailTemplate(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 24px; font-weight: bold; color: #4F46E5; }
                    .otp-box { background-color: #4F46E5; color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; border-radius: 8px; letter-spacing: 5px; margin: 30px 0; }
                    .content { color: #333; line-height: 1.6; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; font-size: 12px; }
                    .warning { background-color: #FEF3C7; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #F59E0B; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🎓 Assignment Portal</div>
                    </div>
                   \s
                    <div class="content">
                        <h2>Password Reset Request</h2>
                        <p>Hello,</p>
                        <p>We received a request to reset your password. Use the OTP code below to proceed:</p>
                       \s
                        <div class="otp-box">
                           \s""" + otp + """
                        </div>
                       \s
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul style="margin: 10px 0;">
                                <li>This OTP is valid for <strong>1 minute (60 seconds)</strong></li>
                                <li>Do not share this code with anyone</li>
                                <li>If you didn't request this, please ignore this email</li>
                            </ul>
                        </div>
                       \s
                        <p>If you have any issues, please contact support.</p>
                        <p>Best regards,<br><strong>Assignment Portal Team</strong></p>
                    </div>
                   \s
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2024 Assignment Portal. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
           \s""";
    }

    private String buildPasswordResetConfirmationTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 24px; font-weight: bold; color: #4F46E5; }
                    .success-icon { font-size: 48px; text-align: center; margin: 20px 0; }
                    .content { color: #333; line-height: 1.6; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; font-size: 12px; }
                    .alert { background-color: #DBEAFE; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #3B82F6; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🎓 Assignment Portal</div>
                    </div>
                   \s
                    <div class="success-icon">✅</div>
                   \s
                    <div class="content">
                        <h2 style="text-align: center;">Password Reset Successful!</h2>
                        <p>Hello,</p>
                        <p>Your password has been successfully reset. You can now log in with your new password.</p>
                       \s
                        <div class="alert">
                            <strong>🔒 Security Tip:</strong>
                            <p style="margin: 10px 0;">If you didn't make this change, please contact support immediately.</p>
                        </div>
                       \s
                        <p style="text-align: center; margin-top: 30px;">
                            <a href="http://localhost:8080/login" style="background-color: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">Login Now</a>
                        </p>
                       \s
                        <p>Best regards,<br><strong>Assignment Portal Team</strong></p>
                    </div>
                   \s
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2024 Assignment Portal. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
           \s""";
    }
}