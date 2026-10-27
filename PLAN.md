# Plan de corrección

## Alcance web

Se seleccionaron los hallazgos Pendiente **H-WEB-01** y **H-WEB-03**, por ser
los dos riesgos de seguridad de interfaz de la matriz de `HALLAZGOS.md`.
`docs/ENDPOINTS.md` no existe; se conserva como referencia el comportamiento
observado, sin inventar un contrato acordado. No se modifica código hasta que
una persona revise y apruebe el plan.

### Aprobación humana
- Pedro Yturrizaga
- 2026/10/26
- H-WEB-01, H-WEB-03

### H-WEB-01 — Token de sesión en `localStorage`

- **Hallazgo:** H-WEB-01 de `HALLAZGOS.md` (componente web, estado Pendiente).
- **Problema:** el login entrega un token en la respuesta y lo guarda en
  `localStorage`; el dashboard lo lee y lo envía como Bearer. Código de
  referencia: `web/src/app/login/login.component.ts:41-49` y
  `web/src/app/dashboard/dashboard.component.ts:36,43-45`. Scripts del mismo
  origen pueden leer el token.
- **Cambio propuesto:** no reemplazar `localStorage` por otra opción web-only
  como si eliminara el acceso de JavaScript o conservara el comportamiento. La
  remediación propuesta queda **bloqueada** hasta coordinar con los responsables
  de auth y api una sesión mediante cookie `HttpOnly` y un contrato compatible
  con login, consulta de tareas y cierre de sesión. El frontend deberá dejar de
  leer o persistir el token y usar el mecanismo acordado. No se presume que los
  backends acepten cookies, ni que exista un endpoint de logout. No tocar auth
  ni api en este alcance.
- **Archivos web a tocar, solo tras acordar el contrato:** `web/src/app/login/login.component.ts`,
  `web/src/app/dashboard/dashboard.component.ts` y, si la separación de sesión
  acordada lo requiere, un servicio nuevo bajo `web/src/app/`. Las pruebas se
  añadirían en archivos `.spec.ts` bajo `web/src/app/`. Los archivos exactos y
  los cambios de protocolo quedan pendientes de la coordinación externa.
- **Prueba de caracterización antes de modificar código:** añadir pruebas del
  flujo actual que comprueben que login envía el `POST /auth/login` con
  `username`, `password` y `client`, persiste token y usuario, y navega al
  dashboard; que el dashboard consulta `/api/tasks` con `owner` y cabecera
  Bearer; y que logout elimina token y usuario y vuelve a `/login`. Esto fija
  la línea base observada, no aprueba la persistencia insegura.
- **Pruebas posteriores, solo después de acordar el contrato:** cubrir login
  con la respuesta/cookie acordada, carga de tareas autenticada mediante el
  mecanismo acordado, ausencia de token en almacenamiento accesible a
  JavaScript y cierre de sesión efectivo. No definir ni ejecutar estas
  expectativas antes de la coordinación con auth y api.
- **Riesgo/decisión pendiente:** cambiar a cookie requiere que los servicios
  server-side emitan, acepten y expiren la cookie de forma compatible con los
  orígenes y CORS del laboratorio. Si no se aprueba ese contrato, H-WEB-01
  permanece pendiente; no se considerará mitigado por centralizar el acceso a
  `localStorage`.

### H-WEB-03 — Mensaje dinámico enlazado con `[innerHTML]`

- **Hallazgo:** H-WEB-03 de `HALLAZGOS.md` (componente web, estado Pendiente).
- **Problema:** el mensaje concatena texto del usuario y del backend y lo
  muestra con `[innerHTML]`. Angular sanitiza este binding por defecto, por lo
  que la revisión no confirma una XSS; aun así, el marcado dinámico no es
  necesario y deja una superficie de interpretación evitable. Código de
  referencia: `web/src/app/login/login.component.ts:54-57` y
  `web/src/app/login/login.component.html:39`.
- **Cambio propuesto:** representar el título y el detalle del mensaje como
  datos de texto, conservar las mismas palabras y el énfasis visual con
  elementos HTML estáticos, e interpolar cada valor en el template. Eliminar
  el binding `[innerHTML]` y no construir fragmentos HTML desde credenciales o
  respuestas del backend. Mantener la validación, el endpoint, el payload y el
  flujo de navegación observados.
- **Archivos web a tocar:** `web/src/app/login/login.component.ts`,
  `web/src/app/login/login.component.html` y una prueba nueva
  `web/src/app/login/login.component.spec.ts`. No se anticipa añadir
  dependencias; el runner actual de Vitest usa entorno `node`, por lo que la
  prueba debe validar el modelo de mensaje y el binding de texto sin asumir un
  entorno DOM no configurado.
- **Prueba de caracterización antes de modificar código:** con Axios simulado y
  un Router espía, comprobar las palabras mostradas para usuario/contraseña
  vacíos, respuesta sin token y error de backend; comprobar que el nombre y el
  mensaje de backend se conservan. Registrar la forma de presentación actual
  como línea base, sin ejecutar contenido HTML de prueba.
- **Pruebas posteriores:** comprobar que el modelo mantiene esos textos, que
  el template usa interpolación para los valores dinámicos y que no conserva
  `[innerHTML]`. Incluir valores con caracteres como `<`, `>` y `&` para
  verificar que permanecen como texto, no como marcado. Confirmar además que
  el login sigue enviando el mismo payload y que los errores no cambian el
  flujo de navegación.
- **Riesgo/decisión pendiente:** Vitest está configurado con entorno `node`.
  Una verificación real de renderizado requeriría introducir y justificar un
  entorno DOM de prueba; no se añade esa dependencia en esta propuesta. El
  test de binding de template debe documentar esta limitación.

## Secuencia y controles

1. Obtener revisión y aprobación humana del alcance antes de implementar.
2. Para H-WEB-03, escribir y ejecutar primero las pruebas de caracterización en
   `web/`; conservar sus resultados reales antes de editar los archivos.
3. Para H-WEB-01, completar la coordinación externa con auth y api y registrar
   el contrato acordado. Hasta entonces no cambiar el almacenamiento ni
   afirmar que el riesgo está resuelto.
4. Implementar solo los cambios aprobados dentro de `web/`, ejecutar las
   pruebas del componente desde `web/` y volver a revisar el diff.
5. Actualizar estados y evidencia en `HALLAZGOS.md` únicamente con resultados
   observados; no inventar aprobación, contrato ni ejecución.

## Ejecución — H-WEB-03

- **Aprobación:** el alcance H-WEB-01 y H-WEB-03 aparece aprobado por Pedro
  Yturrizaga con fecha 2026/10/26. H-WEB-01 no se implementó: la coordinación
  con auth y api requerida por este plan y el contrato de cookie siguen
  pendientes.
- **Caracterización previa (web, H-WEB-03):** se creó
  `web/src/app/login/login.component.spec.ts` antes de editar el componente o
  template. `npm test -- src/app/login/login.component.spec.ts` ejecutado desde
  la raíz falló porque no encontró `package.json`. Ejecutado con cwd `web/`,
  Vitest informó que faltaba cargar el compilador JIT de Angular. Se añadió
  `import '@angular/compiler'` a la prueba (sin instalar dependencias). La
  repetición del mismo comando con cwd `web/` pasó: 1 archivo, 6 pruebas.
- **Cambio ejecutado (web, H-WEB-03):** `LoginComponent` ahora modela título y
  detalle como texto; el template conserva el énfasis y salto de línea con
  elementos estáticos e interpolación, sin `[innerHTML]`. Se añadió cobertura
  de mensajes de validación, respuesta sin token, error de backend y login
  exitoso, además de una comprobación estática de las interpolaciones del
  template.
- **Prueba posterior y cobertura (web, H-WEB-03):**
  `npm test -- --coverage src/app/login/login.component.spec.ts` (cwd `web/`)
  pasó: 1 archivo, 6 pruebas. Para `src/app/login/login.component.ts`, Vitest reportó
  96.82% de statements y líneas, 100% de funciones y 76.92% de branches. La
  cobertura de líneas del archivo modificado supera el objetivo del 85%; la
  métrica de branches queda registrada por separado. El runner Node no hace
  render real de Angular: la prueba verifica el texto del modelo y que el
  template usa interpolación sin `innerHTML`.
- **Build (web, H-WEB-03):** `npm run build` (cwd `web/`) terminó correctamente.
  Angular mantuvo advertencias de CommonJS para axios y lodash, dependencias
  fuera del cambio seleccionado.
- **Regresión del componente (web, H-WEB-03):** `npm test` (cwd `web/`) pasó
  dos veces, ambas con 2 archivos y 7 pruebas en total; la segunda ejecución
  fue después de agregar limpieza de mocks a la prueba.
- **Caracterización antes/después (web, H-WEB-03):** antes del cambio pasaron
  las 6 pruebas que fijaban los textos HTML anteriores, el request de login,
  almacenamiento y navegación. Después pasaron las 6 pruebas adaptadas al
  modelo de texto y la ausencia de `[innerHTML]`; el request, el almacenamiento
  de login y la navegación siguen verificados. No se ejecutó un flujo integrado
  contra los backends ni se volvió a probar tareas/logout, que no cambiaron.
- **Estado:** H-WEB-03 se marca Resuelto en `HALLAZGOS.md`. H-WEB-01 permanece
  Pendiente hasta acordar el contrato de cookie con auth y api. No se agregó
  ninguna dependencia.
