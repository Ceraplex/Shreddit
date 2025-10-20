package com.fhtw.shreddit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExceptionTest {

    @Test
    void constructorWithMessageSetsMessage() {
        DocumentException ex = new DocumentException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
        assertTrue(ex instanceof ShredditException);
    }

    @Test
    void constructorWithMessageAndCauseSetsBoth() {
        Throwable cause = new IllegalStateException("test cause");
        DocumentException ex = new DocumentException("test message", cause);
        assertEquals("test message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}