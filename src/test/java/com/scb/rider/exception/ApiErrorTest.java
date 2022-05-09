package com.scb.rider.exception;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiErrorTest {

    ApiError apiError = new ApiError();
    ApiError apiError1 = new ApiError(HttpStatus.ACCEPTED, "message", "message");
    ApiError apiError2 = new ApiError(HttpStatus.ACCEPTED, "message", new ArrayList<>());

    @BeforeAll
    static void setup() {

    }

    @Test
    public void testCopyProperties() {
        apiError.setError("error");
        apiError.setStatus(HttpStatus.ACCEPTED);
        apiError.setErrors(new ArrayList<>());
        assertNotNull(apiError);
        assertNotNull(apiError1);
        assertNotNull(apiError2);
    }
}