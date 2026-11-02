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
 */
@ApplicationScoped
public class TokenService {
    private static final String TOKEN_ISSUER = "workshop-auth";
    private static final String ROLE_CLAIM = "role";
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    @ConfigProperty(name = "auth.jwt.secret")
    String signingSecret;

    public String create(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .subject(username)
                .claim(ROLE_CLAIM, role)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(TOKEN_LIFETIME)))
                .signWith(key)
                .compact();
    }

    public long expirationSeconds() {
        return TOKEN_LIFETIME.toSeconds();
    }
}
