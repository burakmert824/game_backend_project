package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class InsufficientCoinsException extends RuntimeException {
    public InsufficientCoinsException(String message) {
        super(message);
    }
}

