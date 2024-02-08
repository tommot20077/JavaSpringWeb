package xyz.dowob.blogspring.Exceptions;

public class Postdata_UpdateException extends Exception{
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    private final ErrorCode errorCode;
    public Postdata_UpdateException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        TITLE_TOO_LONG("標題太長"),
        CONTENT_TOO_LONG("內容太長"),
        POSTDATA_UPDATE_FAILED("文章發表失敗"),
        DID_NOT_LOGIN("未登錄");

        private final String errorMessage;
        ErrorCode(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
