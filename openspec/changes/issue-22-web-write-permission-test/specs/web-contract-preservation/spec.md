## ADDED Requirements

### Requirement: Login request contract is preserved
El frontend `web/` SHALL enviar `POST /auth/login` con `username`, `password` y `client` = `angular-workshop`, con timeout de 5000 ms, sin cabeceras explícitas ni parámetros de query.

#### Scenario: Login envía el cuerpo esperado
- **WHEN** la persona usuaria completa usuario y contraseña y pulsa Ingresar
- **THEN** la petición es `POST /auth/login` con el cuerpo `{username, password, client: "angular-workshop"}` y timeout de 5000 ms

### Requirement: Login success stores session data and navigates
El frontend `web/` SHALL considerar exitosa una respuesta HTTP 2xx cuyo `token` sea truthy, guardar `workshop_token` y `workshop_user` y navegar a `/dashboard`.

#### Scenario: Login exitoso
- **WHEN** el backend responde 2xx con `token` truthy
- **THEN** `localStorage` contiene `workshop_token` y `workshop_user`, y la navegación va a `/dashboard`

#### Scenario: Respuesta 2xx sin token
- **WHEN** el backend responde 2xx sin `token` truthy
- **THEN** la UI muestra `El backend respondió sin token.`, no guarda sesión y no navega

### Requirement: Login validation and error messages are preserved
El frontend `web/` SHALL mostrar los mensajes de validación y de error observados sin enviar la petición cuando los campos locales están vacíos.

#### Scenario: Usuario vacío
- **WHEN** el campo usuario está vacío y se pulsa Ingresar
- **THEN** la UI muestra `Ingrese un usuario.` y no envía `POST /auth/login`

#### Scenario: Contraseña vacía
- **WHEN** el campo contraseña está vacío y se pulsa Ingresar
- **THEN** la UI muestra `Ingrese una contraseña.` y no envía `POST /auth/login`

#### Scenario: Error del backend
- **WHEN** Axios rechaza la petición de login
- **THEN** la UI muestra el título `No se pudo autenticar a <usuario>.` con el detalle `error.response.data.message`, o `error.message`, o `Error desconocido`, en ese orden, y no navega

### Requirement: Dashboard task request contract is preserved
El dashboard de `web/` SHALL consultar `GET /api/tasks` con `owner` igual a `user.username` (o cadena vacía) y cabecera `Authorization: Bearer <workshop_token>`.

#### Scenario: Consulta autenticada de tareas
- **WHEN** existe `workshop_token` en `localStorage` y el dashboard se inicializa
- **THEN** la petición es `GET /api/tasks?owner=<usuario>` con `Authorization: Bearer <token>`

#### Scenario: Sin token
- **WHEN** no existe `workshop_token` en `localStorage`
- **THEN** la UI navega a `/login` y no llama a `/api/tasks`

### Requirement: Dashboard filtering and ordering are preserved
El dashboard de `web/` SHALL excluir las tareas con `status` = `ARCHIVED`, ordenar las restantes por `priority` y mostrar `title` y `description`.

#### Scenario: Tareas archivadas
- **WHEN** la respuesta incluye una tarea con `status` = `ARCHIVED`
- **THEN** esa tarea no aparece en el dashboard

#### Scenario: Orden por prioridad
- **WHEN** la respuesta incluye tareas con distintas `priority`
- **THEN** el dashboard las muestra ordenadas por `priority`

### Requirement: Change does not modify web runtime behavior
El cambio asociado al Issue #22 SHALL NOT modificar archivos bajo `web/src/` ni los contratos descritos en los requisitos anteriores.

#### Scenario: Revisión de archivos
- **WHEN** se revisa el diff del cambio
- **THEN** no aparecen cambios en `web/src/`, `auth/` ni `api/`

### Requirement: Write-permission check result is reported
La comprobación de escritura del Issue #22 SHALL reportar su resultado observado (éxito o error de permisos) con la respuesta del servidor GitHub oficial.

#### Scenario: Escritura permitida
- **WHEN** la escritura de prueba contra `user-bcp-student38/ai-labs#22` responde con éxito
- **THEN** el resultado reporta el éxito y la respuesta recibida

#### Scenario: Escritura denegada
- **WHEN** la escritura de prueba responde con error de permisos
- **THEN** el resultado reporta el error de permisos sin ocultarlo ni reintentar con otra identidad
