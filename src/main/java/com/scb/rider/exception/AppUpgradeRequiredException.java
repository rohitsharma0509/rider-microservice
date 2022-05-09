package com.scb.rider.exception;

public class AppUpgradeRequiredException extends RuntimeException {
    public AppUpgradeRequiredException(String message) {
        super(message);
    }
}
