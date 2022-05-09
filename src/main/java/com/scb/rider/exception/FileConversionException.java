package com.scb.rider.exception;

import java.io.IOException;

public class FileConversionException extends Exception {

  private String msg;

  public FileConversionException(IOException e) {
    this.msg = e.getMessage().toString();
  }
}
