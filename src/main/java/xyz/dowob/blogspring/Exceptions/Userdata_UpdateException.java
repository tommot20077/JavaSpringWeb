package xyz.dowob.blogspring.Exceptions;

public class Userdata_UpdateException extends Exception {

    public ErrorCode getErrorCode() {
        return errorCode;
    }
    private final ErrorCode errorCode;



    public Userdata_UpdateException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public enum  ErrorCode {
        USERNAME_ALREADY_EXISTS("用戶名已經存在"),
        USERNAME_CONTAINS_ILLEGAL_CHARACTERS("用戶名包含非法字符"),
        PASSWORD_LENGTH_INVALID("密碼長度不符合要求"),
        PASSWORD_CONTAINS_USERNAME("密碼不能包含用戶名"),
        PASSWORD_COMPLEXITY_INSUFFICIENT("密碼複雜度不足"),
        PASSWORD_NOT_MATCH("輸入的密碼不一致"),
        EMAIL_ALREADY_EXISTS("電子郵件已經存在");



        private final String errorMessage;
        ErrorCode(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

