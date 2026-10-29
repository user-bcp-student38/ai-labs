## Referencia

- Issue: [user-bcp-student38/ai-labs#22](https://github.com/user-bcp-student38/ai-labs/issues/22), "MCP local - write permission test", abierto, creado el 2026-10-27 por `user-bcp-student38`.
- El Issue no tiene cuerpo. El alcance se toma de su título y de la selección confirmada por la persona responsable: componente `web/` y alcance "Solo prueba de permisos".

## Por qué importa

El Issue #22 verifica que el servidor GitHub oficial puede escribir en el repositorio `ai-labs` desde el entorno local. Para que esa verificación sea útil sin riesgo, la propuesta fija por escrito el comportamiento actual del componente `web/` que debe preservarse, de modo que cualquier cambio posterior tenga una línea base comparable.

## Qué cambia

- Se registra el resultado de una comprobación de escritura contra el Issue #22 usando el servidor GitHub oficial.
- Se fijan como requisitos verificables los contratos que el frontend `web/` envía y consume, tomados de [docs/ENDPOINTS.md](docs/ENDPOINTS.md).

## Fuera de alcance

- Cualquier cambio en código funcional de `web/`, `auth/` o `api/`.
- Pruebas nuevas en `web/` o cambios en `web/src/`.
- Diseño técnico de implementación y `tasks.md`; no se crean en esta propuesta.
- Cambios de contrato con los backends `auth/` y `api/`.
- Cambios en otras ramas, remotos, tags o PR.

## Comportamiento que debe preservarse

Línea base: [docs/ENDPOINTS.md](docs/ENDPOINTS.md), sección `web/`.

- `POST /auth/login` con `username`, `password` y `client` = `angular-workshop`, timeout de 5000 ms.
- Éxito: cualquier 2xx con `token` truthy; se guardan `workshop_token` y `workshop_user` y se navega a `/dashboard`.
- Validación local: mensajes `Ingrese un usuario.` e `Ingrese una contraseña.` sin enviar la petición.
- `GET /api/tasks?owner=<usuario>` con cabecera `Authorization: Bearer <token>`.
- El dashboard descarta tareas con `status` = `ARCHIVED` y ordena por `priority`.

El Issue no cambia ninguno de estos contratos. Las preguntas abiertas de [docs/ENDPOINTS.md](docs/ENDPOINTS.md) (status de éxito del backend, esquema de `user`, formato de errores y si `owner` es obligatorio) siguen abiertas y no se resuelven aquí.

## Riesgos

- El Issue no define un cambio funcional; una escritura de prueba no debe modificar el código del componente.
- Si la comprobación de escritura falla por permisos, el resultado debe reportarse sin ocultar el error.
