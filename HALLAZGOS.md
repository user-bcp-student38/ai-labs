# Hallazgos de Discovery

## Contrato observado de web

`docs/ENDPOINTS.md` no existe todavía. Lo siguiente describe únicamente lo que
se observa en el frontend; no es un contrato acordado ni una confirmación del
comportamiento de los backends:

- **Login:** envía `POST http://localhost:8080/auth/login` con `username`,
  `password` y `client`; espera `data.token` y `data.user`, guarda esos valores
  y las preferencias en `localStorage`, y navega a `/dashboard`.
  Evidencia: `web/src/app/login/login.component.ts:41-49`.
- **Carga de tareas:** si no encuentra token, navega a `/login`; si lo encuentra,
  envía `GET http://localhost:8000/api/tasks?owner=<username>` con
  `Authorization: Bearer <token>`. Después excluye tareas `ARCHIVED` y prepara
  las etiquetas y el orden de visualización.
  Evidencia: `web/src/app/dashboard/dashboard.component.ts:35-55`.
- **Cierre de sesión:** elimina el token y el usuario de `localStorage` y
  navega a `/login`; no se observa una petición de logout ni se eliminan las
  preferencias.
  Evidencia: `web/src/app/dashboard/dashboard.component.ts:74-77`.
- **Pendiente de acordar:** confirmar los contratos con los responsables de
  auth y api. Discovery no inspeccionó esos componentes.

## Matriz de seguimiento

| id | componente | hallazgo | tipo | explicacion | evidencia (archivo:linea) | severidad | estado |
|---|---|---|---|---|---|---|---|
| H-WEB-01 | web | Token de sesión almacenado en `localStorage` | Vulnerabilidad | Un script que se ejecute en el mismo origen podría leer el token persistente y reutilizarlo. El riesgo depende de la exposición a scripts del origen; no se probó un robo de token. | `web/src/app/login/login.component.ts:46-49`; `web/src/app/dashboard/dashboard.component.ts:36,75-76` | media | Pendiente |
| H-WEB-02 | web | Dependencias directas de axios y lodash en las versiones señaladas por el reto; estado de advisories sin verificar | Sugerencia | Las versiones `axios 0.31.0` y `lodash 4.17.21` están declaradas y lodash se usa en el login. La presencia de estas versiones no confirma por sí sola un CVE aplicable; falta ejecutar y revisar el análisis de dependencias. | `web/package.json:19-20`; `web/src/app/login/login.component.ts:5-6,25` | media | Pendiente |
| H-WEB-03 | web | Mensajes de error dinámicos renderizados mediante `innerHTML` | Refactor | El mensaje incluía el nombre ingresado y un mensaje recibido del backend. Angular sanitiza el binding `innerHTML` de forma predeterminada; no se demostró una XSS, pero el contenido podía mostrarse como marcado. | `web/src/app/login/login.component.ts:61-64`; `web/src/app/login/login.component.html:39-42` | baja | Resuelto |
| H-WEB-04 | web | Los componentes concentran HTTP, URLs, sesión y navegación | Refactor | Login y dashboard realizan llamadas HTTP, manipulan `localStorage` y navegan directamente. Esto acopla presentación y servicios, dificulta pruebas aisladas y obliga a cambiar componentes para ajustar configuración o sesión. | `web/src/app/login/login.component.ts:22-25,41-49`; `web/src/app/dashboard/dashboard.component.ts:35-44,74-77` | baja | Pendiente |
| H-WEB-05 | web | Uso de `any` y validaciones con condicionales anidados | Refactor | `any` elimina comprobaciones estáticas sobre requests, responses y estado de tareas; la validación anidada en login reduce legibilidad y hace más difícil cubrir cada rama. No se afirma duplicación sin evidencia específica. | `web/src/app/login/login.component.ts:16-20,33-37,41,54`; `web/src/app/dashboard/dashboard.component.ts:23-27,43,47,54,65`; `web/src/app/utils/legacy-format.ts:1` | baja | Pendiente |
| H-WEB-06 | web | Cobertura incompleta de flujos de autenticación y tareas | Sugerencia | Login y carga/error de tareas tienen pruebas unitarias; logout, integración real y arranque de la app siguen sin cobertura, así que la cobertura de flujos continúa parcial. | `web/src/app/login/login.component.spec.ts:46,55,65,97,114,133`; `web/src/app/dashboard/dashboard.component.spec.ts:55,82,92,109,122,135,147`; resultados en `docs/TESTING.md` | baja | Pendiente |

**Resolución H-WEB-03:** los mensajes ahora se representan con campos de texto
interpolados en el template; se eliminó el binding `[innerHTML]`.

### Límites de esta revisión

- Se ejecutaron las pruebas web de H-WEB-03; los comandos y resultados están
  registrados en `PLAN.md`. No se ejecutó `npm audit`, por lo que los advisories
  de dependencias siguen pendientes.
- La lista y los contratos de `auth` y `api` no se verificaron dentro de este
  alcance.
