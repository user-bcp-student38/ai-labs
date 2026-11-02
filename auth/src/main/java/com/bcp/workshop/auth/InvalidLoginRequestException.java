package com.bcp.workshop.auth;

/** Se lanza cuando el request de login no trae username/password válidos. */
public class InvalidLoginRequestException extends RuntimeException {
    public InvalidLoginRequestException(String message) {
        super(message);
    }
}
