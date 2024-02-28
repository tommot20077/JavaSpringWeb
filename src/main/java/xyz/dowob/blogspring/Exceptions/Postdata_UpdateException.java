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
        POST_UPDATE_FAILED("文章編輯時發生錯誤"),

        POST_INVALID("標題與內容不可為空或僅有圖片"),
        DID_NOT_LOGIN("未登錄"),
        NOT_FOUND_USER("找不到用戶"),
        NOT_AUTHORIZED("未授權"),
        POST_NOT_FOUND("找不到文章"),
        POST_UPDATE_CAST_ERROR("類型轉換發生錯誤"),
        POST_UPDATE_JSON_ERROR("轉換JSON時發生錯誤");



        private final String errorMessage;
        ErrorCode(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
