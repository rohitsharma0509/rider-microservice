package com.scb.rider.exception;

public class StatusTransitionNotAllowedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StatusTransitionNotAllowedException(final String message) {
		super(message);
	}
}
