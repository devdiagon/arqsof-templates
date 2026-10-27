# <Nombre del proyecto> — cliente web (Jakarta EE, JSP)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre(s) del/los servicio(s) SOAP>, URL completa de cada uno `<URL_POR_DEFECTO>`, WSDL `<URL>?wsdl` (copia en `src/main/resources/wsdl`, de la que `wsimport` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Despliegue del cliente:** Payara 7, context root `<context-root>`, URL `http://localhost:8080/<context-root>/`
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla servicio / operación / parámetros / respuesta>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Pantallas y rutas:** <tabla ruta / controlador / JSP / ¿protegida?>
- **Fuente canónica del núcleo y versión copiada:** <proyecto y commit o etiqueta>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración del proxy (`ConfiguracionServicio`), traducción de fallos de red (`TraductorRed`), cliente perezoso compartido (`ClientePerezoso`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), `ContextoWeb`, `FiltroCodificacion`, componentes JSP y estilos, una pantalla inicial de ejemplo (`/inicio`) y una pantalla de acceso pasiva (`login.jsp`, sin controlador). Compila y pasa las pruebas de la plantilla; no tiene login conectado ni pantallas de dominio.

- [ ] Servidor SOAP configurado (`ConfiguracionServicio`) y WSDL + `.xsd` del servidor ya actualizado copiados a `src/main/resources/wsdl`
- [ ] Nombre de la aplicación, context root y `artifactId` actualizados
- [ ] Proxy generado (ejecución `generar-proxy` del `pom.xml` descomentada, `mvn generate-sources`), interfaz propia `XCliente`, envoltorio `XClienteWS`, DTO propios y servicios (`extends ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Retirados los ejemplos de conformidad (`ConvencionClienteTest`, `src/test/resources/wsdl/WSEjemplo.*` y la ejecución `generar-proxy-ejemplo` del `pom.xml`): las pruebas de conformidad están fuera de alcance (`docs/CONVENCION_PRUEBAS.md` §8)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: sesión por empleado en la sesión HTTP (nunca compartida), controlador de acceso y login verificado contra el servidor real
- [ ] Servicios, controladores y vistas del dominio construidos y sus rutas protegidas, si hay login
- [ ] Servidor SOAP apagado / timeout probado
- [ ] Ventana ancha y estrecha (< 900 px) revisadas en un navegador
- [ ] WAR desplegado y probado en Payara 7

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
