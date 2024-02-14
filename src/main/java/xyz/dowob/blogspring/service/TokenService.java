package xyz.dowob.blogspring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.model.VerificationToken;
import xyz.dowob.blogspring.repository.TokenRepository;
import xyz.dowob.blogspring.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class TokenService {
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    @Autowired
    public TokenService(JavaMailSender javaMailSender, TokenRepository tokenRepository, UserRepository userRepository) {
        this.javaMailSender = javaMailSender;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public void sendVerificationEmail(User user) throws Userdata_UpdateException {
        if (canSendEmail(user)){
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, user);
            tokenRepository.save(verificationToken);

            String verificationLink = "https://900303.xyz/verify?token=" + token;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tommot50@tommot20067.onmicrosoft.com");
            message.setTo(user.getEmail());
            message.setSubject("請驗證您的電子郵件");
            message.setText("請點擊以下鏈接完成驗證\n"+verificationLink+"\n(此鏈接在發送後一小時內有效)");
            javaMailSender.send(message);
        }else{
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USER_SEND_EMAIL_LIMIT);
        }
    }
    public void sendResetPasswordEmail(User user)throws Userdata_UpdateException{
        if (canSendEmail(user)) {
            Random random = new Random();
            String token = Integer.toString((random.nextInt(900000)+100000));
            VerificationToken verificationToken = new VerificationToken(token, user);
            tokenRepository.save(verificationToken);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tommot50@tommot20067.onmicrosoft.com");
            message.setTo(user.getEmail());
            message.setSubject("請驗證您的電子郵件");
            message.setText("請複製下面代碼完成驗證\n"+token+"\n(此代碼在發送後一小時內有效)");
            javaMailSender.send(message);
        }else{
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USER_SEND_EMAIL_LIMIT);
        }
    }

    private synchronized boolean canSendEmail(User user){
        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(user.getResetTime())){
            user.setResetTime(now.plusHours(1));
            user.setCurrentSendTimes(0);
        }
        boolean canSend = user.getCurrentSendTimes() < 3;
        if(canSend){
            user.setCurrentSendTimes(user.getCurrentSendTimes() + 1);
            userRepository.save(user);
        }
        return canSend;
    }


    public boolean verifyActiveEmailToken(String token) throws Userdata_UpdateException {
        VerificationToken verificationToken = tokenRepository.findByToken(token).orElseThrow(() -> new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.TOKEN_INVALID));
        if (verificationToken != null && verificationToken.getExpiryDate().after(new Date())) {
            User user = verificationToken.getUser();
            if(user != null && !user.getEmailActiveStatus()){
                user.setEmailActiveStatus(true);
                userRepository.save(user);
                tokenRepository.delete(verificationToken);
                return true;
            }
        }
        return false;
    }
    public boolean resetPasswordVerifyToken(String token) throws Userdata_UpdateException {
        VerificationToken verificationToken = tokenRepository.findByToken(token).orElseThrow(() -> new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.TOKEN_INVALID));
        if (verificationToken != null && verificationToken.getExpiryDate().after(new Date())) {
            User user = verificationToken.getUser();
            return user != null;
        }
        return false;
    }

    public void removeExpiredTokens(){
        Date now = new Date();
        List<VerificationToken> expiredTokens = tokenRepository.findAllByExpiryDateLessThan(now);
        tokenRepository.deleteAll(expiredTokens);
    }
}
