package com.fhtw.shreddit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentCreationExceptionTest {

    @Test
    void constructorWithMessageSetsMessage() {
        DocumentCreationException ex = new DocumentCreationException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
        assertTrue(ex instanceof DocumentException);
        assertTrue(ex instanceof ShredditException);
    }

    @Test
    void constructorWithMessageAndCauseSetsBoth() {
        Throwable cause = new IllegalStateException("test cause");
        DocumentCreationException ex = new DocumentCreationException("test message", cause);
        assertEquals("test message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}