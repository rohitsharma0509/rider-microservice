package com.scb.rider.exception;

public class AccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AccessDeniedException(final String message) {
		super(message);
	}
}
