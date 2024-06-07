package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class NotEnoughCompetitorsException extends RuntimeException {
    public NotEnoughCompetitorsException(String message) {
        super(message);
    }
}
