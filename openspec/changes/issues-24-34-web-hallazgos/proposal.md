# Propuesta: hallazgos web de los Issues #24 a #34

## Referencia

- Issues: [user-bcp-student38/ai-labs#24](https://github.com/user-bcp-student38/ai-labs/issues/24) a [#34](https://github.com/user-bcp-student38/ai-labs/issues/34), uno por hallazgo H-WEB-01, H-WEB-02, H-WEB-04 a H-WEB-12.
- Componente elegido: `web/`. Todos los issues afectan solo este componente.
- Issue [#23](https://github.com/user-bcp-student38/ai-labs/pull/23) ("Day2/skill mcp"): está cerrado, su cuerpo es una plantilla sin hallazgo ni alcance web. Queda fuera de esta propuesta.
- Baseline de comportamiento: [docs/ENDPOINTS.md](docs/ENDPOINTS.md), sección `web/`.
- Origen de hallazgos: [HALLAZGOS.md](HALLAZGOS.md) y [PLAN.md](PLAN.md).

## Por qué importa

Los hallazgos de `web/` cubren exposición de sesión y de datos en consola, cobertura de pruebas insuficiente, dependencias sin verificar, errores de manejo de estado y acoplamiento de componentes. Cada issue define un criterio verificable. Esta propuesta fija qué debe seguir igual y qué cambia de forma explícita, para que la implementación posterior tenga una línea base comparable.

## Alcance

- Corregir en `web/` los hallazgos con criterio ya definido: H-WEB-02 (#25), H-WEB-04 (#26), H-WEB-05 (#27), H-WEB-06 (#28), H-WEB-07 (#29), H-WEB-08 (#30), H-WEB-09 (#31) y H-WEB-10 (#32).
- Ejecutar y registrar `npm audit` en `web/` (#25).
- Cubrir con pruebas en `web/` los flujos de logout, arranque y los escenarios nuevos de los issues.
- Registrar las decisiones pendientes de H-WEB-11 (#33) y H-WEB-12 (#34) antes de cambiar su comportamiento.
- Limitar cada cambio a `web/`; las pruebas se ejecutan desde `web/`.

## Fuera de alcance

- Cualquier cambio en `auth/` o `api/`, incluidos sus contratos.
- Cambiar el almacenamiento del token a cookie `HttpOnly` (#24). El issue exige una decisión acordada con auth y api; sin ella el riesgo permanece abierto y no se declara mitigado.
- Definir el comportamiento final de token inválido (#33) y de empates de orden (#34) sin decisión humana registrada.
- Diseño técnico de la implementación y `tasks.md`. Esta propuesta no los crea.
- Actualizaciones de dependencias sin revisión de advisories.
- Cambios de endpoints, de su formato o de los campos que `web/` consume.

## Comportamiento que debe preservarse

Tomado de [docs/ENDPOINTS.md](docs/ENDPOINTS.md) y del flujo de logout de [HALLAZGOS.md](HALLAZGOS.md):

- `POST /auth/login` con `username`, `password` y `client` = `angular-workshop`, timeout de 5000 ms y sin cabeceras ni query explícitas.
- Éxito: respuesta 2xx con `token` truthy; se guardan `workshop_token` y `workshop_user` y se navega a `/dashboard`.
- Validación local: `Ingrese un usuario.` e `Ingrese una contraseña.`, sin enviar la petición.
- Error de autenticación: título `No se pudo autenticar a <usuario>.` y detalle según el orden documentado.
- `GET /api/tasks?owner=<usuario>` con `Authorization: Bearer <workshop_token>`.
- Sin token, el dashboard navega a `/login` y no llama a `/api/tasks`.
- El dashboard excluye tareas con `status` = `ARCHIVED` y ordena por `priority`.
- Logout: elimina `workshop_token` y `workshop_user` y navega a `/login`.

## Cambios de comportamiento explícitos

Estos cambios se apartan del baseline y deben aprobarse con la propuesta:

- **H-WEB-07 (#29):** el login deja de registrar el objeto de error completo. [docs/ENDPOINTS.md](docs/ENDPOINTS.md) documenta que hoy sí lo registra en consola.
- **H-WEB-08 (#30):** un `workshop_user` malformado deja de provocar una excepción no controlada en la inicialización y se trata como sesión ausente. El destino propuesto es `/login`.
- **H-WEB-09 (#31):** el parámetro `owner` se codifica. Para usernames sin caracteres reservados la URL resultante es idéntica; solo cambia para caracteres reservados.
- **H-WEB-10 (#32):** un fallo de navegación tras login exitoso ya no se muestra como error de autenticación. El texto que lo reemplaza está pendiente de definir.

## Decisiones pendientes

1. **H-WEB-01 (#24):** dónde y cómo se almacena el token, acordado con auth y api.
2. **H-WEB-11 (#33):** qué hacer ante token inválido o expirado frente a un error no relacionado con autenticación.
3. **H-WEB-12 (#34):** criterio de orden para tareas con igual prioridad.
4. **H-WEB-10 (#32):** texto del mensaje de fallo de navegación y si la sesión guardada se conserva.
5. **H-WEB-08 (#30):** confirmar que el destino tras sesión malformada es `/login`.
6. **H-WEB-02 (#25):** resultado real de `npm audit`, no ejecutado en esta propuesta.
7. Preguntas abiertas de [docs/ENDPOINTS.md](docs/ENDPOINTS.md) con auth y api (status de éxito, esquema de `user`, formato de errores).

## Riesgos

- Un cambio de almacenamiento o de sesión sin contrato con auth y api puede romper el login o la carga de tareas.
- Actualizar dependencias para cerrar advisories puede alterar el build; debe validarse con `python tools/lab.py test web`.
- Las decisiones #33 y #34 pueden cambiar el comportamiento visible; si no se registran, el cambio no debe modificarlas.

## Revisión humana

Esta propuesta y sus specs requieren revisión y aprobación humana antes de pasar a Architect. Las decisiones pendientes son parte de esa revisión.
