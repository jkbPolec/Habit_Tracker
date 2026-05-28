package com.example.habittracker.exception;

public class DuplicateCompletionException extends RuntimeException {

    public DuplicateCompletionException(String message) {
        super(message);
    }
}
