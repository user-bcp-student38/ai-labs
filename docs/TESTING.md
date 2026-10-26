# Pruebas de flujos web

Alcance de esta ejecución: `web/`, hallazgo relacionado H-WEB-06. Los tests
son unitarios y simulan Axios, Router y almacenamiento local; no se conectan a
los backends. Los casos de indisponibilidad, token inválido y timeout son
respuestas simuladas, no incidentes reproducidos en servicios reales.

## Casos y resultados esperados

| Flujo | Caso | Tipo | Resultado esperado |
|---|---|---|---|
| Login (`POST /auth/login`) | Credenciales válidas | Camino feliz | Se envía el body y timeout documentados; guarda token, usuario y preferencias; navega a `/dashboard`. |
| Tareas (`GET /api/tasks`) | Token presente y lista recibida | Camino feliz | Envía `owner` y Bearer; excluye `ARCHIVED`, agrega etiquetas, ordena tareas y quita estado de carga. |
| Login | Usuario vacío | Borde | Muestra `Ingrese un usuario.` y no llama al servicio. |
| Login | Contraseña vacía | Borde | Muestra `Ingrese una contraseña.` y no llama al servicio. |
| Login | Respuesta 2xx sin token | Borde / respuesta inesperada | Muestra `El backend respondió sin token.`, no guarda sesión ni navega. |
| Login | Servicio de autenticación no disponible (`Network Error`) | Error | Muestra el mensaje de error, restablece `busy` y no navega. |
| Login | Timeout de 5000 ms | Error | Muestra el error de timeout, restablece `busy` y no navega. |
| Tareas | Token ausente | Borde de sesión | Navega a `/login` y no llama a `/api/tasks`. |
| Tareas | Token inválido con `detail` | Error de autenticación | Muestra el `detail`, deja de cargar y no borra el token ni redirige. |
| Tareas | Timeout/rechazo de la petición | Error | Muestra el mensaje de Axios y termina la carga. En este endpoint la app no configura timeout explícito; el test simula el rechazo, conforme a la pregunta abierta de `ENDPOINTS.md`. |
| Tareas | Servicio no disponible | Error | Muestra `Network Error`, termina la carga y no redirige. |
| Tareas | Respuesta inesperada (`data: null`) | Error / respuesta inesperada | La excepción del procesamiento se muestra como error y termina la carga. |
| Tareas | Error sin `detail` ni `message` | Error / fallback | Muestra `No se pudo cargar el dashboard` y termina la carga. |

Código fuente de los tests: [login.component.spec.ts](../web/src/app/login/login.component.spec.ts)
y [dashboard.component.spec.ts](../web/src/app/dashboard/dashboard.component.spec.ts).
Vitest carga Angular JIT con `@angular/compiler`; la prueba sustituye Axios y
el almacenamiento por dobles controlados. Extractos de los casos principales:

```ts
it('preserves the login request and successful navigation behavior', async () => {
  const { component, router } = createComponent();
  component.username = 'student';
  component.password = 'Workshop123!';
  vi.mocked(axios.post).mockResolvedValue({
    data: { token: 'test-token', user: { username: 'student' } }
  } as never);

  await component.login();

  expect(axios.post).toHaveBeenCalledWith(
    'http://localhost:8080/auth/login',
    { username: 'student', password: 'Workshop123!', client: 'angular-workshop' },
    { timeout: 5000 }
  );
  expect(storage.get(tokenStorageKey)).toBe('test-token');
  expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
});

it('shows an unavailable authentication service error without navigating', async () => {
  const { component, router } = createComponent();
  component.username = 'student';
  component.password = 'Workshop123!';
  vi.spyOn(console, 'error').mockImplementation(() => undefined);
  vi.mocked(axios.post).mockRejectedValue(new Error('Network Error'));

  await component.login();

  expect(component.message.detail).toBe('Network Error');
  expect(component.busy).toBe(false);
  expect(router.navigate).not.toHaveBeenCalled();
});

it('loads, filters, labels, and sorts tasks for an authenticated user', async () => {
  storage.set('workshop_token', 'test-token');
  storage.set('workshop_user', JSON.stringify({ username: 'student' }));
  const { component } = createComponent();
  const tasks = [
    { status: 'TODO', priority: 'HIGH', owner: 'student', title: 'Review', description: 'High' },
    { status: 'ARCHIVED', priority: 'LOW', owner: 'student', title: 'Old', description: 'Archived' },
    { status: 'IN_PROGRESS', priority: 'URGENT', owner: 'student', title: 'Resolve', description: 'Urgent' }
  ];
  vi.mocked(axios.get).mockResolvedValue({ data: tasks } as never);

  await component.ngOnInit();

  expect(axios.get).toHaveBeenCalledWith(
    'http://localhost:8000/api/tasks?owner=student',
    { headers: { Authorization: 'Bearer test-token' } }
  );
  expect(component.visibleTasks.map((task) => task.title)).toEqual(['Resolve', 'Review']);
  expect(component.loading).toBe(false);
});

it('shows an invalid-token response and leaves the session in place', async () => {
  storage.set('workshop_token', 'expired-token');
  storage.set('workshop_user', JSON.stringify({ username: 'student' }));
  const { component, router } = createComponent();
  vi.mocked(axios.get).mockRejectedValue({
    response: { data: { detail: 'Token inválido' } }
  });

  await component.ngOnInit();

  expect(component.error).toBe('Token inválido');
  expect(storage.get('workshop_token')).toBe('expired-token');
  expect(router.navigate).not.toHaveBeenCalled();
});
```

Los tests completos (incluidos usuario/contraseña vacíos, respuesta sin token,
timeouts, error de red de tareas, respuesta inesperada y fallback) están en
los dos archivos `.spec.ts` enlazados arriba.

## Ejecuciones y cobertura

Los siguientes comandos se ejecutaron desde el directorio `web/`:

| Comando | Código de salida | Resultado |
|---|---:|---|
| `npm run test:coverage -- src/app/login/login.component.spec.ts src/app/dashboard/dashboard.component.spec.ts` | 0 | 2 archivos, 15 tests aprobados. |
| `npm run test:coverage` | 0 | Suite completa: 3 archivos, 16 tests aprobados. Reporte de cobertura generado por Vitest/V8 en `web/coverage/`. |
| `git diff --check` (comando ejecutado desde la raíz) | 0 | Sin errores de whitespace en el diff comprobado. También se inspeccionaron los archivos nuevos de pruebas y documentación para whitespace final. |

**Método:** cobertura de líneas y statements reportada por el proveedor V8 de
Vitest; numerador = líneas/statements ejecutados, denominador = líneas/
statements instrumentados del archivo. Los archivos de producción ejercitados
por los tests de login y tareas quedan así:

| Archivo de producción probado | Líneas cubiertas / instrumentadas | Cobertura de líneas | Statements cubiertos / instrumentados | Cobertura de statements |
|---|---:|---:|---:|---:|
| `web/src/app/login/login.component.ts` | 61 / 63 | 96.82% | 61 / 63 | 96.82% |
| `web/src/app/dashboard/dashboard.component.ts` | 62 / 68 | 91.17% | 62 / 68 | 91.17% |
| Ambos archivos anteriores (agregado ponderado) | 123 / 131 | 93.89% | 123 / 131 | 93.89% |

Ambos módulos de producción directamente cubiertos por las nuevas pruebas
superan el 85% de líneas. No se modificó código de producción en este paso;
la cobertura indicada es de los módulos bajo prueba, no de los propios
archivos `.spec.ts`.

### Cobertura global

El total de `web/src` en la ejecución completa fue de **129 / 167 líneas
(77.24%)**, **129 / 167 statements (77.24%)**, **6 / 10 funciones (60%)** y
**30 / 39 branches (76.92%)**. La cobertura global está por debajo del 85%;
no se cambió ningún umbral. La diferencia se debe, entre otros, a que
`main.ts` y `app.component.ts` no están cubiertos por estas pruebas.

## Fallos, pendientes y pruebas propuestas

- **Fallos de pruebas ejecutadas:** ninguno.
- **Pendiente de integración real:** no se iniciaron auth/api ni se verificó
  una respuesta HTTP real; los códigos de status, CORS y esquemas del servidor
  siguen pendientes de coordinación según `docs/ENDPOINTS.md`.
- **Pendiente de timeout real en tareas:** el componente no configura un
  timeout para `GET /api/tasks`; el test cubre cómo la UI maneja una promesa
  rechazada con mensaje de timeout, no que la petición realmente expire tras
  una duración determinada.
- **H-WEB-06 permanece Pendiente:** login y carga de tareas tienen cobertura,
  pero no se añadió una prueba para logout ni para el arranque de la app.
- **Pruebas propuestas:** prueba de integración con los backends del laboratorio
  para status y CORS; decidir y documentar timeout para `/api/tasks` antes de
  probar su duración; agregar prueba de logout y de integración/renderizado
  cuando esos flujos se seleccionen.
