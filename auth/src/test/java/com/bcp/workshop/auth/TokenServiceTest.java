package com.bcp.workshop.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HU-01: caracteriza el contrato compartido con los verificadores del JWT
 * (p. ej. {@code api/app/main.py::legacy_identity}): un token con {@code exp}
 * en el pasado debe ser rechazado al parsear/validar. `auth` no valida
 * tokens entrantes en ningún endpoint propio, por eso el token expirado se
 * construye aquí directamente con la misma librería y el mismo secreto que
 * usa {@link TokenService}, en vez de agregar un método de verificación
 * nuevo fuera del alcance aprobado.
 */
@QuarkusTest
class TokenServiceTest {
    private static final Duration PAST_TOKEN_LIFETIME = Duration.ofHours(1);

    @Inject
    TokenService tokenService;

    @Test
    void rejectsExpiredToken() {
        SecretKey key = tokenService.signingKey();
        Instant pastIssuedAt = Instant.now().minus(PAST_TOKEN_LIFETIME.multipliedBy(2));
        String expiredToken = Jwts.builder()
                .issuer(TokenService.TOKEN_ISSUER)
                .subject("student")
                .claim(TokenService.ROLE_CLAIM, "STUDENT")
                .claim(TokenService.CLIENT_CLAIM, "angular-workshop")
                .issuedAt(Date.from(pastIssuedAt))
                .expiration(Date.from(pastIssuedAt.plus(PAST_TOKEN_LIFETIME)))
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class,
                () -> Jwts.parser().verifyWith(key).build().parseSignedClaims(expiredToken));
    }
}
