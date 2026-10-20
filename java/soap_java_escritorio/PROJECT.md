# <Nombre del proyecto> — cliente de escritorio (Java Swing)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<URL_POR_DEFECTO>`, WSDL `<URL>?wsdl` (copia en `src/main/resources/wsdl`, de la que `wsimport` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio: login funcional (`LoginVista`) y pantalla de ejemplo (`InicioVista`). Compila y pasa las pruebas de la plantilla.

- [ ] Servidor configurado (`ConfiguracionServicio.URL_POR_DEFECTO`), WSDL + `.xsd` del servidor ya actualizado copiados a `src/main/resources/wsdl` (reemplazan los de ejemplo) y `artifactId` actualizado
- [ ] Proxy regenerado (`mvn generate-sources`), `ClienteServicio`/`ClienteWS` y pruebas de conformidad (`ConvencionClienteTest`) adaptados a las operaciones reales
- [ ] Login verificado contra el servidor real
- [ ] Operaciones del dominio incorporadas (una fila por operación en §1)
- [ ] Pantallas del dominio construidas
- [ ] Servidor apagado / timeout probado
- [ ] Ventana maximizada y a tamaño mínimo revisadas
- [ ] Empaquetado y ejecución del jar probados

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6. Datos propios de este proyecto (usuario de prueba, puertos, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
