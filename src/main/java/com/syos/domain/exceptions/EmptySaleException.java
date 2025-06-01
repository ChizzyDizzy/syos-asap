package com.syos.domain.exceptions;

public class EmptySaleException extends RuntimeException {
    public EmptySaleException(String message) {
        super(message);
    }
}
