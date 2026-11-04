# Token firmado con expiración y claims mínimos en POST /auth/login

## Fuente

Issue aprobado: [HU-01.md](./HU-01.md) — "Token firmado con expiracion y claims minimos"
(veredicto: "Usable con ajustes"). Evidencia de estado actual citada abajo.

## Contexto actual

`POST /auth/login` (`auth/src/main/java/com/bcp/workshop/auth/AuthResource.java`)
recibe `{username, password, client}` ([LoginRequest.java](./auth/src/main/java/com/bcp/workshop/auth/LoginRequest.java))
y responde `200` con `{token, expiresIn, user: {username, displayName, role}}`
([LoginResponseDto.java](./auth/src/main/java/com/bcp/workshop/auth/LoginResponseDto.java),
[UserSummaryDto.java](./auth/src/main/java/com/bcp/workshop/auth/UserSummaryDto.java)).

El token se emite en [TokenService.create](./auth/src/main/java/com/bcp/workshop/auth/TokenService.java)
(líneas 28-39) como JWT HS256 firmado con `io.jsonwebtoken:jjwt`, con claims
`iss` ("workshop-auth"), `sub` (username), `role`, `iat` y `exp` (vigencia fija
de 1 hora, `TOKEN_LIFETIME`). `expiresIn` se calcula aparte, en
`TokenService.expirationSeconds()` (líneas 41-43), como la misma constante en
segundos.

El campo `client` existe en el contrato de entrada
([LoginRequest.java](./auth/src/main/java/com/bcp/workshop/auth/LoginRequest.java):6)
pero no se lee en [AuthService.login](./auth/src/main/java/com/bcp/workshop/auth/AuthService.java)
ni se pasa a `TokenService.create`, por lo que no aparece como claim del token.

El secreto de firma se lee de `AUTH_JWT_SECRET`, con un valor por defecto
documentado como exclusivo de laboratorio
([application.properties](./auth/src/main/resources/application.properties):13).
`AuthResource` no registra el token ni el secreto en los logs; solo registra
el detalle de errores inesperados y de consulta a usuario
([AuthResource.java](./auth/src/main/java/com/bcp/workshop/auth/AuthResource.java),
[UserRepository.java](./auth/src/main/java/com/bcp/workshop/auth/UserRepository.java)).

El único test existente del login
([AuthResourceTest.java](./auth/src/test/java/com/bcp/workshop/auth/AuthResourceTest.java))
cubre: login válido, username/password faltantes, password incorrecta e
inyección SQL en username. Ninguno decodifica el JWT ni verifica sus claims,
su `iat`/`exp`, ni la coherencia de `expiresIn`.

## Problema confirmado

El token emitido no incluye el claim `client`, pese a que el cliente lo
envía en la solicitud. No existe verificación automatizada (prueba) de que
el token contenga `sub`, `client`, `iat` y `exp`, ni de que `expiresIn` sea
coherente con `exp - iat`. Tampoco hay una prueba de caracterización para el
caso de token expirado dentro del componente `auth`.

## Comportamiento a preservar

- Contrato HTTP de `POST /auth/login`: request `{username, password, client}`,
  respuesta `200` con `{token, expiresIn, user: {username, displayName, role}}`,
  o `400`/`401` con `{message}` en los casos ya cubiertos por
  `AuthResourceTest` (username/password faltante, credenciales inválidas,
  intento de inyección SQL en username).
- Emisión de JWT HS256 firmado, con `iss`, `sub`, `role`, `iat`, `exp` ya
  presentes.
- Lectura del secreto de firma vía `AUTH_JWT_SECRET` (variable de entorno),
  sin secretos reales embebidos en el código.
- Ausencia de logs de token o secreto (comportamiento actual, sin evidencia
  de regresión que preservar explícitamente).

## Alcance

- Componente `auth` únicamente: `LoginRequest`, `AuthService`, `TokenService`,
  `AuthResourceTest` (y equivalentes de prueba).
- Definir y validar el claim `client` en el token emitido por
  `POST /auth/login`.
- Definir y documentar la tolerancia de coherencia entre `expiresIn` y
  `exp - iat`.
- Añadir pruebas de caracterización/verificación de claims (`sub`, `client`,
  `iat`, `exp`), de expiración y de los escenarios de login ya cubiertos.
- Documentar la decisión técnica de mantener `io.jsonwebtoken:jjwt` (HMAC)
  frente a otra librería JWT.
- Confirmar que no se registran tokens ni secretos en los logs del
  componente `auth`.

## Fuera de alcance

- Cualquier cambio en `api/app/main.py::legacy_identity` u otro código de
  `api`, incluida la posible exigencia del claim `client` del lado
  verificador. Si el nuevo claim debe ser exigido también ahí, requiere
  ampliar el alcance acordado antes de tocar ese archivo.
- Cambios en `web/` (consumo del token en el frontend).
- Mecanismos de fail-fast en el arranque si falta `AUTH_JWT_SECRET`
  (comportamiento de configuración, no de claims del token).
- Cambios de CORS o de persistencia de usuarios.

## Criterios de aceptación

- Un token emitido por `POST /auth/login` con credenciales válidas contiene
  los claims `sub`, `client`, `iat` y `exp`.
- El valor del claim `client` refleja el `client` recibido en la solicitud
  (vacío/ausente se valida explícitamente; el comportamiento ante
  `client` vacío o ausente queda definido como criterio adicional, a
  decidir en diseño).
- `expiresIn` en la respuesta es coherente con `exp - iat` del token, dentro
  de una tolerancia documentada (valor y justificación registrados en el
  diseño).
- Existen pruebas automatizadas para: login válido (incluye verificación de
  claims), credenciales inválidas (401), solicitud inválida (400), y un
  escenario de token expirado.
- Ninguna prueba ni código de producción registra el valor completo de un
  token ni del secreto de firma en logs.
- La decisión de conservar `io.jsonwebtoken:jjwt` (HMAC) o migrar a otra
  librería queda documentada con su justificación.
- Las pruebas de caracterización existentes (login válido, username/password
  faltante, credenciales inválidas, inyección SQL) siguen en verde.

## Contratos afectados/protegidos

- `POST /auth/login` — contrato protegido (ver
  [reqflow-definition-of-done](./.agents/skills/reqflow-definition-of-done/SKILL.md)):
  no cambia su forma HTTP (campos de request/response, códigos de estado);
  solo se amplía el contenido interno del JWT (`token`) y se documenta
  `expiresIn`.
- Contratos protegidos no tocados por este alcance: `GET/POST /api/tasks`,
  `GET /api/summary`, `POST /api/requirements`.
- Dependencia implícita con `api/app/main.py::legacy_identity`, que hoy
  verifica `exp`/`sub` del mismo JWT: un claim adicional no debería romper su
  verificación actual (no exige campos extra), pero cualquier decisión de
  *requerir* `client` ahí es un cambio de contrato fuera de este alcance.

## Riesgos, supuestos y preguntas abiertas

- Pregunta abierta: ¿qué debe pasar si `client` llega vacío o ausente en la
  solicitud? (¿400 como con `username`/`password`, o se permite y el claim
  queda vacío/omitido?) HU-01 pide "validarlo" sin especificar la regla.
- Pregunta abierta: ¿cuál es la tolerancia aceptada entre `expiresIn` y
  `exp - iat` (por ejemplo, segundos de margen por latencia de cómputo)?
- Pregunta abierta: ¿el escenario de "token expirado" se prueba a nivel de
  `TokenService`/firma (unitario, dentro de `auth`), ya que `auth` no valida
  tokens entrantes en ningún endpoint propio? El componente `api` ya tiene
  una prueba equivalente (`test_rejects_expired_token`), pero está fuera de
  este alcance.
- Supuesto: se mantiene `io.jsonwebtoken:jjwt` como librería JWT (ya
  adoptado), salvo que la revisión humana decida lo contrario; esta spec no
  prescribe cuál persiste, solo exige que la decisión quede documentada.
- Riesgo: si `client` se vuelve obligatorio y `api` no lo valida, el
  endurecimiento del contrato en `auth` no se refleja en la verificación de
  `api`, dando una falsa sensación de control de origen por cliente.

## Estado

Aprobado por Enrique
