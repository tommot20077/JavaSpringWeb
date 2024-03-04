package xyz.dowob.blogspring.service;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.functions.UserHashMethod;
import xyz.dowob.blogspring.functions.UserInspection;
import xyz.dowob.blogspring.model.PersistentLogin;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.PersistentLoginRepository;
import xyz.dowob.blogspring.repository.TokenRepository;
import xyz.dowob.blogspring.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class UserService{

    private final UserRepository userRepository;

    private final UserInspection userInspection;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final PersistentLoginRepository persistentLoginRepository;




    @Autowired
    public UserService(UserInspection userInspection, UserRepository userRepository, TokenService tokenService, TokenRepository tokenRepository, PersistentLoginRepository persistentLoginRepository) {
        this.userInspection = userInspection;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.persistentLoginRepository = persistentLoginRepository;

    }

    public void registerUser(User user, String confirmPassword) throws Userdata_UpdateException {
        if (!user.getPassword().equals(confirmPassword)) {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (userInspection.isValidUsername(user.getUsername())) {
            if (userInspection.isValidPassword(user.getPassword(), user.getUsername())) {
                user.setPassword(UserHashMethod.argonMethod(user.getPassword()));
            }
            user.setEmail(userInspection.hasEmail(user.getEmail()));
            userRepository.save(user);
            if (user.getEmail() != null) {
                tokenService.sendVerificationEmail(user);
            }
        }
    }



    public Boolean authenticate(String username, String password){
        return userRepository.findByUsername(username)
                .map(user -> UserHashMethod.argonMethodVerifyInRepository(user.getPassword(), password))
                .orElse(false);

    }

    public void updateUser(User newInputUser, User repositoryUser, String confirmPassword, String originPassword) throws Userdata_UpdateException {
        if (StringUtils.isNotBlank(originPassword) && authenticate(repositoryUser.getUsername(), originPassword)) {

            String checkUsername;
            if (StringUtils.isNotBlank(newInputUser.getUsername()) && newInputUser.getUsername().equals(repositoryUser.getUsername())){
                checkUsername = repositoryUser.getUsername();
            }else if (StringUtils.isNotBlank(newInputUser.getUsername())){
                if (userInspection.isValidUsername(newInputUser.getUsername())) {
                    repositoryUser.setUsername(newInputUser.getUsername());
                    checkUsername = newInputUser.getUsername();
                } else {
                    throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USERNAME_ALREADY_EXISTS);
                }
            }else {
                throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USERNAME_CONTAINS_ILLEGAL_CHARACTERS);
            }


            if (StringUtils.isNotBlank(newInputUser.getPassword()) && newInputUser.getPassword().equals(confirmPassword)) {
                if (userInspection.isValidPassword(newInputUser.getPassword(), checkUsername)) {
                    repositoryUser.setPassword(UserHashMethod.argonMethod(newInputUser.getPassword()));
                }
            } else if (StringUtils.isNotBlank(newInputUser.getPassword())) {
                throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_NOT_MATCH);
            }


            if (newInputUser.getBirthdate() != null && !newInputUser.getBirthdate().equals(repositoryUser.getBirthdate())) {
                repositoryUser.setBirthdate(newInputUser.getBirthdate());
            }


            boolean emailChanged = false;

            if(StringUtils.isNotBlank(newInputUser.getEmail()) && !newInputUser.getEmail().equals(repositoryUser.getEmail())){
                // 如果電子郵件在資料庫中不存在，則不應拋出異常
                if (userInspection.hasEmail(newInputUser.getEmail()) != null) {
                    repositoryUser.setEmail(newInputUser.getEmail());
                    emailChanged = true;
                    repositoryUser.setEmailActiveStatus(false);
                } else {
                    throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.EMAIL_ALREADY_EXISTS);
                }
            }
            userRepository.save(repositoryUser);


            if (emailChanged) {
                tokenService.sendVerificationEmail(repositoryUser);
            }

        } else {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_WRONG);
        }
    }

    public void updateBasicUser(User newInputUser, User repositoryUser) {
        repositoryUser.setBirthdate(newInputUser.getBirthdate());
        userRepository.save(repositoryUser);
    }


    public void sendResetPasswordMail(User user) throws Userdata_UpdateException {
        if (user.getEmailActiveStatus()) {
            tokenService.sendResetPasswordEmail(user);
        } else {
            throw new UsernameNotFoundException("使用者" + user.getUsername() + "的電子郵件尚未驗證，無法找回密碼。");
        }



    }
    public void resetPassword(User user, String token, String newPassword, String confirmPassword) throws Userdata_UpdateException {
        if (user == null) throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USER_NOT_FOUND);
        if (tokenService.resetPasswordVerifyToken(token)) {
            if (newPassword.equals(confirmPassword)) {
                if (userInspection.isValidPassword(newPassword, user.getUsername())) {
                    user.setPassword(UserHashMethod.argonMethod(newPassword));
                    userRepository.save(user);
                    tokenRepository.delete(tokenRepository.findByToken(token).orElseThrow(() -> new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.TOKEN_INVALID)));
                } else {
                    throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_NOT_MATCH);
                }
            } else {
                throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_NOT_MATCH);
            }
        } else {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.TOKEN_INVALID);
        }
    }

    public User getUserByUsernameOrEmail(String username_or_email) throws Userdata_UpdateException {
        User user;
        try {
            user = getUserByUsername(username_or_email);
        } catch (UsernameNotFoundException e) {
            try {
                user = getUserByEmail(username_or_email);
            } catch (UsernameNotFoundException e1) {
                throw new UsernameNotFoundException("找不到" + username_or_email + "的使用者資料。");
            }
        }
        return user;
    }

    public User getUserById(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("找不到ID為" + id + "的使用者。"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("找不到名為" + username + "的使用者。"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("找不到電子郵件為" + email + "的使用者。"));
    }





    public void deleteUser(Long id) {
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
        }else{
            throw new UsernameNotFoundException("找不到ID為" + id + "的使用者。");
        }
    }

    public Long verifyRememberMeToken(String base64Token) {
        if (base64Token == null) return null;
        String argonToken = new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);
        PersistentLogin persistentLogin = persistentLoginRepository.findByToken(argonToken).orElse(null);
        if (persistentLogin != null){
            if (persistentLogin.getExpiryTime().isAfter(java.time.LocalDateTime.now())) {
            return persistentLogin.getUser().getId();}
        }
        return null;
    }



    public void createRememberMeCookie(HttpServletResponse response, Long userId){
        String rememberMeToken = UUID.randomUUID().toString();
        String hashRememberMeToken = UserHashMethod.argonMethod(rememberMeToken);
        String base64RememberMeToken = Base64.getEncoder().encodeToString(hashRememberMeToken.getBytes(StandardCharsets.UTF_8));

        int expireTimeDays = 14;
        PersistentLogin persistentLogin = new PersistentLogin();
        persistentLogin.setUser(getUserById(userId));
        persistentLogin.createOrUpdateUserRememberMeToken(hashRememberMeToken, expireTimeDays);
        persistentLoginRepository.save(persistentLogin);

        Cookie rememberMeCookie = new Cookie("REMEMBER_ME", base64RememberMeToken);
        rememberMeCookie.setMaxAge(60 * 60 * 24 * expireTimeDays);
        rememberMeCookie.setPath("/");
        rememberMeCookie.setHttpOnly(true);
        rememberMeCookie.setSecure(true);
        response.addCookie(rememberMeCookie);
    }



    public void deleteRememberMeCookie(HttpServletResponse response, HttpSession session, Cookie[] cookies){
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("REMEMBER_ME")) {
                    Long userId = (Long) session.getAttribute("currentUserId");
                    persistentLoginRepository.findByUser_Id(userId).ifPresent(persistentLoginRepository::delete);
                    Cookie rememberMeCookie = new Cookie("REMEMBER_ME", null);
                    rememberMeCookie.setMaxAge(0);
                    rememberMeCookie.setPath("/");
                    rememberMeCookie.setHttpOnly(true);
                    rememberMeCookie.setSecure(true);
                    response.addCookie(rememberMeCookie);
                }
            }
        }
    }
}
