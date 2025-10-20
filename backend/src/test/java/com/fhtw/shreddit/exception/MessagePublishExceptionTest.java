package com.fhtw.shreddit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagePublishExceptionTest {

    @Test
    void constructorWithMessageSetsMessage() {
        MessagePublishException ex = new MessagePublishException("publish failed");
        assertEquals("publish failed", ex.getMessage());
        assertNull(ex.getCause());
        assertTrue(ex instanceof ShredditException);
    }

    @Test
    void constructorWithMessageAndCauseSetsBoth() {
        Throwable cause = new IllegalStateException("broker down");
        MessagePublishException ex = new MessagePublishException("publish failed", cause);
        assertEquals("publish failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
