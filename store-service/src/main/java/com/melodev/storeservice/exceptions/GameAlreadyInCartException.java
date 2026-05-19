package com.melodev.storeservice.exceptions;

public class GameAlreadyInCartException extends RuntimeException {
    public GameAlreadyInCartException(String id) {
        super("Game with id " + id + " is already in Cart");
    }
}
