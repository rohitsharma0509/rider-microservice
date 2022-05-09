package com.scb.rider.exception;

public class TrainingSlotEmptyException extends RuntimeException{

    public TrainingSlotEmptyException(String message) {
        super(message);
    }
}