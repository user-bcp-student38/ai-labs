package com.bcp.workshop.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AuthResourceTest {
    @Inject
    TokenService tokenService;

    @Test
    void logsInWithWorkshopUser() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("user.displayName", is("Workshop Student"))
                .body("user.role", is("STUDENT"))
                .body("token", notNullValue());
    }

    // Caracterización: contrato que debe preservarse tras el refactor de
    // persistencia/token (H-AUTH-03..08).
    @Test
    void rejectsMissingUsername() {
        given()
                .contentType("application/json")
                .body("{\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsMissingPassword() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsWrongPassword() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"not-the-password\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401);
    }

    // Regresión H-AUTH-04: antes del fix, este intento de inyección SQL
    // ('--' comenta la condición de password) autenticaba como "student"
    // sin conocer la contraseña. Tras parametrizar la consulta y verificar
    // el hash en Java, debe rechazarse con 401.
    @Test
    void rejectsSqlInjectionInUsername() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student' -- \",\"password\":\"anything\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401);
    }

    // HU-01: `client` viene en el request pero antes no se validaba ni se
    // reflejaba en el token (H-AUTH-09, candidato). Decisión confirmada:
    // ausente/en blanco se rechaza igual que username/password.
    @Test
    void rejectsMissingClient() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400);
    }

    // HU-01: el token válido debe contener sub, client, iat y exp, y
    // expiresIn debe ser coherente con exp - iat (H-AUTH-09/H-AUTH-10,
    // candidatos). Decodifica con el mismo secreto que usa TokenService.
    @Test
    void issuesTokenWithRequiredClaims() {
        Response response = given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login");

        response.then().statusCode(200);

        String token = response.jsonPath().getString("token");
        long expiresIn = response.jsonPath().getLong("expiresIn");
        Claims claims = decodeClaims(token);

        assertEquals("student", claims.getSubject());
        assertEquals("STUDENT", claims.get(TokenService.ROLE_CLAIM, String.class));
        assertEquals("angular-workshop", claims.get(TokenService.CLIENT_CLAIM, String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long actualLifetimeSeconds = Duration.between(
                claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()).getSeconds();
        assertEquals(actualLifetimeSeconds, expiresIn,
                "expiresIn debe coincidir con exp - iat del token (tolerancia 0s: ambos derivan de la misma constante)");
    }

    private Claims decodeClaims(String token) {
        return Jwts.parser().verifyWith(tokenService.signingKey()).build().parseSignedClaims(token).getPayload();
    }
}
