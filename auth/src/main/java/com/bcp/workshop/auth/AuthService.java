package com.bcp.workshop.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Reglas de negocio del login: valida el request, verifica credenciales
 * contra el hash persistido y arma la respuesta. No conoce HTTP ni SQL.
 */
@ApplicationScoped
public class AuthService {
    @Inject
    UserRepository userRepository;

    @Inject
    TokenService tokenService;

    public LoginResponseDto login(LoginRequest request) {
        String username = requireNonBlank(request == null ? null : request.username, "Username is required");
        String password = requireNonBlank(request == null ? null : request.password, "Password is required");
        String client = requireNonBlank(request == null ? null : request.client, "Client is required");

        UserRecord user = userRepository.findByUsername(username);
        if (user == null || !BcryptUtil.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = tokenService.create(user.username(), user.role(), client);
        UserSummaryDto userSummary = new UserSummaryDto(
                user.username(),
                StringEscapeUtils.escapeHtml4(user.displayName()),
                user.role());
        return new LoginResponseDto(token, tokenService.expirationSeconds(), userSummary);
    }

    private static String requireNonBlank(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidLoginRequestException(errorMessage);
        }
        return value;
    }
}
