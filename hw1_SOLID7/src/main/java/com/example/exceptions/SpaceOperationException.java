package com.example.exceptions;


public class SpaceOperationException extends RuntimeException {
    public SpaceOperationException(String message) {
        super(message);
    }

    public SpaceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}