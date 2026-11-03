# Plan — auth (Lab 01)

Alcance acordado: componente `auth`, con extensión puntual a `api/app/main.py`
únicamente en la función `legacy_identity` (verificación de token), porque
`auth` y `api` comparten hoy el mismo secreto y formato de token. No se toca
ningún otro archivo de `api/` ni de `web/`.

Contrato a preservar: `POST /auth/login` recibe `{username, password, client}`
y responde `200` con `{token, expiresIn, user: {username, displayName, role}}`,
o `400/401` con `{message}` en los casos ya cubiertos por
`AuthResourceTest.logsInWithWorkshopUser` (usuario `student` /
`Workshop123!`). El cliente web (`login.component.ts`) solo lee
`response.data.token` y `response.data.user`; el campo `debug` no se consume
y se retira por ser un detalle interno expuesto sin necesidad (ligado a
H-AUTH-08).

## Decisión de diseño: token

Se reemplaza el Base64 casero por un JWT real firmado con **HS256** usando
una librería madura (`io.jsonwebtoken:jjwt` en `auth`, `PyJWT` en `api`), en
vez de una implementación de firma manual. El secreto deja de estar en el
código: se lee de la variable de entorno `AUTH_JWT_SECRET` (mismo valor en
ambos servicios, solo para este laboratorio). Claims: `sub` (username),
`role`, `iss` (`workshop-auth`), `exp`. `api.legacy_identity` pasa a
verificar firma, emisor y expiración con `pyjwt` en vez de decodificar
Base64 a mano.

## Hallazgos y diseño por cada uno

| ID | Diseño | Archivos afectados |
|---|---|---|
| H-AUTH-01 | Subir `commons-text` de `1.9` a `1.15.0` (corrige CVE-2022-42889). | `auth/pom.xml` |
| H-AUTH-02 | Subir `quarkus.platform.version` a `3.40.1.1`, `sqlite-jdbc` a `3.54.1.0`, `jacoco-maven-plugin` a `0.8.16`, `maven-surefire-plugin` a `3.6.1` (versiones ya validadas por Dependabot en `origin`). | `auth/pom.xml` |
| H-AUTH-03 | Reemplazar texto plano por hash BCrypt (`io.quarkus.elytron.security.common.BcryptUtil`, dependencia ya administrada por el BOM de Quarkus vía `quarkus-elytron-security-common`). Las contraseñas semilla (`student`/`trainer`) se siembran ya hasheadas. | `auth/pom.xml`, `UserRepository` (antes `LegacyDatabase`) |
| H-AUTH-04 | La consulta busca únicamente por `username` con `PreparedStatement`; la verificación de contraseña se hace en Java con `BcryptUtil.matches`, nunca en el SQL. | `UserRepository` |
| H-AUTH-05 / H-AUTH-06 | JWT HS256 firmado (ver decisión de diseño arriba), secreto y CORS externalizados a variables de entorno (`AUTH_JWT_SECRET`, `AUTH_CORS_ORIGINS`, por defecto `http://localhost:4200` en vez de `/.*/`). | `TokenService` (antes `LegacyTokenUtil`), `application.properties`, `api/app/main.py` (`legacy_identity`), `compose.yaml` |
| H-AUTH-07 | Separar en capas: `AuthResource` (HTTP, guard clauses, sin anidamiento) → `AuthService` (orquesta validación + autenticación + token) → `UserRepository` (persistencia) → `TokenService` (firma). `LoginResponseDto` reemplaza el `Map` armado a mano. | `AuthResource`, nuevo `AuthService`, `UserRepository`, `TokenService`, `LoginResponseDto` |
| H-AUTH-08 | Catch específico en `AuthResource`/`AuthService`; mensaje genérico al cliente, log del detalle con `org.jboss.logging.Logger` en servidor. Se retira el campo `debug` de la respuesta. | `AuthResource`, `AuthService` |

Constantes nuevas (elimina números/strings mágicos, Clean code #12):
`TOKEN_EXPIRATION_SECONDS = 3600`, `TOKEN_ISSUER = "workshop-auth"`,
variables de entorno documentadas arriba en vez de literales.

## Pruebas de caracterización (antes de refactorizar)

1. `AuthResourceTest.logsInWithWorkshopUser` (ya existe) — se ejecuta primero
   tal cual para confirmar la línea base en verde.
2. Nuevas antes de tocar persistencia/token:
   - Login con credenciales inválidas → `401` y mensaje genérico (sin el
     username reflejado, fija H-AUTH-08 como contrato esperado).
   - Login con body nulo/username vacío/password vacío → `400` (ya cubre
     parte de H-AUTH-07, fija el comportamiento actual antes de refactor).
   - Verificación directa de `UserRepository.findByUsername` con el dato
     semilla, antes de migrar a hash.

## Pruebas posteriores (tras implementar)

- Repetir las pruebas de caracterización: deben seguir en verde con el mismo
  contrato HTTP.
- Nueva prueba: el token emitido es un JWT válido (tres segmentos, `exp`
  futuro, `sub=student`), no Base64 plano de `user|role|exp|secret`.
- Nueva prueba: `UserRepository` rechaza intentos de inyección SQL en
  `username` (ej. `' OR '1'='1`) sin lanzar excepción ni autenticar.
- Lado `api`: `legacy_identity` acepta un JWT válido emitido por `auth` y
  rechaza uno expirado o con firma alterada (prueba de caracterización nueva
  en `api/tests/test_api.py`, ya que se amplía su alcance).
- `mvn test` (JaCoCo) y `pytest --cov` (del subconjunto tocado en `api`) en
  verde, cobertura del código modificado ≥ 85%.

## Pendiente de revisión humana

Este plan no se implementa hasta que una persona lo revise y registre su
decisión aquí.

- [x] Aprobado para implementar — revisor: Usuario (sesión AI Labs) — fecha: 2026-11-02
