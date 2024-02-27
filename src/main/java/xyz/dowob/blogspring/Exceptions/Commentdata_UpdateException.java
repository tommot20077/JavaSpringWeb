package xyz.dowob.blogspring.Exceptions;

public class Commentdata_UpdateException extends Exception {
    public Commentdata_UpdateException.ErrorCode getErrorCode() {
        return errorCode;
    }
    private final Commentdata_UpdateException.ErrorCode errorCode;
    public Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        CONTENT_TOO_LONG("內容太長"),
        COMMENT_UPDATE_FAILED("評論編輯時發生錯誤"),
        COMMENT_INVALID("內容不可為空"),
        UNAUTHORIZED("未授權的身分"),
        NOT_FOUND_USER("找不到用戶"),
        COMMENT_NOT_FOUND("找不到評論"),
        COMMENT_UPDATE_CAST_ERROR("類型轉換發生錯誤"),
        COMMENT_UPDATE_JSON_ERROR("轉換JSON時發生錯誤");



        private final String errorMessage;
        ErrorCode(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}


