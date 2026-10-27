# <Nombre del proyecto> — cliente de escritorio (Java Swing)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre(s) del/los servicio(s) SOAP>, URL completa de cada uno `<URL_POR_DEFECTO>`, WSDL `<URL>?wsdl` (copia en `src/main/resources/wsdl`, de la que `wsimport` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla servicio / operación / parámetros / respuesta>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración del proxy (`ConfiguracionServicio`), traducción de fallos de red (`TraductorRed`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), ventana principal, estilos (`vista/estilo`), ejecución asíncrona (`EjecutorAsincrono`), `Textos`, una pantalla inicial de ejemplo (`InicioVista`) y una pantalla de acceso pasiva (`LoginVista`, sin controlador). Compila y pasa las pruebas de la plantilla; no tiene login conectado ni pantallas de dominio.

- [ ] Servidor configurado (`ConfiguracionServicio.URL_POR_DEFECTO`) y WSDL + `.xsd` del servidor ya actualizado copiados a `src/main/resources/wsdl`
- [ ] `artifactId` actualizado
- [ ] Proxy generado (ejecución `generar-proxy` del `pom.xml` descomentada, `mvn generate-sources`), interfaz propia `XCliente`, envoltorio `XClienteWS`, DTO propios y servicios (`extends ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Retirados los ejemplos de conformidad (`ConvencionClienteTest`, `src/test/resources/wsdl/WSEjemplo.*` y la ejecución `generar-proxy-ejemplo` del `pom.xml`): las pruebas de conformidad están fuera de alcance (`docs/CONVENCION_PRUEBAS.md` §8)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: controlador de `LoginVista` y sesión definida en `modelo` y login verificado contra el servidor real
- [ ] Servicios, vistas y controladores del dominio construidos y conectados en `Main`
- [ ] Servidor apagado / timeout probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión
- [ ] Ventana maximizada y a tamaño mínimo revisadas; tildes y «ñ» probadas
- [ ] Empaquetado y ejecución del jar probados

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
