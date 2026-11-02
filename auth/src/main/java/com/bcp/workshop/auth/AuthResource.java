package com.bcp.workshop.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger LOG = Logger.getLogger(AuthResource.class);
    private static final String GENERIC_ERROR_MESSAGE = "No se pudo procesar la solicitud";

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            return Response.ok(authService.login(request)).build();
        } catch (InvalidLoginRequestException error) {
            return Response.status(400).entity(Map.of("message", error.getMessage())).build();
        } catch (InvalidCredentialsException error) {
            return Response.status(401).entity(Map.of("message", error.getMessage())).build();
        } catch (Exception error) {
            LOG.error("Error inesperado en /auth/login", error);
            return Response.status(500).entity(Map.of("message", GENERIC_ERROR_MESSAGE)).build();
        }
    }
}

