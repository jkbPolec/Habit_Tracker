package com.example.habittracker.exception;

public class ForbiddenResourceException extends RuntimeException {

    public ForbiddenResourceException(String message) {
        super(message);
    }
}
