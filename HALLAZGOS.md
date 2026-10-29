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
| H-WEB-01 | web | Token de sesión almacenado en `localStorage` | Vulnerabilidad | Un script que se ejecute en el mismo origen podría leer el token persistente y reutilizarlo. El riesgo depende de la exposición a scripts del origen; no se probó un robo de token. | `web/src/app/login/login.component.ts:46-49`; `web/src/app/dashboard/dashboard.component.ts:36,75-76` | media | CONFIRMADO |
| H-WEB-02 | web | Dependencias directas de axios y lodash en las versiones señaladas por el reto; estado de advisories sin verificar | Sugerencia | Las versiones `axios 0.31.0` y `lodash 4.17.21` están declaradas y lodash se usa en el login. La presencia de estas versiones no confirma por sí sola un CVE aplicable; falta ejecutar y revisar el análisis de dependencias. | `web/package.json:19-20`; `web/src/app/login/login.component.ts:5-6,25` | media | CONFIRMADO |
| H-WEB-03 | web | Mensajes de error dinámicos renderizados mediante `innerHTML` | Refactor | El mensaje incluía el nombre ingresado y un mensaje recibido del backend. Angular sanitiza el binding `innerHTML` de forma predeterminada; no se demostró una XSS, pero el contenido podía mostrarse como marcado. | `web/src/app/login/login.component.ts:61-64`; `web/src/app/login/login.component.html:39-42` | baja | Resuelto |
| H-WEB-04 | web | Los componentes concentran HTTP, URLs, sesión y navegación | Refactor | Login y dashboard realizan llamadas HTTP, manipulan `localStorage` y navegan directamente. Esto acopla presentación y servicios, dificulta pruebas aisladas y obliga a cambiar componentes para ajustar configuración o sesión. | `web/src/app/login/login.component.ts:22-25,41-49`; `web/src/app/dashboard/dashboard.component.ts:35-44,74-77` | baja | CONFIRMADO |
| H-WEB-05 | web | Uso de `any` y validaciones con condicionales anidados | Refactor | `any` elimina comprobaciones estáticas sobre requests, responses y estado de tareas; la validación anidada en login reduce legibilidad y hace más difícil cubrir cada rama. No se afirma duplicación sin evidencia específica. | `web/src/app/login/login.component.ts:16-20,33-37,41,54`; `web/src/app/dashboard/dashboard.component.ts:23-27,43,47,54,65`; `web/src/app/utils/legacy-format.ts:1` | baja | CONFIRMADO |
| H-WEB-06 | web | Cobertura incompleta de flujos de autenticación y tareas | Sugerencia | Login y carga/error de tareas tienen pruebas unitarias; logout, integración real y arranque de la app siguen sin cobertura, así que la cobertura de flujos continúa parcial. | `web/src/app/login/login.component.spec.ts:46,55,65,97,114,133`; `web/src/app/dashboard/dashboard.component.spec.ts:55,82,92,109,122,135,147`; resultados en `docs/TESTING.md` | baja | CONFIRMADO |
| H-WEB-07 | web | El error completo de Axios se envía a la consola en fallos de login | APROBADO | El objeto de error de una petición puede incluir su configuración y datos de solicitud; debe comprobarse si el registro expone información sensible del login en la consola. No se confirmó la salida del objeto en ejecución. | `web/src/app/login/login.component.ts:45-47,58-65` | media | CONFIRMADO |
| H-WEB-08 | web | La lectura del usuario de sesión puede fallar con JSON local malformado | Candidato | El parseo de `workshop_user` ocurre antes del bloque `try`; un valor local inválido puede interrumpir la inicialización del dashboard antes de procesar el estado de sesión. | `web/src/app/dashboard/dashboard.component.ts:35-38` | baja | CONFIRMADO |
| H-WEB-09 | web | El nombre de usuario se concatena directamente en la URL de tareas | CONFIRMADO | Un nombre con caracteres reservados de URL podría alterar la consulta `owner` o hacer que el servicio reciba un valor distinto; no se verificó el comportamiento del backend. | `web/src/app/dashboard/dashboard.component.ts:43-45` | baja | CONFIRMADO |
| H-WEB-10 | web | Un error de navegación tras login exitoso se maneja como fallo de autenticación | Refactor | El bloque `try` cubre la respuesta exitosa, la persistencia de sesión y la navegación. Si la navegación rechaza la promesa después de persistir la sesión, el `catch` muestra error de autenticación aunque la sesión ya quedó guardada. No se verificó este escenario. | `web/src/app/login/login.component.ts:50-65` | baja | CONFIRMADO |
| H-WEB-11 | web | Una respuesta de token inválido conserva la sesión local | Sugerencia | Ante un error al cargar tareas, el componente muestra el error sin limpiar el token ni redirigir. La prueba existente confirma que conserva el token; no se ha acordado si el flujo de sesión debe cerrarse ante esa respuesta. | `web/src/app/dashboard/dashboard.component.ts:35-68`; `web/src/app/dashboard/dashboard.component.spec.ts:92-106` | baja | CONFIRMADO |
| H-WEB-12 | web | El comparador de tareas no distingue prioridades iguales | Refactor | El comparador devuelve `1` también cuando las prioridades son iguales, en vez de indicar igualdad. Esto puede dejar el orden de tareas de igual prioridad dependiente del comportamiento de ordenación. No hay prueba para empates. | `web/src/app/dashboard/dashboard.component.ts:54`; `web/src/app/dashboard/dashboard.component.spec.ts:58-73` | baja | CONFIRMADO |

**Resolución H-WEB-03:** los mensajes ahora se representan con campos de texto
interpolados en el template; se eliminó el binding `[innerHTML]`.

### Límites de esta revisión

- Se ejecutaron las pruebas web de H-WEB-03; los comandos y resultados están
  registrados en `PLAN.md`. No se ejecutó `npm audit`, por lo que los advisories
  de dependencias siguen pendientes.
- La lista y los contratos de `auth` y `api` no se verificaron dentro de este
  alcance.

## Candidatos de discovery web

### H-WEB-07: Posible exposición de datos de login al registrar errores de Axios
- Estado: CONFIRMADO
- Evidencia: `web/src/app/login/login.component.ts:45-47,58-65` envía el cuerpo de login y luego registra el objeto de error completo con `console.error(error)`.
- Impacto: si el error registrado contiene configuración o datos de solicitud, información sensible de autenticación podría quedar visible en la consola. Esta exposición no se confirmó en ejecución.
- Prioridad propuesta: Media, pendiente de verificar el contenido real del objeto registrado.
- Contrato potencialmente afectado: manejo de errores de `POST /auth/login` y política de no registrar información sensible.
- Cómo validarlo: provocar un rechazo en una prueba aislada con un error Axios simulado y capturar la llamada al logger; verificar que el registro no contenga el cuerpo ni datos sensibles de la solicitud. No usar credenciales reales.

### H-WEB-08: Posible interrupción del dashboard por estado de usuario local malformado
- Estado: CONFIRMADO
- Evidencia: `web/src/app/dashboard/dashboard.component.ts:35-38` ejecuta `JSON.parse` del usuario almacenado antes de entrar al bloque `try`.
- Impacto: si el valor almacenado no es JSON válido, la inicialización puede rechazarse antes de completar la carga o redirigir, dejando el estado de pantalla sin resolver. No se observó este escenario en las pruebas actuales.
- Prioridad propuesta: Baja.
- Contrato potencialmente afectado: restauración de sesión y comportamiento de inicio del dashboard ante estado local inválido.
- Cómo validarlo: en una prueba, colocar un valor JSON malformado en `workshop_user`, invocar `ngOnInit` y comprobar el rechazo, navegación y estado de carga resultantes.

### H-WEB-09: Posible alteración del filtro `owner` al construir la URL de tareas
- Estado: CONFIRMADO
- Evidencia: `web/src/app/dashboard/dashboard.component.ts:43-45` concatena `this.user.username` directamente en la URL de `GET /api/tasks`.
- Impacto: caracteres reservados de URL podrían cambiar la interpretación de la consulta o producir un valor distinto en el servicio. No se revisó el comportamiento del API en este alcance.
- Prioridad propuesta: Baja, pendiente de confirmar el dominio de usernames permitido y la interpretación del servicio.
- Contrato potencialmente afectado: formato de consulta `owner` de `GET /api/tasks`.
- Cómo validarlo: probar con valores de username de test que incluyan caracteres reservados, inspeccionar la URL/parámetros recibidos por un doble del cliente HTTP y confirmar que `owner` conserva exactamente el valor esperado.

### H-WEB-10: Error de navegación tras login exitoso se presenta como fallo de autenticación
- Estado: CONFIRMADO
- Evidencia: `web/src/app/login/login.component.ts:50-65`; tras recibir token, el componente persiste la sesión y espera `router.navigate` dentro del mismo `try`, cuyo `catch` construye un error de autenticación.
- Impacto: si la navegación rechaza la promesa después de guardar la sesión, el usuario puede ver un error de autenticación pese a que sus datos de sesión ya quedaron persistidos. Este escenario no está cubierto por las pruebas actuales.
- Prioridad propuesta: Baja, pendiente de verificar el comportamiento de navegación fallida.
- Contrato potencialmente afectado: resultado visible y persistencia de sesión del flujo de login exitoso a `/dashboard`.
- Cómo validarlo: en una prueba aislada, simular respuesta exitosa de login y rechazo de `router.navigate`; comprobar el mensaje presentado, el estado `busy` y la persistencia de sesión, y acordar el resultado esperado.

### H-WEB-11: La sesión local se conserva cuando la carga de tareas informa token inválido
- Estado: CONFIRMADO
- Evidencia: `web/src/app/dashboard/dashboard.component.ts:35-68` maneja el error sin limpiar la sesión; `web/src/app/dashboard/dashboard.component.spec.ts:92-106` confirma explícitamente que el token se conserva en el caso probado.
- Impacto: si el error corresponde a una sesión realmente inválida o expirada, el estado local permanece y futuras inicializaciones podrían repetir la solicitud fallida. No se confirmó el contrato de expiración ni que todos los errores del servicio impliquen token inválido.
- Prioridad propuesta: Baja, pendiente de acordar el contrato para expiración/autorización.
- Contrato potencialmente afectado: restauración/cierre de sesión en el frontend y respuesta de autorización de `GET /api/tasks`.
- Cómo validarlo: con un caso de prueba controlado para una respuesta de token inválido y otro para un error no relacionado con autenticación, verificar el comportamiento de sesión y navegación esperado según el contrato acordado.

### H-WEB-12: El comparador de prioridades devuelve un valor no igual para empates
- Estado: CONFIRMADO
- Evidencia: `web/src/app/dashboard/dashboard.component.ts:54` devuelve `-1` si una prioridad es mayor y `1` en cualquier otro caso; `web/src/app/dashboard/dashboard.component.spec.ts:58-73` prueba prioridades distintas, no empates.
- Impacto: para prioridades iguales el comparador no comunica igualdad, por lo que el orden relativo puede depender de la entrada y del algoritmo de ordenación. No hay un requisito que defina el orden de empate.
- Prioridad propuesta: Baja.
- Contrato potencialmente afectado: orden de presentación de tareas de igual prioridad en el dashboard, si dicho orden forma parte del comportamiento esperado.
- Cómo validarlo: añadir una prueba con varias tareas con igual prioridad y verificar el orden esperado; acordar primero si se debe preservar el orden recibido u otro criterio.

### Validación ejecutada
- Comando: `cd web; npm test` — resultado: 3 archivos y 16 pruebas aprobados. Estas pruebas no cubren los escenarios candidatos descritos arriba.
