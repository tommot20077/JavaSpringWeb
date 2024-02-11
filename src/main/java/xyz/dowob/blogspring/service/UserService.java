package xyz.dowob.blogspring.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.functions.UserHashMethod;
import xyz.dowob.blogspring.functions.UserInspection;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.model.VerificationToken;
import xyz.dowob.blogspring.repository.TokenRepository;
import xyz.dowob.blogspring.repository.UserRepository;

import java.util.Date;

@Service
public class UserService{

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final UserInspection userInspection;
    private final EmailService emailService;


    @Autowired
    public UserService(UserInspection userInspection, UserRepository userRepository, TokenRepository tokenRepository, EmailService emailService) {
        this.userInspection = userInspection;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    public void registerUser(User user, String confirmPassword) throws Userdata_UpdateException {
        if (!user.getPassword().equals(confirmPassword)) {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (userInspection.isValidUsername(user.getUsername())) {
            if (userInspection.isValidPassword(user.getPassword(), user.getUsername())) {
                user.setPassword(UserHashMethod.hashPassword(user.getPassword()));
            }
            user.setEmail(userInspection.hasEmail(user.getEmail()));
            userRepository.save(user);

            String token = emailService.createVerificationCode(user);
            String verificationLink = "http://localhost:8080/verify?token=" + token;
            emailService.sendVerificationEmail(user.getEmail(), "請驗證您的電子郵件", verificationLink);
        }
    }



    public Boolean authenticate(String username, String password){
        return userRepository.findByUsername(username)
                .map(user -> {
                    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
                    char[] passwordChars = password.toCharArray();
                    try {
                        return argon2.verify(user.getPassword(), passwordChars);
                    }finally {
                        argon2.wipeArray(passwordChars);
                    }
                })
                .orElse(false);

    }

    public void updateUser(User newInputUser, User repositoryUser, String confirmPassword) throws Userdata_UpdateException {

        if (StringUtils.isNotBlank(newInputUser.getPassword()) && newInputUser.getPassword().equals(confirmPassword)) {
            if (userInspection.isValidPassword(newInputUser.getPassword(), newInputUser.getUsername())) {
                repositoryUser.setPassword(UserHashMethod.hashPassword(newInputUser.getPassword()));
            }
        } else if (StringUtils.isNotBlank(newInputUser.getPassword())) {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_NOT_MATCH);
        }


        if(StringUtils.isNotBlank(newInputUser.getEmail()) && !newInputUser.getEmail().equals(repositoryUser.getEmail())){
            // 如果電子郵件在資料庫中不存在，則不應拋出異常
            if (userInspection.hasEmail(newInputUser.getEmail()) != null) {
                repositoryUser.setEmail(newInputUser.getEmail());
            } else {
                throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        if (newInputUser.getBirthdate() != null && !newInputUser.getBirthdate().equals(repositoryUser.getBirthdate())) {
            repositoryUser.setBirthdate(newInputUser.getBirthdate());
        }
        userRepository.save(repositoryUser);
    }



    public User getUserById(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("找不到ID為" + id + "的使用者。"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("找不到名為" + username + "的使用者。"));
    }

    public VerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }
    public boolean checkEmailIsVerified(String email, String token) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("找不到電子郵件為" + email + "的使用者。"));
        if (user.isActive()) {
            throw new UsernameNotFoundException("電子郵件為" + email + "的使用者已經驗證。");
        } else {
            return verifyToken(token);
        }
    }
    public boolean verifyToken(String token){
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken != null && verificationToken.getExpiryDate().after(new Date())) {
            User user = verificationToken.getUser();
            if(user != null && !user.isActive()){
                user.setActive(true);
                userRepository.save(user);
                tokenRepository.delete(verificationToken);
                return true;
            }
        }
        return false;
    }

    public Page<User> getAllUsersWithPge(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }



    public void deleteUser(Long id) {
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
        }else{
            throw new UsernameNotFoundException("找不到ID為" + id + "的使用者。");
        }
    }

}
