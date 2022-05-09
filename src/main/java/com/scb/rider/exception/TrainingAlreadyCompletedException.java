package com.scb.rider.exception;

public class TrainingAlreadyCompletedException extends RuntimeException {

	public TrainingAlreadyCompletedException(final String message) {
		super(message);
	}
}
