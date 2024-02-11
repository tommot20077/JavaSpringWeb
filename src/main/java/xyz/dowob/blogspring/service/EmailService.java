package xyz.dowob.blogspring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.model.VerificationToken;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.TokenRepository;

import java.util.UUID;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TokenRepository tokenRepository;
    @Autowired
    public EmailService(JavaMailSender javaMailSender, TokenRepository tokenRepository) {
        this.javaMailSender = javaMailSender;
        this.tokenRepository = tokenRepository;
    }

    public void sendVerificationEmail(String to, String subject, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tommot50@tommot20067.onmicrosoft.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText("請點擊以下鏈接完成驗證 "+verificationLink);
        javaMailSender.send(message);
    }
    public String createVerificationCode(User user){
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
        return verificationToken.getToken();
    }

}
