package com.scb.rider.exception;

public class AppointmentIdNotFoundException extends RuntimeException{

	public AppointmentIdNotFoundException(String message) {
		super(message);
	}
}
