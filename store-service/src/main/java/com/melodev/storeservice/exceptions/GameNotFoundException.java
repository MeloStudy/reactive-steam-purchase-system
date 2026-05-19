package com.melodev.storeservice.exceptions;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String id) {
        super("Game with id " + id + " not found");
    }
}
