package com.bcp.workshop.auth;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnexpectedLoginErrorTest {
    @Test
    void returnsGenericErrorWithoutExposingUnexpectedFailure() {
        AuthResource resource = new AuthResource();
        resource.authService = new AuthService() {
            @Override
            public LoginResponseDto login(LoginRequest request) {
                throw new IllegalStateException("internal failure details");
            }
        };

        Response response = resource.login(new LoginRequest());

        assertEquals(500, response.getStatus());
        assertEquals(
                Map.of("message", "No se pudo procesar la solicitud"),
                response.getEntity());
    }
}
