package com.scb.rider.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortProfileException extends RuntimeException {

    private Integer code;
    public ShortProfileException(final String message, Integer code) {
        super(message);
        this.code = code;
    }
}
