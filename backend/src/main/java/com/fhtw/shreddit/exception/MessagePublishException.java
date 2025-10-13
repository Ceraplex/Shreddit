package com.fhtw.shreddit.exception;

public class MessagePublishException extends ShredditException {
    public MessagePublishException(String message) {
        super(message);
    }
    
    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}