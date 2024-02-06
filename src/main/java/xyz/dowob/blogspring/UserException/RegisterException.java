package xyz.dowob.blogspring.UserException;

public class RegisterException extends Exception {

    private final ErrorCode errorCode;
    public RegisterException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }



    public enum  ErrorCode {
        USERNAME_ALREADY_EXISTS("用戶名已經存在"),
        USERNAME_CONTAINS_ILLEGAL_CHARACTERS("用戶名包含非法字符"),
        PASSWORD_LENGTH_INVALID("密碼長度不符合要求"),
        PASSWORD_CONTAINS_USERNAME("密碼不能包含用戶名"),
        PASSWORD_COMPLEXITY_INSUFFICIENT("密碼複雜度不足"),
        PASSWORD_NOT_MATCH("輸入的密碼不一致");



        private final String errorMessage;
        ErrorCode(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
