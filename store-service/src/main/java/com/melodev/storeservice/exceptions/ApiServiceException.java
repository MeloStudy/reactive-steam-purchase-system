package com.melodev.storeservice.exceptions;

public class ApiServiceException extends RuntimeException {
    public ApiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
