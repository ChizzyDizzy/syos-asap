package com.syos.domain.exceptions;

public class InvalidItemCodeException extends RuntimeException {
    public InvalidItemCodeException(String message) {
        super(message);
    }
}