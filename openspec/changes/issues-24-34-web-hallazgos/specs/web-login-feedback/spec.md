## ADDED Requirements

### Requirement: Los errores de login no registran datos sensibles (#29)
El login de `web/` SHALL registrar en consola solo información no sensible cuando la petición falla, sin el objeto de error completo de Axios.

#### Scenario: Registro de error de login
- **WHEN** `POST /auth/login` falla con un error de Axios simulado en una prueba
- **THEN** el logger recibe solo información no sensible, por ejemplo el código de estado, y no recibe `username`, `password` ni el cuerpo de la solicitud

#### Scenario: Mensaje de error preservado
- **WHEN** el login falla
- **THEN** la UI muestra `No se pudo autenticar a <usuario>.` con el detalle definido en el contrato de login y no navega

### Requirement: Un fallo de navegación no se presenta como fallo de autenticación (#32)
El login de `web/` SHALL distinguir un rechazo de `router.navigate` tras un login exitoso del error de autenticación.

#### Scenario: Navegación rechazada tras login exitoso
- **WHEN** `POST /auth/login` responde 2xx con `token` truthy y `router.navigate` rechaza la promesa
- **THEN** la UI no muestra `No se pudo autenticar a <usuario>.`, muestra el mensaje de navegación acordado y `busy` vuelve a `false`

#### Scenario: Persistencia de sesión en fallo de navegación
- **WHEN** ocurre el rechazo de navegación descrito
- **THEN** la sesión queda en el estado definido por la decisión registrada en el cambio, y la prueba verifica ese estado

#### Scenario: Login exitoso sin error
- **WHEN** la navegación a `/dashboard` se completa
- **THEN** el comportamiento es el de la propuesta sin cambios: guarda sesión y navega a `/dashboard`
