package com.bcp.workshop.auth;

/** Contrato de respuesta de {@code POST /auth/login}. */
public record LoginResponseDto(String token, long expiresIn, UserSummaryDto user) {}
