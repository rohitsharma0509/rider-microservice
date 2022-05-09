package com.scb.rider.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RiderJobStatusException extends RuntimeException {
	 private String errorCode;
	 private String errorMessage;
}
