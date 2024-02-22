package xyz.dowob.blogspring.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.repository.UserRepository;

import java.util.regex.Pattern;

@Service
public class UserInspection {
    private final UserRepository userRepository;
    @Autowired
    public UserInspection(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean isValidPassword(String password,String username) throws Userdata_UpdateException {
        if(password == null || password.trim().isEmpty()) throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_LENGTH_INVALID);
        if(password.length() < 8) throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_LENGTH_INVALID);
        if(password.equals(username)) throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_CONTAINS_USERNAME);
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for(char ch : password.toCharArray()){
            if(Character.isUpperCase(ch)) hasUpper = true;
            if(Character.isLowerCase(ch)) hasLower = true;
            if(Character.isDigit(ch)) hasDigit = true;
        }
        if(!(hasUpper && hasLower && hasDigit)) {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.PASSWORD_COMPLEXITY_INSUFFICIENT);
        }
        return true;

    }





    public boolean isValidUsername(String username) throws Userdata_UpdateException {
        if(userRepository.findByUsername(username).isPresent()) {
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if(!Pattern.matches("^[a-zA-Z0-9]+$", username)){
            throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.USERNAME_CONTAINS_ILLEGAL_CHARACTERS);
        }
        return true;
    }


    public String hasEmail(String email) throws Userdata_UpdateException {
        if (email != null && !email.trim().isEmpty()) {
            if (userRepository.findByEmail(email).isPresent()){
                throw new Userdata_UpdateException(Userdata_UpdateException.ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            return email;
        } else {
           return null;
        }
    }

}

