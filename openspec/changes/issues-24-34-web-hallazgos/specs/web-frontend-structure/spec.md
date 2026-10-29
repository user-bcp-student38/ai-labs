## ADDED Requirements

### Requirement: Los contratos observados de web se preservan (#26)
El frontend `web/` SHALL mantener sin cambios el método, la URL, el body, las cabeceras y el timeout de `POST /auth/login` y de `GET /api/tasks` mientras se separan HTTP, sesión y navegación de los componentes.

#### Scenario: Login envía el cuerpo esperado
- **WHEN** la persona usuaria completa usuario y contraseña y pulsa Ingresar
- **THEN** la petición es `POST /auth/login` con `{username, password, client: "angular-workshop"}` y timeout de 5000 ms

#### Scenario: Consulta autenticada de tareas
- **WHEN** existe `workshop_token` y el dashboard se inicializa
- **THEN** la petición es `GET /api/tasks?owner=<usuario>` con `Authorization: Bearer <token>`

#### Scenario: Componentes delegan HTTP, sesión y navegación
- **WHEN** se revisa el código modificado
- **THEN** las llamadas HTTP y la gestión de sesión están en servicios dedicados y los componentes solo presentan datos y delegan la navegación, según el criterio de #26

### Requirement: El código modificado no usa any ni validaciones anidadas (#27)
El código de `web/` modificado SHALL no usar `any` en requests, responses ni estado de tareas, y la validación de login SHALL usar guard clauses o condiciones combinadas.

#### Scenario: Tipos explícitos
- **WHEN** se revisa el código modificado de login y dashboard
- **THEN** no aparece `any` en requests, responses ni estado de tareas

#### Scenario: Validación de login con campos vacíos
- **WHEN** usuario o contraseña están vacíos
- **THEN** la UI muestra `Ingrese un usuario.` o `Ingrese una contraseña.` y no envía la petición

### Requirement: El cambio no modifica auth ni api
El cambio SHALL limitar sus modificaciones a `web/` y SHALL NOT alterar los contratos de `auth/` ni de `api/`.

#### Scenario: Revisión del diff
- **WHEN** se revisa el diff del cambio
- **THEN** no aparecen cambios en `auth/` ni en `api/`
