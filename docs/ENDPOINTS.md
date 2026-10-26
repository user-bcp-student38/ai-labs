# Contratos externos observados

Este documento describe lo que el frontend web envía y consume. Los ejemplos
de respuesta se basan en las pruebas frontend o en la forma que el código
accede; no son capturas de respuestas reales de los backends ni un contrato
confirmado por sus responsables. Axios considera exitosas por defecto las
respuestas HTTP 2xx. No se inspeccionó el código de `auth/` ni `api/` en este
paso, por lo que cualquier comportamiento del servidor que no se deduce del
consumidor queda como pregunta abierta.

## Web — `POST /auth/login`

### 1. Quién lo llama

`LoginComponent.login()` llama directamente a Axios al pulsar **Ingresar**.
Antes de hacer la petición, exige que usuario y contraseña no estén vacíos.
Fuente: `web/src/app/login/login.component.ts:36-49`.

### 2. Qué envía

URL observada: `http://localhost:8080/auth/login`. El componente no configura
headers explícitos. No envía token de sesión ni parámetros de query. El timeout
configurado para esta petición es de 5000 ms.

| Parte | Campo | Valor enviado |
|---|---|---|
| Método y URL | — | `POST http://localhost:8080/auth/login` |
| Headers | — | Ninguno configurado explícitamente por el componente. El valor efectivo de headers que Axios agrega por defecto no se verifica aquí. |
| Body | `username` | Texto ingresado en el campo Usuario |
| Body | `password` | Texto ingresado en el campo Contraseña |
| Body | `client` | Constante `angular-workshop` |
| Configuración Axios | `timeout` | `5000` ms |

Ejemplo de body con credenciales ficticias usadas por el laboratorio:

```json
{
  "username": "student",
  "password": "Workshop123!",
  "client": "angular-workshop"
}
```

La estructura y el valor de `client` están en
`web/src/app/login/login.component.ts:46-49`; las credenciales ficticias
aparecen en `README.md`. La prueba confirma el body y el timeout en
`web/src/app/login/login.component.spec.ts:101-113`.

### 3. Qué espera recibir en éxito

El consumidor acepta cualquier respuesta HTTP 2xx que Axios resuelva y cuyo
`response.data.token` sea truthy. No valida un status específico, el tipo ni
los demás campos. Lee y guarda `data.token` y `data.user`, persiste también las
preferencias, y navega a `/dashboard`
(`web/src/app/login/login.component.ts:51-55`).

| Campo consumido | Uso |
|---|---|
| `token` | Requerido como valor truthy; se guarda en `localStorage` bajo `workshop_token`. |
| `user` | Se serializa y guarda bajo `workshop_user`; su estructura no se valida en el login. |

Ejemplo de respuesta compatible con la prueba frontend:

```json
{
  "token": "test-token",
  "user": {
    "username": "student"
  }
}
```

Este ejemplo reproduce el fixture de `web/src/app/login/login.component.spec.ts:103-106`;
no es una respuesta capturada del backend. **Pregunta abierta:** confirmar con
auth el status de éxito, el esquema completo de `user`, el formato real del
token y los headers definitivos de la petición.

### 4. Reacción ante errores o respuestas inesperadas

| Caso | Reacción observada en la UI |
|---|---|
| Usuario vacío | No envía la petición y muestra `Ingrese un usuario.`. |
| Contraseña vacía | No envía la petición y muestra `Ingrese una contraseña.`. |
| Error HTTP, incluidas credenciales inválidas si el backend responde con un status no 2xx | Axios rechaza la petición. El componente muestra el título `No se pudo autenticar a <usuario>.` y como detalle `error.response.data.message`, o `error.message`, o `Error desconocido`, en ese orden. También registra el objeto de error en consola. No navega al dashboard. |
| Timeout | Si la petición excede los 5000 ms, el error pasa por el mismo `catch`; la UI muestra el mensaje provisto por Axios (o `Error desconocido`) y no navega. |
| Respuesta 2xx sin `data.token` truthy | Muestra `El backend respondió sin token.`; no guarda usuario/token ni navega. |
| JSON mal formado o fallo de red | Entra al mismo `catch`; muestra el mensaje de Axios o `Error desconocido`, y no navega. |

La validación, el `catch` y el restablecimiento de `busy` están en
`web/src/app/login/login.component.ts:37-69`. **Pregunta abierta:** el texto
exacto que devuelve auth para credenciales inválidas y otros errores no se
confirmó contra el backend.

### 5. Notas de seguridad

- El token se guarda en `localStorage` (`workshop_token`), accesible a
  JavaScript del origen; el plan registra la coordinación pendiente para una
  posible alternativa de cookie HttpOnly. Evidencia:
  `web/src/app/login/login.component.ts:51-54` y `PLAN.md`, apartado H-WEB-01.
- Los mensajes se presentan con interpolación de texto y elementos HTML
  estáticos; no se usa `[innerHTML]` en el template actual:
  `web/src/app/login/login.component.html:39-42`.
- El login muestra el mensaje recibido del backend y registra el error en
  consola. No se verificó si esos detalles son adecuados para producción.
- El README advierte que se deben usar credenciales ficticias de laboratorio,
  no credenciales reales.

## Web — `GET /api/tasks`

### 1. Quién lo llama

`DashboardComponent.ngOnInit()` consulta las tareas al inicializar el
dashboard, siempre que encuentre un token en `localStorage`. Si no hay token,
navega a `/login` y no llama al endpoint. Fuente:
`web/src/app/dashboard/dashboard.component.ts:35-45`.

### 2. Qué envía

URL observada: `http://localhost:8000/api/tasks`. No envía body. El query
`owner` se concatena desde `user.username` (o cadena vacía si no existe); no se
codifica explícitamente. Envía el token como Bearer en `Authorization`.

| Parte | Campo | Valor enviado |
|---|---|---|
| Método y URL | — | `GET http://localhost:8000/api/tasks` |
| Header | `Authorization` | `Bearer <valor de workshop_token>` |
| Query param | `owner` | `user.username` leído de `localStorage`; si falta, `''` |
| Body | — | Ninguno configurado |
| Timeout | — | No configurado explícitamente para esta petición |

Ejemplo de forma de petición (token ficticio):

```http
GET /api/tasks?owner=student HTTP/1.1
Host: localhost:8000
Authorization: Bearer test-token
```

La URL y el header se construyen en
`web/src/app/dashboard/dashboard.component.ts:36-45`. **Pregunta abierta:**
confirmar con api si `owner` es obligatorio, las reglas de codificación del
valor y el comportamiento de autenticación/autorización.

### 3. Qué espera recibir en éxito

El consumidor acepta cualquier respuesta HTTP 2xx que Axios resuelva y usa
`response.data` como una colección con `.length`, elementos y `.sort()`. No
valida el status ni el esquema antes de recorrerla. Descarta elementos cuyo
`status` sea exactamente `ARCHIVED`, genera `label` usando `priority` y
`owner`, y ordena las tareas visibles por `priority`
(`web/src/app/dashboard/dashboard.component.ts:46-55`).

| Campo de cada tarea leído | Uso observado |
|---|---|
| `status` | Excluye `ARCHIVED`; se usa también para la etiqueta de estado. |
| `priority` | Se usa para ordenar y formar `label`. |
| `owner` | Se usa para formar `label`. |
| `title` | Se muestra en la tarjeta del dashboard. |
| `description` | Se muestra en la tarjeta del dashboard. |

El campo `label` se genera en frontend; no se requiere que venga del servidor.
`formatTaskLabel` aplica `NORMAL` si `priority` está vacío y `SIN-ASIGNAR` si
`owner` está vacío (`web/src/app/utils/legacy-format.ts:1-8`).

Ejemplo de forma de datos compatible con los campos leídos:

```json
[
  {
    "status": "TODO",
    "priority": "HIGH",
    "owner": "student",
    "title": "Revisar solicitud",
    "description": "Validar los datos recibidos"
  }
]
```

Este ejemplo es ilustrativo de los accesos de
`web/src/app/dashboard/dashboard.component.ts:46-55` y
`web/src/app/dashboard/dashboard.component.html:19-23`; no es una respuesta
capturada ni una garantía del esquema de api. **Pregunta abierta:** confirmar
el status de éxito, la estructura de respuesta y los tipos/campos requeridos
con el responsable de api.

### 4. Reacción ante errores o respuestas inesperadas

| Caso | Reacción observada en la UI |
|---|---|
| No hay token guardado | Navega a `/login` sin llamar al endpoint. |
| Token inválido/expirado, si api responde con error HTTP | Axios rechaza la petición; el dashboard muestra `error.response.data.detail`, o `error.message`, o `No se pudo cargar el dashboard`, en ese orden. Deja de mostrar el estado de carga. No borra el token ni redirige automáticamente al login. |
| Timeout | No hay timeout configurado explícitamente en esta llamada. Si la petición rechaza por un timeout de Axios u otro nivel de configuración, aplica el mismo mensaje de error; no se observó una configuración global de timeout en el componente. |
| Respuesta inesperada, por ejemplo `data` nulo o ausente | El acceso a `.length` puede lanzar; el `catch` muestra `detail`, `message` o el texto genérico, y detiene el estado de carga. Si `data` es un objeto sin `length`, el bucle no procesa tareas y la UI termina mostrando el estado vacío. |
| Error de red u otro rechazo | Aplica el mismo `catch` y la misma selección de mensaje; no elimina la sesión ni redirige. |

La reacción está implementada en
`web/src/app/dashboard/dashboard.component.ts:46-70`. **Pregunta abierta:**
confirmar el formato de errores de api, incluidos los de token inválido, para
saber si la UI mostrará `detail`, `message` o el fallback.

### 5. Notas de seguridad

- El token se lee de `localStorage` y se adjunta como Bearer. No existe
  centralización de sesión en un servicio en el código observado:
  `web/src/app/dashboard/dashboard.component.ts:36-44`.
- El valor de `owner` se concatena directamente a la URL en lugar de codificarse
  como parámetro de query mediante una utilidad de URL
  (`web/src/app/dashboard/dashboard.component.ts:43`). La autorización efectiva
  no puede confirmarse desde el consumidor.
- Los títulos, descripciones y errores se muestran con interpolación de texto
  en `web/src/app/dashboard/dashboard.component.html:16-23`; no se observa
  `[innerHTML]` en este template.
- La UI no invalida localmente la sesión ni redirige por sí sola cuando recibe
  un error de token; el comportamiento del backend no se verificó.

## Contratos de auth y api

Esta creación documenta solo el consumidor `web/`, conforme al alcance de la
tarea. Las secciones de contrato de los componentes `auth` y `api` quedan
pendientes de documentar por sus responsables; no se inspeccionó su código ni
se infirieron sus respuestas.
