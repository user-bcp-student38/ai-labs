## Contexto técnico

- Componente afectado: `web/` (Angular 21, Vitest 3.2.4 con entorno `node`, `include: src/**/*.spec.ts`).
- Línea base medida el 2026-10-28 desde `web/` con `npm test`: 3 archivos, 16 pruebas, todas en verde (`legacy-format` 1, `dashboard.component` 7, `login.component` 8).
- Contratos fuente: [docs/ENDPOINTS.md](../../../docs/ENDPOINTS.md) y los requisitos de [specs/web-contract-preservation/spec.md](specs/web-contract-preservation/spec.md).
- Código de referencia (no se modifica):
  - Login: [web/src/app/login/login.component.ts](../../../web/src/app/login/login.component.ts). Valida con `trim()`, envía el valor sin recortar, guarda `workshop_token`, `workshop_user` y `workshop_preferences`.
  - Dashboard: [web/src/app/dashboard/dashboard.component.ts](../../../web/src/app/dashboard/dashboard.component.ts). Concatena `owner` sin codificar, descarta `ARCHIVED` y ordena con un comparador de cadenas.
- Pruebas existentes que ya fijan el contrato: [login.component.spec.ts](../../../web/src/app/login/login.component.spec.ts) y [dashboard.component.spec.ts](../../../web/src/app/dashboard/dashboard.component.spec.ts).

## Goals

- Mantener los requisitos de la spec sin cambios de comportamiento en `web/`.
- Registrar la comprobación de escritura del Issue #22 con el servidor GitHub oficial y su resultado observado.
- Dejar trazables la línea base de pruebas y las decisiones sobre el alcance.

## Non-goals

- No modificar `web/src/`, `auth/` ni `api/`.
- No crear pruebas nuevas en esta etapa. Las existentes ya cubren los escenarios de la spec.
- No resolver las preguntas abiertas de [docs/ENDPOINTS.md](../../../docs/ENDPOINTS.md) (status de éxito, esquema de `user`, formato de errores, obligatoriedad de `owner`).
- No corregir el orden de `priority` ni la codificación de `owner`. Son comportamientos actuales que se preservan.
- No tocar H-WEB-01 (token en `localStorage`), que depende de coordinación con `auth/` y `api/`.

## Contratos protegidos

| Contrato | Evidencia existente |
|---|---|
| `POST /auth/login` con `{username, password, client: "angular-workshop"}`, timeout 5000 ms | `login.component.spec.ts`, prueba "preserves the login request..." |
| Éxito con `token` truthy: guarda `workshop_token`, `workshop_user`, `workshop_preferences`; navega a `/dashboard` | misma prueba |
| Mensajes de validación `Ingrese un usuario.` / `Ingrese una contraseña.` sin petición | pruebas de validación |
| `El backend respondió sin token.` | prueba "shows a missing-token message..." |
| Error de login: `No se pudo autenticar a <usuario>.` con detalle del backend o de Axios | pruebas de error y timeout |
| `GET /api/tasks?owner=<usuario>` con `Authorization: Bearer` | prueba "loads, filters, labels, and sorts..." |
| Sin token: navega a `/login`, sin llamada | prueba "redirects to login..." |
| Excluir `ARCHIVED`; orden por `priority` | misma prueba de carga |

## Componentes afectados

- Ninguno en código. El cambio afecta solo la carpeta de OpenSpec `openspec/changes/issue-22-web-write-permission-test/` y, al ejecutarse, la comprobación de escritura contra `user-bcp-student38/ai-labs#22`.

## Decisiones técnicas y alternativas

1. **Sin cambios de código (elegida).** Los requisitos ya se cumplen y están cubiertos por las pruebas existentes. Alternativa descartada: añadir pruebas nuevas ahora. Aumentaría el alcance sin cambiar comportamiento; se reservan para un cambio que sí modifique `web/`.
2. **Comprobación de escritura con una sola escritura identificada.** Alternativa: solo lectura de permisos. Las herramientas disponibles no exponen una consulta de permisos de escritura directa, así que la escritura de prueba es la única forma de verificar el permiso. Se publicará un comentario identificable en #22.
3. **Preservar el orden actual por `priority`.** El comparador actual (`a.priority > b.priority ? -1 : 1`) ordena por cadena en orden descendente; con `HIGH`, `LOW` y `URGENT` no refleja una escala de severidad. Se documenta como ambigüedad de la spec, no se corrige aquí.
4. **Preservar `owner` sin codificar.** Codificarlo con `encodeURIComponent` cambiaría la URL para usuarios con caracteres especiales. Es un cambio de contrato y queda fuera de alcance.

## Estrategia incremental

1. Confirmar la línea base: `npm test` desde `web/` (ya ejecutada: 16/16).
2. Mantener el cambio en documentos de OpenSpec. No hay diff en `web/`.
3. Ejecutar la comprobación de escritura contra #22 y registrar la respuesta.
4. Antes de cualquier cambio futuro en `web/`, repetir `npm test` desde `web/` y añadir las pruebas que falten (ver riesgos).

## Pruebas de caracterización

- Requeridas antes de modificar comportamiento: las 15 pruebas de login y dashboard existentes. Este cambio no modifica comportamiento, así que no se añaden.
- Huecos detectados que una futura modificación de `web/` debería cubrir antes de tocar el código:
  - Login: error sin `response.data.message` ni `message` (fallback `Error desconocido`).
  - Login: usuario solo con espacios (validación con `trim()`).
  - Dashboard: `owner` vacío cuando falta `workshop_user`.
  - Dashboard: orden con `priority` iguales (el comparador devuelve `1`, por lo que el resultado depende de la implementación).
- Verificación de la spec: el requisito "Change does not modify web runtime behavior" se verifica revisando que `git diff` no muestre cambios en `web/src/`, `auth/` ni `api/`.

## Riesgos y trade-offs

- **Escritura en GitHub:** la comprobación crea un comentario visible en #22. Riesgo bajo, pero público para el repositorio.
- **Spec ambigua en orden:** "ordenar por priority" no define dirección ni escala. Una implementación futura podría cambiar el orden sin violar la spec tal como está escrita. Recomiendo precisarla antes de cualquier cambio.
- **Pruebas sin DOM:** Vitest usa `environment: 'node'`. El requisito de "sin innerHTML" se verifica leyendo el template como texto, no renderizando.
- **Preguntas abiertas de backend:** el contrato del lado servidor sigue sin confirmar; las pruebas solo fijan el consumidor.

## Rollback

- Sin cambios de código, no hay nada que revertir en `web/`.
- Para revertir la propuesta: eliminar `openspec/changes/issue-22-web-write-permission-test/` mediante `git` desde la raíz (sin tocar otras ramas).
- Para el comentario de prueba en #22: las herramientas disponibles incluyen `update_issue_comment` pero no eliminación de comentarios. El rollback es editar el comentario o eliminarlo desde la interfaz de GitHub.
