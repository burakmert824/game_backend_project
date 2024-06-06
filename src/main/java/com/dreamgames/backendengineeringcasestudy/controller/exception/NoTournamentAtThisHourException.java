package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class NoTournamentAtThisHourException extends RuntimeException {
    public NoTournamentAtThisHourException(String message) {
        super(message);
    }
}
