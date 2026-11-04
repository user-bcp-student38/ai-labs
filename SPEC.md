| Pieza | Categoría | Motivo |
|---|---|---|
| 1 | **Validate** | Es una prueba del comportamiento esperado. |
| 2 | **Plan** | Define una decisión de diseño sobre dónde guardar el bloqueo. |
| 3 | **Specify** | Describe una necesidad: avisar al cliente al bloquear o desbloquear. |
| 4 | **Specify** | Expresa una capacidad que debe tener el cliente. |
| 5 | **Validate** | Es una prueba de seguridad relacionada con el desbloqueo. |
| 6 | **Tasks** | Es trabajo concreto de implementación en la app. |
| 7 | **Plan** | Identifica un riesgo y define cómo mitigarlo. |
| 8 | **Specify** | Describe la regla de negocio; no formula una prueba. |
| 9 | **Tasks** | Indica una acción concreta de implementación. |
| 10 | **Specify** | Define una restricción de seguridad que el sistema debe cumplir. |
| 11 | **Validate** | Es una prueba de que el aviso llega después del bloqueo. |
| 12 | **Specify** | Define un requisito para desbloquear la tarjeta. |

| Pieza | Categoría | Motivo |
|---|---|---|

**Pieza 1.** | Prueba que una compra con tarjeta bloqueada es rechazada. | Validate |
**Pieza 2.** Decisión: el bloqueo se guarda en un solo lugar, el sistema de tarjetas.
 Plan
**Pieza 3.** Avisar al cliente cuando bloquea o desbloquea.
 Tasks
**Pieza 4.** El cliente puede bloquear su tarjeta desde la app.
 Specify
**Pieza 5.** Prueba que desbloquear sin confirmar identidad no es posible.
 Validate
**Pieza 6.** Crear la opción de bloqueo en la app.
 Tasks
**Pieza 7.** Riesgo: el bloqueo falla a medias. Se mitiga dejando la tarjeta como estaba.
 Plan
**Pieza 8.** Si la tarjeta está bloqueada, el sistema rechaza las compras.
 Specify
**Pieza 9.** Hacer que el sistema rechace las compras de una tarjeta bloqueada.
 Tasks
**Pieza 10.** Decisión: el número completo de la tarjeta nunca aparece en los registros.
 Plan
**Pieza 11.** Prueba que el aviso llega al cliente tras bloquear.
 Validate
**Pieza 12.** Para desbloquear, el cliente debe confirmar su identidad.
Specify