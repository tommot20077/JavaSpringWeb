package xyz.dowob.blogspring.service;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import xyz.dowob.blogspring.UserException.RegisterException;
import xyz.dowob.blogspring.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.functions.UserHashMethod;
import xyz.dowob.blogspring.functions.UserInspection;
import xyz.dowob.blogspring.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService{

    private final UserRepository userRepository;
    private final UserInspection userInspection;

    @Autowired
    public UserService(UserInspection userInspection,UserRepository userRepository) {
        this.userInspection = userInspection;
        this.userRepository = userRepository;
    }

    public void registerUser(User user) throws RegisterException {
        if (userInspection.isValidUsername(user.getUsername())){
            if (userInspection.isValidPassword(user.getPassword(), user.getUsername())){
                user.setPassword(UserHashMethod.hashPassword(user.getPassword()));
            }
            user.setEmail(userInspection.hasEmail(user.getEmail()));


        }
        userRepository.save(user);
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

    public void updateUser(User currentUser) throws RegisterException {

        User repositoryUser = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("找不到名為" + currentUser.getUsername() + "的使用者。"));

        if(StringUtils.isNotBlank(currentUser.getPassword())) {
            if(!currentUser.getPassword().equals(repositoryUser.getPassword())) {
                if (userInspection.isValidPassword(currentUser.getPassword(), currentUser.getUsername())) {
                    repositoryUser.setPassword(UserHashMethod.hashPassword(currentUser.getPassword()));
                }else {
                    throw new RegisterException(RegisterException.ErrorCode.PASSWORD_COMPLEXITY_INSUFFICIENT);
                }

            }

        }

        if(StringUtils.isNotBlank(currentUser.getEmail()) && !currentUser.getEmail().equals(repositoryUser.getEmail())){
            // 如果電子郵件在資料庫中不存在，則不應拋出異常
            if(userRepository.findByEmail(currentUser.getEmail()).isEmpty() || currentUser.getEmail().equals(repositoryUser.getEmail())) {
                repositoryUser.setEmail(currentUser.getEmail());
            }else {
                throw new RegisterException(RegisterException.ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        if (currentUser.getBirthdate() != null) {
            repositoryUser.setBirthdate(currentUser.getBirthdate());
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
