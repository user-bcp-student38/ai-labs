## 1. Línea base y caracterización

- [x] 1.1 Ejecutar `npm test` desde `web/` y confirmar 3 archivos y 16 pruebas en verde.
- [x] 1.2 Verificar que cada contrato de `design.md` tiene una prueba existente que lo fija: login (petición, éxito, validación, sin token, errores, timeout) y dashboard (petición con `owner` y `Bearer`, sin token, errores, `ARCHIVED`, orden).
- [x] 1.3 Confirmar que no se añaden pruebas nuevas: los huecos de `design.md` quedan fuera de este cambio.

## 2. Cambio funcional

- [x] 2.1 Ejecutar la comprobación de escritura contra [user-bcp-student38/ai-labs#22](https://github.com/user-bcp-student38/ai-labs/issues/22) con el servidor GitHub oficial. Requiere confirmación explícita antes de publicar el comentario.
- [x] 2.2 Registrar en la sección de resultado de este archivo si la escritura fue permitida o rechazada, con el mensaje de error textual si falla.

## 3. Verificación

- [x] 3.1 Confirmar con `git status` desde la raíz que no hay cambios en `web/src/`, `auth/` ni `api/`.
- [x] 3.2 Volver a ejecutar `npm test` desde `web/` y confirmar que sigue en 16/16.
- [x] 3.3 Revisar el alcance del árbol: además de la carpeta `openspec/changes/issue-22-web-write-permission-test/` y el comentario en #22, el PR incluye por decisión de la persona responsable `.vscode/mcp.json`, `HALLAZGOS.md`, `.github/` (agentes, prompts, skills y workflow) y `openspec/` (cambios `issues-24-34-web-hallazgos` y `archive`). No hay cambios en `web/src/`, `auth/` ni `api/`.

## 4. Resultado de la comprobación de escritura

- Escritura permitida. Comentario publicado en https://github.com/user-bcp-student38/ai-labs/issues/22#issuecomment-6500691187 (id 6500691187) con el servidor GitHub oficial. Sin mensaje de error.

## Fuera de alcance

- Cambios en `web/src/`, `auth/` o `api/`.
- Resolver las preguntas abiertas de `docs/ENDPOINTS.md`.
- Corregir el orden por `priority` o la codificación de `owner`.
- Eliminar el comentario de prueba (la herramienta disponible no lo permite; rollback manual desde la interfaz de GitHub).
