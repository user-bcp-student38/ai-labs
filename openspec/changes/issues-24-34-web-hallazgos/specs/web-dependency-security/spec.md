## ADDED Requirements

### Requirement: Las dependencias directas se verifican contra advisories (#25)
El frontend `web/` SHALL ejecutar `npm audit` y registrar su resultado en el issue antes de cerrar el hallazgo H-WEB-02.

#### Scenario: Resultado registrado
- **WHEN** se ejecuta `npm audit` en `web/`
- **THEN** el resultado, con los advisories por severidad, queda registrado en el issue #25

#### Scenario: Advisory aplicable
- **WHEN** existe un advisory aplicable a `axios` o `lodash`
- **THEN** la dependencia se actualiza a una versión corregida o se registra una justificación aceptada por el equipo

#### Scenario: Actualización de dependencias
- **WHEN** se actualiza una dependencia directa
- **THEN** `python tools/lab.py test web` pasa y los contratos de login y tareas no cambian
