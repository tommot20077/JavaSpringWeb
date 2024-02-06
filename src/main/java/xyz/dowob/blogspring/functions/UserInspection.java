package xyz.dowob.blogspring.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.UserException.RegisterException;
import xyz.dowob.blogspring.repository.UserRepository;
import java.util.regex.Pattern;

@Service
public class UserInspection {
    private final UserRepository userRepository;
    @Autowired
    public UserInspection(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean isValidPassword(String password,String username) throws RegisterException {
        if(password.length() < 8) throw new RegisterException(RegisterException.ErrorCode.PASSWORD_LENGTH_INVALID);
        if(password.equals(username)) throw new RegisterException(RegisterException.ErrorCode.PASSWORD_CONTAINS_USERNAME);
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for(char ch : password.toCharArray()){
            if(Character.isUpperCase(ch)) hasUpper = true;
            if(Character.isLowerCase(ch)) hasLower = true;
            if(Character.isDigit(ch)) hasDigit = true;
        }
        if(!(hasUpper && hasLower && hasDigit)) {
            throw new RegisterException(RegisterException.ErrorCode.PASSWORD_COMPLEXITY_INSUFFICIENT);
        }
        return true;

    }





    public boolean isValidUsername(String username) throws RegisterException {
        if(userRepository.findByUsername(username).isPresent()) {
            throw new RegisterException(RegisterException.ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if(!Pattern.matches("^[a-zA-Z0-9]+$", username)){
            throw new RegisterException(RegisterException.ErrorCode.USERNAME_CONTAINS_ILLEGAL_CHARACTERS);
        }
        return true;
    }
    }

