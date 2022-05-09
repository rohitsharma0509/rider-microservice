package com.scb.rider.exception;

import lombok.Getter;

@Getter
public class DocumentAlreadyApprovedException  extends RuntimeException {
    Object[] objects;
    public DocumentAlreadyApprovedException(final String message, Object[] objects) {
        super(message);
        this.objects = objects;
    }
}