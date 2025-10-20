package com.fhtw.shreddit.exception;

public class ShredditException extends RuntimeException {
    public ShredditException(String message) {
        super(message);
    }
    
    public ShredditException(String message, Throwable cause) {
        super(message, cause);
    }
}