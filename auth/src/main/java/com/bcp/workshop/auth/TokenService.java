package com.bcp.workshop.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Emite los JWT de sesión: firmados (HS256), con expiración y claims
 * explícitos. El secreto de firma se externaliza a {@code AUTH_JWT_SECRET}
 * (variable de entorno); nunca vive embebido en el código.
 *
 * <p>Decisión técnica (HU-01): se mantiene {@code io.jsonwebtoken:jjwt} con
 * firma HMAC (HS256) en vez de migrar a otra librería JWT. Es la misma
 * librería y algoritmo ya validados por {@code AuthResourceTest} y por el
 * verificador en {@code api/app/main.py::legacy_identity} (PyJWT, HS256);
 * cambiarla ampliaría el alcance sin un problema concreto que lo justifique.
 */
@ApplicationScoped
public class TokenService {
    // Visibilidad de paquete (no `private`): las pruebas de caracterización
    // del JWT (mismo paquete) las reutilizan para no duplicar literales.
    static final String TOKEN_ISSUER = "workshop-auth";
    static final String ROLE_CLAIM = "role";
    static final String CLIENT_CLAIM = "client";
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    @ConfigProperty(name = "auth.jwt.secret")
    String signingSecret;

    public String create(String username, String role, String client) {
        SecretKey key = signingKey();
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .subject(username)
                .claim(ROLE_CLAIM, role)
                .claim(CLIENT_CLAIM, client)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(TOKEN_LIFETIME)))
                .signWith(key)
                .compact();
    }

    public long expirationSeconds() {
        return TOKEN_LIFETIME.toSeconds();
    }

    // Visibilidad de paquete: expone la clave de firma como método (no como
    // campo) para que las pruebas de caracterización del JWT, en el mismo
    // paquete, puedan decodificar sin duplicar el secreto. Acceder al campo
    // directamente en un bean CDI inyectado no funciona (el proxy no lo
    // delega), por eso es un método y no un getter de campo.
    SecretKey signingKey() {
        return Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
    }
}
