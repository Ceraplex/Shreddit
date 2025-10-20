package com.fhtw.shreddit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShredditExceptionTest {

    @Test
    void constructorWithMessageSetsMessage() {
        ShredditException ex = new ShredditException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructorWithMessageAndCauseSetsBoth() {
        Throwable cause = new IllegalStateException("test cause");
        ShredditException ex = new ShredditException("test message", cause);
        assertEquals("test message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}