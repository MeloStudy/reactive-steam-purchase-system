package com.melodev.storeservice.config.advice;

import com.melodev.storeservice.exceptions.ApiServiceException;
import com.melodev.storeservice.exceptions.GameNotAvailableException;
import com.melodev.storeservice.exceptions.GameNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Simplifications: Not Exception hierarchy implemented, not rigorous status code definition

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error not handled: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Try again later."
        );
    }

    @ExceptionHandler(value = GameNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGameNotFound(GameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Game Not Found", ex.getMessage());
    }

    //Not Handling GameAlreadyInCartException for showcase purposes
    //@ExceptionHandler({GameAlreadyInCartException.class, GameNotAvailableException.class})
    @ExceptionHandler(GameNotAvailableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(RuntimeException ex) {
        // alternative 422 UNPROCESSABLE_ENTITY
        return buildResponse(HttpStatus.BAD_REQUEST, "Game Not Processable", ex.getMessage());
    }

    @ExceptionHandler(ApiServiceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleApiServiceError(ApiServiceException ex) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "External Service Error",
                ex.getMessage()
        );
    }

    private Mono<ResponseEntity<ErrorResponse>> buildResponse(HttpStatus status, String errorLabel, String message) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                errorLabel,
                message
        );

        return Mono.just(ResponseEntity.status(status).body(response));
    }

}
