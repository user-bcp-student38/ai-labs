## ADDED Requirements

### Requirement: El almacenamiento del token no cambia sin contrato acordado (#24)
El frontend `web/` SHALL mantener `workshop_token` en `localStorage` hasta que el contrato de almacenamiento y transporte del token esté acordado con auth y api y registrado en el cambio.

#### Scenario: Sin contrato acordado
- **WHEN** no existe un contrato registrado con auth y api
- **THEN** el login guarda `workshop_token` y `workshop_user` como hoy y el dashboard lee el token para la cabecera Bearer

#### Scenario: Con contrato acordado
- **WHEN** el contrato registrado define otro mecanismo de sesión
- **THEN** el token no queda en `localStorage` después del login, las tareas se cargan con el mecanismo acordado y el contrato de `POST /auth/login` no cambia

### Requirement: Una sesión malformada se trata como ausente (#30)
El dashboard de `web/` SHALL tratar un valor de `workshop_user` que no sea JSON válido como sesión ausente, sin provocar una excepción no controlada al inicializarse.

#### Scenario: Usuario malformado
- **WHEN** `workshop_user` contiene un valor que no es JSON válido y el dashboard se inicializa
- **THEN** `ngOnInit` no lanza excepción no controlada, la UI navega a `/login` y no llama a `GET /api/tasks`

#### Scenario: Usuario válido
- **WHEN** `workshop_user` contiene JSON válido y existe `workshop_token`
- **THEN** el dashboard consulta `GET /api/tasks` como antes

### Requirement: Token inválido o expirado tiene comportamiento acordado (#33)
El dashboard de `web/` SHALL conservar el token ante una respuesta de error de carga de tareas hasta que se registre un criterio acordado para token inválido o expirado frente a un error no relacionado con autenticación.

#### Scenario: Sin decisión registrada
- **WHEN** no existe un criterio acordado registrado en el cambio
- **THEN** la carga de tareas con error conserva `workshop_token` y muestra el error, como hoy

#### Scenario: Decisión registrada para token inválido
- **WHEN** el criterio acordado indica cierre de sesión y la respuesta de `GET /api/tasks` informa token inválido o expirado
- **THEN** la UI elimina `workshop_token` y `workshop_user`, navega a `/login` y tiene una prueba para ese caso

#### Scenario: Decisión registrada para error no relacionado
- **WHEN** la respuesta de error no corresponde a token inválido según el criterio acordado
- **THEN** la sesión se conserva y la UI muestra el error, con una prueba para ese caso
