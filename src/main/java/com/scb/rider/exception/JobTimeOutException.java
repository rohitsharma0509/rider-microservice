package com.scb.rider.exception;

public class JobTimeOutException extends RuntimeException {

    public JobTimeOutException() {
        super();
    }

    public JobTimeOutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JobTimeOutException(final String message) {
        super(message);
    }

    public JobTimeOutException(final Throwable cause) {
        super(cause);
    }
}