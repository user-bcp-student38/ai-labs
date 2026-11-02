package com.bcp.workshop.auth;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {
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
                .body("{\"username\":\"student\",\"password\":\"not-the-password\"}")
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
                .body("{\"username\":\"student' -- \",\"password\":\"anything\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401);
    }
}
