package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class AlreadyInTournamentException extends RuntimeException {
    public AlreadyInTournamentException(String message) {
        super(message);
    }
}
