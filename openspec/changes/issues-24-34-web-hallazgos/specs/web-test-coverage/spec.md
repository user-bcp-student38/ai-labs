## ADDED Requirements

### Requirement: Los flujos de logout y arranque tienen pruebas (#28)
Las pruebas de `web/` SHALL cubrir el flujo de logout y el arranque de la aplicación usando datos de prueba, sin credenciales reales.

#### Scenario: Logout
- **WHEN** la persona usuaria cierra sesión
- **THEN** la prueba verifica que se eliminan `workshop_token` y `workshop_user` y que la UI navega a `/login`

#### Scenario: Arranque de la aplicación
- **WHEN** se ejecuta la prueba de arranque con datos de prueba
- **THEN** la aplicación se crea sin errores y la prueba no usa credenciales reales

### Requirement: Cobertura y pruebas del código modificado (#24 a #34)
El cambio SHALL alcanzar una cobertura de 85% o más en el código modificado de `web/` y `python tools/lab.py test web` SHALL pasar.

#### Scenario: Cobertura del código modificado
- **WHEN** se ejecutan las pruebas de `web/` con cobertura
- **THEN** el código modificado tiene una cobertura de 85% o más

#### Scenario: Pruebas del componente
- **WHEN** se ejecuta `python tools/lab.py test web`
- **THEN** las pruebas pasan, incluidos login, carga de tareas, logout y los escenarios nuevos de los issues
