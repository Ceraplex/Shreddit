package com.fhtw.shreddit.exception;

public class DocumentCreationException extends DocumentException {
    public DocumentCreationException(String message) {
        super(message);
    }
    
    public DocumentCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}