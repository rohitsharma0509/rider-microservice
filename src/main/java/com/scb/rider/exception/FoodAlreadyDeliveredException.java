package com.scb.rider.exception;

public class FoodAlreadyDeliveredException extends RuntimeException{
    public FoodAlreadyDeliveredException(String message) {
        super(message);
    }
}
