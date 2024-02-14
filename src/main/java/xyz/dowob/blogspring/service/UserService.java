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
import xyz.dowob.blogspring.repository.TokenRepository;
import xyz.dowob.blogspring.repository.UserRepository;

@Service
public class UserService{

    private final UserRepository userRepository;

    private final UserInspection userInspection;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;



    @Autowired
    public UserService(UserInspection userInspection, UserRepository userRepository, TokenService tokenService, TokenRepository tokenRepository) {
        this.userInspection = userInspection;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
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
            if (user.getEmail() != null) {
                tokenService.sendVerificationEmail(user);
            }
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

    public void updateUser(User newInputUser, User repositoryUser, String confirmPassword, String originPassword) throws Userdata_UpdateException {
        if (StringUtils.isNotBlank(originPassword) && authenticate(repositoryUser.getUsername(), originPassword)) {
            if (StringUtils.isNotBlank(newInputUser.getPassword()) && newInputUser.getPassword().equals(confirmPassword)) {
                if (userInspection.isValidPassword(newInputUser.getPassword(), newInputUser.getUsername())) {
                    repositoryUser.setPassword(UserHashMethod.hashPassword(newInputUser.getPassword()));
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

    public Long getUserByUsernameOrEmail(String username_or_email) throws Userdata_UpdateException {
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
        return user.getId();
    }
    public void sendResetPasswordMail(Long id) throws Userdata_UpdateException {
        User user = getUserById(id);
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
                    user.setPassword(UserHashMethod.hashPassword(newPassword));
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
