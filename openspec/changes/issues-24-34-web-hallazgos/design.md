# Diseño: hallazgos web de los Issues #24 a #34

## Contexto técnico

- Componente: `web/` (Angular 21, Vitest 3.2.4 con `environment: 'node'` e `include: src/**/*.spec.ts`, Axios 0.31.0, lodash 4.17.21).
- Verificación: `python tools/lab.py test web` ejecuta `npm test`, `npm run test:coverage` y `npm run build`. El umbral de 85% no está configurado en Vitest; se verifica a mano sobre los archivos modificados.
- Línea base: 3 archivos y 16 pruebas en verde (`legacy-format` 1, `dashboard.component` 7, `login.component` 8).
- Código actual relevante:
  - [login.component.ts](../../../web/src/app/login/login.component.ts): validación, `axios.post` a `/auth/login`, persistencia de `workshop_token`, `workshop_user` y `workshop_preferences`, navegación a `/dashboard` dentro del mismo `try` que el `catch` de autenticación, y `console.error(error)` en el `catch`.
  - [dashboard.component.ts](../../../web/src/app/dashboard/dashboard.component.ts): `JSON.parse` de `workshop_user` antes de comprobar el token, concatenación de `owner` sin codificar, filtro exacto `ARCHIVED`, comparador `a.priority > b.priority ? -1 : 1` y logout con eliminación de token y usuario.
- Contratos: [docs/ENDPOINTS.md](../../../docs/ENDPOINTS.md) y las specs de este cambio.

## Goals

- Corregir los hallazgos con criterio definido (#25, #26, #27, #28, #29, #30, #31, #32) sin cambiar los contratos observados.
- Dejar cada decisión pendiente (#24, #33, #34, texto de #32) explícita y bloqueante para su fase.
- Ejecutar cada cambio en una fase pequeña, verificable y revertible.

## Non-goals

- No cambiar `auth/` ni `api/`, ni los endpoints, formatos o campos que consume `web/`.
- No mover el token a cookie `HttpOnly` (#24); requiere acuerdo con auth y api.
- No definir el comportamiento de token inválido (#33) ni el criterio de empate (#34) sin decisión humana.
- No cambiar el orden lexicográfico actual por `priority` ni la codificación de `owner` en el valor de los usuarios actuales.
- No añadir entorno DOM ni dependencias de pruebas (Angular TestBed, jsdom, happy-dom).
- No rediseñar la aplicación: no se añaden stores, routers ni librerías de estado.
- No renombrar `workshop_token`, `workshop_user` ni `workshop_preferences`.

## Contratos protegidos

| Contrato | Protegido por |
|---|---|
| `POST /auth/login` con `{username, password, client: "angular-workshop"}` y timeout 5000 ms | prueba "preserves the login request..." |
| Éxito con `token` truthy: guarda las tres claves de `localStorage` y navega a `/dashboard` | misma prueba |
| Validación local `Ingrese un usuario.` / `Ingrese una contraseña.` sin petición | pruebas de validación |
| `El backend respondió sin token.` | prueba de token ausente |
| Error de login `No se pudo autenticar a <usuario>.` con detalle del backend o de Axios | pruebas de error y timeout |
| `GET /api/tasks?owner=<usuario>` con `Authorization: Bearer <token>` | prueba de carga de tareas |
| Sin token: navega a `/login` sin llamar a la API | prueba "redirects to login..." |
| Excluir `ARCHIVED` (coincidencia exacta) y ordenar por `priority` | prueba de carga de tareas |
| Error de tareas: muestra `detail`, `message` o `No se pudo cargar el dashboard`; no borra sesión | pruebas de error del dashboard |
| Logout: elimina `workshop_token` y `workshop_user` y navega a `/login` | pruebas de característica nuevas (fase 2) |
| Claves de `localStorage` y forma de las entradas | pruebas de login (`storage.get(...)`) |

## Componentes afectados

- [login.component.ts](../../../web/src/app/login/login.component.ts) y su spec.
- [dashboard.component.ts](../../../web/src/app/dashboard/dashboard.component.ts) y su spec.
- Nuevos archivos de soporte bajo `web/src/app/core/` solo en la fase de refactor: lectura y escritura de sesión, cliente de login, cliente de tareas y tipos. No se modifican templates ni `app.routes.ts`.
- [app.routes.ts](../../../web/src/app/app.routes.ts): solo lectura, para la prueba de arranque.
- [docs/ENDPOINTS.md](../../../docs/ENDPOINTS.md): actualizar únicamente la sección `web/` cuando cambie un comportamiento documentado (#29, #30, #31, #32).

## Decisiones técnicas y alternativas

**D1. Alcance por fases, con prueba antes de cambio.** Cada issue es una fase, en el orden de la sección de estrategia. La alternativa de un cambio único descartada: mezcla refactor y cambio de comportamiento, y dificulta revertir.

**D2. Pruebas sin DOM.** Se mantiene `environment: 'node'`. Las pruebas de template leen el HTML como texto, igual que hoy. Alternativa descartada: añadir jsdom o TestBed. Introduce dependencias nuevas sin criterio que lo justifique.

**D3. Codificación de `owner` con `encodeURIComponent` en la cadena de la URL.** Para usernames sin caracteres reservados la URL es idéntica a la actual, y la prueba existente con `?owner=student` sigue válida. Alternativa descartada: el parámetro `params` de Axios. Cambia la codificación de espacios (`+` en lugar de `%20`) y obliga a cambiar la aserción de la prueba existente.

**D4. Log sanitizado en login (#29).** Se sustituye `console.error(error)` por un registro con el mensaje fijo y solo `status` y `code` del error, cuando existan. Alternativas: eliminar el log (oculta fallos al operador) o usar un servicio de logging (añade complejidad sin necesidad). La prueba captura el logger simulado y verifica que no aparecen `username`, `password` ni el cuerpo de la solicitud.

**D5. Sesión válida requiere token truthy y usuario parseable (#30).** Se comprueba el token antes de parsear el usuario. Un `workshop_user` malformado se trata como sesión ausente: navegación a `/login` y sin llamada a `/api/tasks`. No se borran claves de `localStorage` en esta fase; alternativa de borrar ambas claves queda registrada como opción si el equipo lo prefiere, porque cambia datos persistidos.

**D6. Navegación fuera del `catch` de autenticación (#32).** Se separan dos bloques: la petición y la persistencia de sesión siguen en el bloque de autenticación; la navegación queda en su propio manejo de error. El texto del mensaje de navegación se modela como constante con valor pendiente. La fase no se fusiona hasta que el texto esté decidido.

**D7. Comparador con empate igual a 0 (#34), solo tras decisión.** Si el criterio acordado es el orden recibido, el comparador devuelve `0` para igual `priority`, y el orden estable de `Array.prototype.sort` conserva el orden de entrada. Alternativa de un criterio explícito (por ejemplo, por título) se descarta por ahora porque no está acordada. El comparador actual devuelve `1` en empates, lo que depende de la implementación del motor; por eso el cambio sí es necesario una vez decidido el criterio.

**D8. Token inválido (#33) sin cambio.** Distinguir token inválido de otros errores requiere saber qué estado HTTP responde api, dato que ENDPOINTS marca como pregunta abierta. Sin esa confirmación no se implementa; si se confirma un 401, la decisión queda como fase separada.

**D9. Token en `localStorage` sin cambio (#24).** Bloqueado por coordinación con auth y api. No se introduce ningún mecanismo nuevo en esta propuesta.

**D10. Refactor de servicios (#26, #27) sin cambiar la superficie de los componentes.** Se crean servicios inyectables que encapsulan la llamada HTTP y la sesión. Los componentes mantienen sus métodos (`login()`, `ngOnInit()`, `logout()`) y sus campos (`message`, `busy`, `tasks`, `visibleTasks`, `user`, `loading`, `error`), así que los templates no cambian. Como los servicios siguen importando `axios`, los `vi.mock('axios')` existentes siguen interceptando las llamadas. El cambio de constructor obliga a actualizar la construcción de componentes en las specs; se hace en un commit aparte de la lógica.

**D11. Navegación en los componentes (ambigüedad de la spec).** La spec #26 dice que los componentes "delegan la navegación". Se interpreta como: `Router` sigue siendo usado por los componentes, y solo la gestión de sesión y HTTP sale a servicios. Alternativa: un servicio de navegación que devuelva resultados. Se descarta por ser un rediseño. Se marca para revisión humana, porque la frase de la spec es ambigua.

**D12. Identidad de `tasks` y mutación de `label` se preservan.** La prueba actual verifica que `component.tasks` es la misma referencia que la respuesta y que `label` se añade a cada tarea. Cambiar eso sería un cambio de comportamiento sin issue que lo pida. En la fase de tipos (#27), `label` se declara como opcional en la interfaz de tarea.

**D13. Tipos (#27).** Se definen interfaces para la respuesta de login, el usuario de sesión y la tarea. `LoginMessage` ya existe y se reutiliza. Para `response.data` nulo, el código actual lanza en `.length` y el `catch` muestra error; esa prueba se conserva, así que no se añade una guarda que cambie ese resultado.

## Estrategia incremental

Orden de fases. Cada fase termina con `python tools/lab.py test web` en verde antes de pasar a la siguiente.

1. **Línea base de dependencias (#25).** Ejecutar `npm audit --audit-level=moderate` en `web/` y registrar el resultado en el issue. Sin cambios de código. Si hay advisories aplicables, se decide en esta fase si se actualiza; una actualización de Axios puede cambiar la forma de los errores, así que se valida con todas las pruebas antes de seguir.
2. **Pruebas de caracterización (#28, parte previa).** Añadir sin modificar código:
   - login: usuario y contraseña con solo espacios muestran el mensaje de validación;
   - login: rechazo sin `response.data` usa el fallback `Error desconocido`;
   - login: respuesta 2xx sin `data` muestra el mensaje de token ausente;
   - login: `workshop_preferences` malformado se ignora y se usan las preferencias por defecto;
   - dashboard: sin `workshop_user` válido en la sesión, `owner` queda vacío (`?owner=`);
   - dashboard: `ARCHIVED` se excluye solo con coincidencia exacta (en minúsculas no se excluye);
   - logout: elimina `workshop_token` y `workshop_user`, conserva `workshop_preferences` y navega a `/login`;
   - arranque: la tabla de rutas redirige `''` y `**` a `/login` y los `loadComponent` resuelven a los componentes.

   No se caracteriza el orden de empate (#34) ni la navegación fallida (#32), porque son comportamientos a corregir o no definidos.
3. **Log sanitizado (#29).** Prueba roja: el logger simulado recibe datos sensibles. Cambio mínimo en el `catch` de login según D4. Actualizar la sección de login de ENDPOINTS.
4. **Sesión malformada (#30).** Pruebas rojas: con `workshop_user` inválido, `ngOnInit` no lanza, navega a `/login` y no llama a Axios. Cambio según D5.
5. **Codificación de `owner` (#31).** Pruebas rojas: un username con `&`, `=`, `#` o `+` produce el valor exacto al decodificar. Un username simple produce la misma URL que hoy. Cambio según D3.
6. **Navegación fallida (#32).** Bloqueado por la decisión del texto y de la persistencia. Una vez decidido, prueba roja con `router.navigate` rechazado: el mensaje no es `No se pudo autenticar a <usuario>.`, `busy` vuelve a `false`, y la sesión queda en el estado decidido. Cambio según D6.
7. **Empates (#34).** Bloqueado por la decisión. Prueba de orden de empate con el criterio acordado. Cambio según D7.
8. **Token inválido (#33) y token en `localStorage` (#24).** Sin cambio de código en esta propuesta; se registran las decisiones y los requisitos de coordinación.
9. **Refactor (#26, #27).** Con las fases 2 a 5 en verde: extraer servicios y tipos según D10 y D13. Primer commit: solo cambios de tipos y de construcción de componentes en specs. Segundo commit: mover la lógica HTTP y de sesión. Ambos con las mismas pruebas.
10. **Cierre (#28).** Verificar cobertura de los archivos modificados mediante `coverage/coverage-summary.json` (≥85%), confirmar que `auth/` y `api/` no tienen cambios y que `python tools/lab.py test web` pasa.

## Pruebas de caracterización necesarias antes de modificar

- Antes de la fase 3: prueba del comportamiento de login sin sesión y de errores (ya existente) más las nuevas de la fase 2.
- Antes de la fase 4: prueba de carga con sesión válida (ya existente) y de logout (nueva).
- Antes de la fase 5: prueba de carga con `owner` simple (ya existente), para verificar que la URL no cambia.
- Antes de la fase 9: todas las pruebas de las fases 2 a 5 en verde.

## Riesgos y trade-offs

- **Coordinación de #24 y #33:** sin respuesta de auth y api, el cambio de sesión queda fuera. El riesgo se traslada a las preguntas abiertas de ENDPOINTS.
- **Decisiones pendientes bloquean fases:** #32 y #34 no se fusionan hasta tener el texto y el criterio. Si no hay decisión, la fase se omite y se documenta.
- **Refactor de constructores:** cambiar la firma de los componentes rompe la construcción de las specs; se mitiga con un commit separado.
- **Ambigüedad de la spec #26:** D11 interpreta "delegan la navegación". Si el equipo quiere otra interpretación, la fase 9 cambia.
- **Pruebas sin DOM:** las pruebas de arranque no renderizan la aplicación. Cubren la configuración de rutas, no el render.
- **Cobertura sin umbral automático:** el 85% se verifica a mano. El comando de cobertura regenera `web/coverage/`, lo que puede ensuciar el diff; se revisa antes de incluirlo.
- **Dependencias:** actualizar Axios o lodash por advisories puede cambiar errores y timeouts; por eso va primero y se valida con las pruebas de caracterización.

## Rollback

- Cada fase es un cambio revertible de forma independiente. Revertir una fase con `git revert` de su commit, sin tocar otras fases ni ramas.
- Las claves de `localStorage` y su forma no cambian, así que revertir no requiere migración de datos ni afecta sesiones guardadas.
- La fase 9 se revierte en orden inverso: primero el commit de movimiento de lógica, luego el de tipos y construcción.
- Si se modifica `docs/ENDPOINTS.md`, la revisión de la sección `web/` se revierte junto con su fase.
- Si una actualización de dependencias (fase 1) rompe el build, se revierte el `package.json` y `package-lock.json` en el mismo commit.
