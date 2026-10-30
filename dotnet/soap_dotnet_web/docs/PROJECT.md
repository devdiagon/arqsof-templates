# <Nombre del proyecto> — cliente web (.NET, ASP.NET Core MVC, SOAP)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`; el estilo, `docs/CLIENT_STYLES_DOTNET.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre(s) del/los servicio(s) SOAP>, URL completa de cada uno `<UrlPorDefecto>`, WSDL `<URL>?wsdl` (copia en `wsdl/`, de la que `dotnet-svcutil` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla servicio / operación / parámetros / respuesta>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Rutas de las pantallas:** <ruta, controlador y vista de cada una>
- **Origen del núcleo:** <cliente de consola del que se copió `dto`, `excepciones`, `modelo`, `servicios` y `ws`, y su versión o commit>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración del transporte (`ConfiguracionServicio`), traducción de fallos de red (`TraductorRed`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), composición (`Aplicacion`), base de los controladores (`ControladorBase`, con el resultado de una sola lectura), vistas con su layout en dos modos, 15 *tag helpers* `ui-*`, estilos (`wwwroot/css/estilos.css`), `app.js`, `Textos`, una pantalla inicial de ejemplo (`Inicio`) y la página de error. Compila y pasa las pruebas de la plantilla; no tiene pantallas de dominio.

- [ ] Servidor configurado (`ConfiguracionServicio.UrlPorDefecto`, o una constante por servicio) y WSDL + esquemas del servidor ya actualizado copiados a `wsdl/`
- [ ] Nombre del proyecto actualizado (`.csproj`, `.slnx`, `@addTagHelper` de `vista/_ViewImports.cshtml`) y `Textos.NombreAplicacion`
- [ ] Proxy generado (`ws/generado/<servicio>/Reference.cs`), interfaz propia `IXCliente`, envoltorio `XClienteWS`, DTO propios y servicios (`: ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Clientes y servicios registrados en `Aplicacion.Construir`
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Controladores, modelos y vistas del dominio construidos (receta del README §4)
- [ ] `docs/PANTALLAS.md` escrito (orden de los elementos, anchos, estados y decisiones de cada pantalla)
- [ ] Pruebas de los servicios y de los controladores; `PantallasWebPruebas` ampliada con las pantallas del dominio (clientes SOAP simulados)
- [ ] Imagen decorativa del proyecto (`wwwroot/img/fondo_app.png`) decidida
- [ ] Servidor apagado / tiempo agotado probado
- [ ] Ventana ancha, intermedia y estrecha revisadas (< 900 y < 600 px); tildes y «ñ» probadas
- [ ] Recorrido manual de la guía de verificación y de los criterios de aceptación contra el servidor real

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (puertos, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
