package xyz.dowob.blogspring.service;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
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










    public User getUserById(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("找不到ID為" + id + "的使用者。"));
    }

    public Page<User> getAllUsersWithPge(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }


    public User updateUser(Long id, User updateUser) {
        return userRepository.findById(id)
            .map(user -> {
                user.setUsername(updateUser.getUsername());
                user.setPassword(updateUser.getPassword());
                /*user.setEmail(updateUser.getEmail());*/
                /*user.setBirthdate(updateUser.getBirthdate());*/
                return userRepository.save(user);
            })
            .orElseGet(()->{
                updateUser.setId(id);
                return userRepository.save(updateUser);
            });
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
        }else{
            throw new UsernameNotFoundException("找不到ID為" + id + "的使用者。");
        }
    }

}
