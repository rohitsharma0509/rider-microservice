package com.scb.rider.exception;


public class PdpaException extends RuntimeException {

    public PdpaException() {
        super();
    }

    public PdpaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PdpaException(final String message) {
        super(message);
    }

    public PdpaException(final Throwable cause) {
        super(cause);
    }
}