package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class TournamentNotEndedException extends RuntimeException {
    public TournamentNotEndedException(String message) {
        super(message);
    }
}
