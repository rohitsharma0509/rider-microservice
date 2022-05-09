package com.scb.rider.exception;

public class JobAlreadyAcceptedException extends RuntimeException {

    public JobAlreadyAcceptedException(final String message) {
        super(message);
    }
}

