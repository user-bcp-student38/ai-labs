package com.bcp.workshop.auth;

/** Fila persistida de usuario. El hash de contraseña nunca sale de esta capa. */
public record UserRecord(long id, String username, String passwordHash, String displayName, String role) {}
