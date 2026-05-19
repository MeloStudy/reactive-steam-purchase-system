package com.melodev.storeservice.exceptions;

public class GameNotAvailableException extends RuntimeException {
    public GameNotAvailableException(String id) {
        super("Game with id " + id + " is not available in Catalogue");
    }
}
