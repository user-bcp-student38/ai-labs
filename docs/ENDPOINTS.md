# Endpoints

Contratos confirmados contra el código real (no inventados). Alcance de hoy:
componente `auth` completo; en `api` solo se documenta el contrato de
autorización porque fue el único punto tocado (coordinado con H-AUTH-05/06).
El resto de endpoints de `api` queda pendiente de su propio Discovery.

## auth

### `POST /auth/login`

Request (`application/json`):

```json
{ "username": "string", "password": "string", "client": "string (opcional, no se valida)" }
```

Respuestas:

| Caso | Status | Body |
|---|---|---|
| Credenciales válidas | `200` | `{ "token": "<JWT HS256>", "expiresIn": 3600, "user": { "username", "displayName", "role" } }` |
| `username` vacío o ausente | `400` | `{ "message": "Username is required" }` |
| `password` vacío o ausente | `400` | `{ "message": "Password is required" }` |
| Usuario inexistente o password incorrecta | `401` | `{ "message": "Invalid username or password" }` |
| Error inesperado del servidor | `500` | `{ "message": "No se pudo procesar la solicitud" }` (sin detalles internos) |

Notas de contrato:
- El campo `debug` que existía en la respuesta se retiró: no lo consumía
  ningún cliente (`web/src/app/login/login.component.ts` solo lee `token` y
  `user`).
- `token` dejó de ser Base64 reversible; ahora es un JWT firmado HS256 con
  claims `iss=workshop-auth`, `sub=<username>`, `role`, `exp`. Cualquier
  consumidor del token (hoy, `api`) debe verificar firma y expiración, no
  decodificarlo a ciegas.
- Usuarios de prueba del seed: `student` / `Workshop123!` (rol `STUDENT`) y
  `trainer` / `Trainer123!` (rol `TRAINER`).

## api (solo el punto tocado hoy)

Todos los endpoints de `api/app/main.py` (`/api/tasks`, `/api/summary`,
`/api/requirements`, `/api/backend-health`) siguen exigiendo
`Authorization: Bearer <token>`. Lo que cambió es cómo se valida ese token
(`legacy_identity`):

- Antes: decodificaba Base64 y comparaba un secreto embebido, sin verificar
  firma real.
- Ahora: verifica el JWT emitido por `auth` (firma HS256 con el secreto
  compartido `AUTH_JWT_SECRET`, emisor `workshop-auth`, expiración). Si falla
  cualquiera de esas validaciones, responde `401 { "detail": "Invalid token" }`.

El resto del comportamiento de estos endpoints (filtros, autorización por
`owner`, manejo de archivos) no se tocó hoy y no se documenta aquí para no
inventar contratos no verificados en esta sesión.
