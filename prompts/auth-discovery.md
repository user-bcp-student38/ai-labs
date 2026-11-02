# Prompt — Discovery auth

## Prompt adaptado

> Analiza el flujo completo de `POST /auth/login` en el componente `auth`
> (Quarkus/Java) sin modificar código. Recorre `AuthResource`,
> `LegacyDatabase`, `LegacyTokenUtil`, `LoginRequest`, `UserRecord` y
> `pom.xml`/`application.properties`. Para cada candidato de `RETO.md`
> (SCA, datos en texto plano, SQL, token, configuración, clean code,
> errores), confirma si existe evidencia real en el código actual y
> documenta el hallazgo en `HALLAZGOS.md` con ID `H-AUTH-NN`, archivo y
> línea. No propongas ni apliques correcciones en este paso.

## Alcance

- Componente: `auth` únicamente. No se lee ni modifica `api/` ni `web/`.
- Solo lectura: discovery, sin cambios de código ni de dependencias.
- Fuente de candidatos: tabla "Hallazgos sembrados" de `RETO.md` (sección
  `auth`).
- Contrato a preservar: `POST /auth/login` (ver `AuthResourceTest.java`).

## Criterios de aceptación

- Cada fila de la tabla de `RETO.md` queda como `Confirmado` (con evidencia
  archivo:línea) o `No encontrado` en `HALLAZGOS.md`.
- La evidencia de SCA cita una fuente verificable (CVE público o aviso de
  Dependabot visible en el repositorio), no una suposición.
- No se incluyen payloads de explotación, solo referencia a la ubicación del
  código y al riesgo.

## Justificación

El contrato para agentes de `auth` (`AGENTS.md`) exige analizar el flujo
completo antes de tocar autenticación y crear pruebas de caracterización
antes de refactorizar persistencia o token. Este prompt cubre esa etapa de
análisis previa, dejando evidencia trazable para que una persona apruebe el
`PLAN.md` antes de implementar cualquier cambio.
