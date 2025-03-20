package vttp.project.server.services;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import vttp.project.server.repositories.MailRepository;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailRepository mailRepo;

    // Generate a random 6-digit verification code
    public String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    // Send email with verification code
    public void sendVerificationEmail(String toEmail, String verificationCode) throws MessagingException {
        // Save verification code
        mailRepo.saveCode(toEmail, verificationCode);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Password Reset Verification Code");
        helper.setText("<p>Your password reset code is: <b>" + verificationCode + "</b></p>", true);

        mailSender.send(message);
    }

    public String getCode(String email) {
        return mailRepo.getCode(email);
    }

    public boolean codeExists(String email) {
        return mailRepo.codeExists(email);
    }
}
