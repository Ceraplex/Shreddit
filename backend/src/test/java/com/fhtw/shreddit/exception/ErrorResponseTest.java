package com.fhtw.shreddit.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void gettersReturnValuesProvidedToConstructor() {
        ErrorResponse er = new ErrorResponse(400, "Bad request", "Missing field");
        assertEquals(400, er.getStatus());
        assertEquals("Bad request", er.getMessage());
        assertEquals("Missing field", er.getDetails());
    }

    @Test
    void settersUpdateValues() {
        ErrorResponse er = new ErrorResponse(200, "OK", "none");
        er.setStatus(500);
        er.setMessage("Internal error");
        er.setDetails("Stacktrace hidden");

        assertEquals(500, er.getStatus());
        assertEquals("Internal error", er.getMessage());
        assertEquals("Stacktrace hidden", er.getDetails());
    }
}
