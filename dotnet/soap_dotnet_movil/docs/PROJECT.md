# <Nombre del proyecto> — cliente móvil (.NET MAUI, SOAP)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre(s) del/los servicio(s) SOAP>, URL completa de cada uno (`ConfiguracionServicio`: `Puerto`, `RutaServicio` o una ruta por servicio, `IpPc`), WSDL `<URL>?wsdl` (copia en `wsdl/`, de la que `dotnet-svcutil` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla servicio / operación / parámetros / respuesta>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado; flujo de navegación>
- **Arquitectura:** capas con MVVM; `vista → controlador (ViewModel) → servicios → ws`; una `PaginaPrincipal` aloja la pantalla actual (sin `Shell`). Los tipos generados no salen de `ws`.

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración del transporte con la URL por plataforma (`ConfiguracionServicio`), traducción de fallos de red (`TraductorRed`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), molde de los controladores (`ControladorBase`, `Comando`), página y componentes (`PaginaPrincipal`, `CampoTexto`, `ZonaEstado`), estilos y textos genéricos (`Textos`). Compila y pasa las pruebas de la plantilla; no tiene login ni pantallas.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Nombre de la aplicación (`ApplicationTitle` y `Textos.NombreAplicacion`) e identificador (`ApplicationId`)
- [ ] Servidor configurado (`ConfiguracionServicio`: `Puerto`, ruta de cada servicio; `IpPc` para teléfono físico)
- [ ] Copias del WSDL y de los esquemas (`xsd`) de **cada servicio** guardadas en `wsdl/`, con los `schemaLocation` apuntando a los archivos locales
- [ ] Proxies generados con `dotnet-svcutil` desde esas copias, **uno por servicio y cada uno en su espacio de nombres** (`ec.edu.monster.ws.generado.<servicio>`; paquetes del `.csproj` limpiados a `Http` y `Primitives`)
- [ ] **Prueba de humo del proxy en Android: Debug y Release**
- [ ] Interfaz propia `IXCliente`, envoltorio `XClienteWS`, DTO propios y servicios (`: ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: sesión definida en `modelo`, manejo de la sesión no válida en los controladores (`Manejar`) y login verificado contra el servidor real
- [ ] Controladores, vistas y `Pantalla` del dominio construidos y conectados en `App.Componer`
- [ ] Pruebas del dominio (servicios con doble del cliente, controladores)
- [ ] Imagen decorativa sustituida (si aplica)
- [ ] Servidor apagado / tiempo agotado probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión
- [ ] Flujo completo repetido en **Release**
- [ ] Tildes y «ñ» en pantalla; teclado numérico (signo menos y coma)
- [ ] Probado en teléfono real (`IpPc`, firewall, red Privada)
- [ ] (Si aplica) HTTPS con un certificado válido y de confianza en el dispositivo
- [ ] (Si aplica) Probado con IIS Express (binding comodín, Visual Studio como administrador, firewall)

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, URL de desarrollo, emulador, particularidades): <completar>.

**Regenerar el proxy** (`ws/generado/<servicio>/Reference.cs`): <comando exacto de `dotnet-svcutil` usado y de dónde sale el WSDL; ruta corta de trabajo>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
