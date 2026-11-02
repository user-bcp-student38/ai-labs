package com.bcp.workshop.auth;

/** Datos de usuario expuestos al cliente tras un login exitoso. */
public record UserSummaryDto(String username, String displayName, String role) {}
