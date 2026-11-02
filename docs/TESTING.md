# Testing — Lab 01

Evidencia real de las ejecuciones de esta sesión (Día 1, componente `auth`,
con extensión coordinada al punto de autorización de `api`). Ninguna
cobertura ni resultado fue inventado; ver comandos y conteos de pruebas
abajo.

## auth — `mvn clean verify` (JDK 21, Maven 3.10)

Comando:

```bash
cd auth
mvn clean verify
```

Resultado: `BUILD SUCCESS`, 9 pruebas, 0 fallos. Reporte de cobertura:
`auth/target/site/jacoco/index.html` → **92% instrucciones / 75% ramas**
(supera la meta del 85% de cobertura del código modificado; casi todas las
clases del paquete son nuevas o reescritas hoy).

| Caso | Tipo | Prueba | Resultado |
|---|---|---|---|
| Login válido (`student`/`Workshop123!`) | Válido | `logsInWithWorkshopUser` | 200, token y `user` presentes |
| `username` ausente | Borde | `rejectsMissingUsername` | 400 |
| `password` ausente | Borde | `rejectsMissingPassword` | 400 |
| Password incorrecta | Error | `rejectsWrongPassword` | 401 |
| Inyección SQL en `username` (`student' -- `) | Error / seguridad | `rejectsSqlInjectionInUsername` | **Antes del fix: fallaba (login exitoso sin password correcta, evidencia de H-AUTH-04). Después del fix: 401** |

Cobertura por clase (`auth/target/site/jacoco/com.bcp.workshop.auth/index.html`):
`TokenService` 100%, `UserRecord`/`UserSummaryDto`/`LoginResponseDto`/excepciones
100%, `AuthService` 94%, `UserRepository` 92%, `AuthResource` 72% (rama 500
genérica sin ejercitar: requiere forzar un error no controlado, fuera de
alcance de hoy).

## api — `pytest --cov=app --cov-fail-under=85` (venv `api/.venv`, Python 3.13)

Comando:

```bash
cd api
python tools/lab.py setup api   # primera vez
.venv/Scripts/python.exe -m pytest --cov=app --cov-report=term-missing --cov-fail-under=85
```

Resultado: 6 pruebas, 0 fallos. Cobertura total de `app/main.py`:
**92.52%** (gate de 85% de toda la app, ya existente en `tools/lab.py`,
superado).

| Caso | Tipo | Prueba | Resultado |
|---|---|---|---|
| Token válido lista tareas sembradas | Válido | `test_lists_seeded_tasks` | 200 |
| Crear tarea y ver resumen | Válido | `test_creates_task_and_summary` | 201 / 200 |
| Subir archivo de requerimiento | Válido | `test_uploads_requirement` | 201 |
| Token con formato inválido | Error | `test_rejects_invalid_token` | 401 |
| Token JWT expirado | Borde / seguridad | `test_rejects_expired_token` (nueva, H-AUTH-05/06) | 401 |
| Token JWT con firma alterada | Error / seguridad | `test_rejects_token_with_wrong_signature` (nueva, H-AUTH-05/06) | 401 |

## Cómo se midió "código modificado"

Para `auth`, casi todas las clases de `com.bcp.workshop.auth` fueron creadas o
reescritas en esta sesión (`AuthResource`, `AuthService`, `UserRepository`,
`TokenService`, DTOs y excepciones), por lo que la cobertura de paquete (92%)
es representativa del diff. Para `api`, el cambio se limitó a
`legacy_identity` dentro de `app/main.py`; esa función queda cubierta por las
6 pruebas (incluidas las 2 nuevas), y el resto del archivo (no tocado hoy) ya
tenía cobertura previa cubierta por los tests existentes.

## Pendiente para una próxima sesión

- Subir la cobertura de rama de `AuthResource` (rama 500 no ejercitada).
- Discovery propio de `api` y `web` si se continúa con esos componentes.
