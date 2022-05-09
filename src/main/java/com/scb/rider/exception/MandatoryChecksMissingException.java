package com.scb.rider.exception;

public class MandatoryChecksMissingException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public MandatoryChecksMissingException(final String message) {
		super(message);
	}
}
