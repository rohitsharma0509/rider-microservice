package com.scb.rider.exception;

public class MandatoryFieldMissingException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public MandatoryFieldMissingException(final String message) {
		super(message);
	}
}
