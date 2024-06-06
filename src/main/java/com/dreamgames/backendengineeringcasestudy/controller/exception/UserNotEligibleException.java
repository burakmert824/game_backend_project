package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class UserNotEligibleException extends RuntimeException {
    public UserNotEligibleException(String message) {
        super(message);
    }
}
