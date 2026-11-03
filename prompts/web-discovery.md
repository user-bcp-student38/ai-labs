# Prompt — Discovery web

## AMBITO

Workspace: ai-labs, abierto en su raiz. Todas las rutas son relativas
a esa raiz. Lee el codigo solo de web/; escribe codigo, configuracion y
pruebas solo en web/. No leas ni modifiques codigo de otros componentes.
Lee README.md, RETO.md y docs/GOOD_PRACTICES.md. Lee
HALLAZGOS.md, PLAN.md y los demas documentos de docs/ solo si existen.
Escribe evidencias solo en HALLAZGOS.md, PLAN.md, prompts/,
docs/ENDPOINTS.md, docs/TESTING.md y docs/GOOD_PRACTICES.md de la raiz.
Cuando este paso pida un artefacto ausente, crealo. Si ya existe, conserva
las filas y secciones de otros componentes. No las sobrescribas.
Cada hallazgo lleva componente web e ID H-WEB-NN, por ejemplo H-WEB-01.
Cita codigo como web/ruta:linea. No renumeres IDs existentes.
Ejecuta Git desde ai-labs/; ejecuta pruebas con cwd web/.
Coordina dependencias externas sin ampliar AMBITO. No inventes aprobaciones
humanas ni resultados de pruebas. Si falta evidencia, registra el pendiente.

## CONTEXTO

Trabaja solo las filas y secciones de web en los documentos generales.
Conserva contenido previo de otros componentes y el historial de este componente.
Si docs/ENDPOINTS.md existe, distingue contrato observado, contrato acordado y
riesgos pendientes. Si docs/TESTING.md existe, relaciona cada ejecucion con
componente e IDs.
Aplicacion Angular de login y dashboard (login.component.ts, dashboard.component.ts,
utils/legacy-format.ts). Lee primero README.md (como correrla), RETO.md (tabla de
hallazgos esperados) y docs/ENDPOINTS.md si
existe. Si no existe, deriva el contrato observado del codigo y registralo en
HALLAZGOS.md. No crees docs/ENDPOINTS.md en este paso. NO EDITES CODIGO todavia.

## OBJETIVO

Mapear componentes, dependencias y el flujo de autenticacion completo (login ->
guardar token -> consultar tareas -> cerrar sesion), y crear o actualizar
HALLAZGOS.md con una matriz de seguimiento. La matriz incluye cada hallazgo de
RETO.md que exista realmente en el codigo, con evidencia archivo + linea.

## RESTRICCIONES

No modifiques ningun archivo de codigo. No des por cierto un hallazgo que no
puedas senalar en el codigo con archivo y linea. No inventes resultados de
herramientas: distingue hallazgos confirmados de hipotesis. Si docs/ENDPOINTS.md
existe, respeta su flujo funcional. No lo cambies durante Discovery.

## CRITERIOS DE ACEPTACION

Cada hallazgo del componente elegido en RETO.md queda en HALLAZGOS.md como una fila con componente web, id (H-WEB-01, H-WEB-02...), hallazgo,
tipo (Vulnerabilidad / Bug / Refactor / Sugerencia — el que mejor describa el
hallazgo), explicacion breve del riesgo (que puede salir mal y por que),
evidencia (archivo:linea), severidad (alta/media/baja) y estado inicial
"Pendiente". Los hallazgos quedan ordenados de mayor a menor severidad. Si un
hallazgo del componente elegido en RETO.md no aparece en el codigo, se marca como "No encontrado".

## FORMATO DE RESPUESTA

HALLAZGOS.md creado o actualizado con esta tabla:
id | componente | hallazgo | tipo | explicacion | evidencia (archivo:linea) | severidad | estado
Conserva las filas de otros componentes si el archivo ya existe.
Sin tocar codigo todavia. HALLAZGOS.md es el unico lugar donde se lleva el
seguimiento: 2-refactor.md lo va a ir actualizando hallazgo por hallazgo.
