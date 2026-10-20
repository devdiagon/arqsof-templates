# <Nombre del proyecto> — cliente de consola (Java)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<URL_POR_DEFECTO>`, WSDL `<URL>?wsdl` (copia en `src/main/resources/wsdl`, de la que `wsimport` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo, menús y textos de la consola** (los comparte con los otros clientes del proyecto): <opciones de cada menú, mensajes, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio: configuración del proxy generado (`ClienteWS`), tipos comunes y entrada/salida (`VistaBase`). Compila y pasa las pruebas de la plantilla; no tiene login ni menús.

- [ ] Servidor configurado (`ClienteWS.URL_POR_DEFECTO`) y WSDL + `.xsd` del servidor ya actualizado copiados a `src/main/resources/wsdl`
- [ ] `artifactId` actualizado
- [ ] Proxy generado (ejecución `generar-proxy` del `pom.xml` descomentada, `mvn generate-sources`), interfaz propia `ClienteXxx`, envoltorio `ClienteWSXxx` y DTO propios de las operaciones del dominio (una fila por operación en §1)
- [ ] Pruebas de conformidad adaptadas al contrato real (modelo: `ConvencionClienteTest`) y retirados los ejemplos (`src/test/resources/wsdl/WSEjemplo.*` y la ejecución `generar-proxy-ejemplo` del `pom.xml`) si ya no sirven
- [ ] Login verificado contra el servidor real
- [ ] Servicios, vistas y controladores del dominio construidos y conectados en `Main`
- [ ] Servidor apagado / timeout probado
- [ ] Token vencido (`AUTH-003`) devuelve al login con el mensaje
- [ ] Tildes, «ñ», entrada vacía y opción inválida probadas en una terminal real
- [ ] Ejecución desde terminal (`mvn compile exec:java`) probada

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
