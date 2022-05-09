package com.scb.rider.exception;

public class JobNotAcceptedException extends RuntimeException {

    public JobNotAcceptedException() {
        super();
    }

    public JobNotAcceptedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JobNotAcceptedException(final String message) {
        super(message);
    }

    public JobNotAcceptedException(final Throwable cause) {
        super(cause);
    }
}