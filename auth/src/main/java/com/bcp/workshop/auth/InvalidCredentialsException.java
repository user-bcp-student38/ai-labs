package com.bcp.workshop.auth;

/** Se lanza cuando el usuario no existe o la contraseña no coincide. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
