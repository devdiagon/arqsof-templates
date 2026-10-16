# <Nombre del proyecto> — cliente de consola (Java)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base `<BASE_POR_DEFECTO>` (contexto + prefijo de la API), ruta de login `<ruta>`
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla método / ruta / cuerpo JSON / respuesta / ¿requiere token?>
- **Flujo, menús y textos de la consola** (los comparte con los otros clientes del proyecto): <opciones de cada menú, mensajes, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio: transporte (`ClienteApi`), tipos comunes y entrada/salida (`VistaBase`). Compila y pasa las pruebas de la plantilla; no tiene login ni menús.

- [ ] Servidor configurado (`ClienteApiHttp.BASE_POR_DEFECTO`)
- [ ] `artifactId` actualizado
- [ ] `ServicioBase` y DTO del dominio (una fila por operación en §1)
- [ ] Login verificado contra el servidor real
- [ ] Servicios, vistas y controladores del dominio construidos y conectados en `Main`
- [ ] Servidor apagado / timeout probado
- [ ] Token vencido (`401`) devuelve al login con el mensaje
- [ ] Tildes, «ñ», entrada vacía y opción inválida probadas en una terminal real
- [ ] Ejecución desde terminal (`mvn compile exec:java`) y jar `-shaded` (`java -jar`) probados

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
