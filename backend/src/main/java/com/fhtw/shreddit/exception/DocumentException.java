package com.fhtw.shreddit.exception;

public class DocumentException extends ShredditException {
    public DocumentException(String message) {
        super(message);
    }
    
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}