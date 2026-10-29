## ADDED Requirements

### Requirement: El parámetro owner llega íntegro a la consulta de tareas (#31)
El dashboard de `web/` SHALL construir `GET /api/tasks` codificando el valor de `owner` como parámetro de query.

#### Scenario: Username con caracteres reservados
- **WHEN** el username contiene caracteres reservados de URL, por ejemplo `&`, `#` o `+`
- **THEN** el cliente HTTP recibe el parámetro `owner` con el valor exacto del username

#### Scenario: Username sin caracteres reservados
- **WHEN** el username contiene solo caracteres no reservados
- **THEN** la petición es `GET /api/tasks?owner=<usuario>` con la misma URL de hoy

### Requirement: El orden de tareas con igual prioridad sigue un criterio acordado (#34)
El dashboard de `web/` SHALL ordenar las tareas por `priority` y SHALL aplicar el criterio de empate registrado en el cambio.

#### Scenario: Prioridades distintas
- **WHEN** la respuesta incluye tareas con distintas `priority`
- **THEN** el dashboard las muestra ordenadas por `priority`, como hoy

#### Scenario: Prioridades iguales
- **WHEN** la respuesta incluye varias tareas con la misma `priority`
- **THEN** el comparador devuelve `0` para esas tareas y su orden relativo sigue el criterio acordado; una prueba lo verifica

#### Scenario: Criterio de empate sin decisión
- **WHEN** no existe un criterio de empate registrado
- **THEN** el cambio no modifica el comparador

### Requirement: Las tareas archivadas no se muestran
El dashboard de `web/` SHALL excluir las tareas con `status` = `ARCHIVED` antes de ordenarlas.

#### Scenario: Tarea archivada
- **WHEN** la respuesta incluye una tarea con `status` = `ARCHIVED`
- **THEN** esa tarea no aparece en el dashboard
