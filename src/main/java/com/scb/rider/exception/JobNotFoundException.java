package com.scb.rider.exception;

public class JobNotFoundException extends RuntimeException{

  public JobNotFoundException(final String message) {
    super(message);
  }
}
