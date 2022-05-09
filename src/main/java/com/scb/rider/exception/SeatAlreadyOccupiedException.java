package com.scb.rider.exception;

public class SeatAlreadyOccupiedException extends RuntimeException {

    public SeatAlreadyOccupiedException(final String message) {
        super(message);
    }
}

