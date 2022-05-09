package com.scb.rider.exception;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataNotFoundExceptionTest {

    @BeforeAll
    static void setup() {

    }

    @Test
    public void testCopyProperties() {
        DataNotFoundException ex1 = new DataNotFoundException();
        DataNotFoundException ex2 = new DataNotFoundException("Exception");
        DataNotFoundException ex3 = new DataNotFoundException("Exception", new Throwable());
        DataNotFoundException ex4 = new DataNotFoundException(new Throwable());
        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
        assertNotNull(ex4);
    }
}