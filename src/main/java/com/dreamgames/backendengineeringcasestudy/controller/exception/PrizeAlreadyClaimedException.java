package com.dreamgames.backendengineeringcasestudy.controller.exception;

public class PrizeAlreadyClaimedException extends RuntimeException {
    public PrizeAlreadyClaimedException(String message) {
        super(message);
    }
}
