# <Nombre del proyecto> — cliente de consola (.NET, SOAP)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre(s) del/los servicio(s) SOAP>, URL completa de cada uno `<UrlPorDefecto>`, WSDL `<URL>?wsdl` (copia en `wsdl/`, de la que `dotnet-svcutil` genera el proxy) (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla servicio / operación / parámetros / respuesta>
- **Flujo, menús y textos de la consola** (los comparte con los otros clientes del proyecto): <opciones de cada menú, mensajes, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración del transporte (`ConfiguracionServicio`), traducción de fallos de red (`TraductorRed`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`) y entrada/salida (`VistaBase`, `Textos`). Compila y pasa las pruebas de la plantilla; no tiene login ni menús.

- [ ] Servidor en marcha y copias del WSDL y de los esquemas (`xsd`) de **cada servicio** guardadas en `wsdl/`, con los `schemaLocation` apuntando a los archivos locales
- [ ] Proxies generados con `dotnet-svcutil` desde esas copias, **uno por servicio y cada uno en su espacio de nombres** (`ec.edu.monster.ws.generado.<servicio>`); URL completa de cada servicio en `ConfiguracionServicio`
- [ ] Interfaz propia `IXCliente`, envoltorio `XClienteWS`, DTO propios y servicios (`: ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: sesión definida en `modelo` y login verificado contra el servidor real
- [ ] Servicios, vistas y controladores del dominio construidos y conectados en `Aplicacion`
- [ ] Servidor apagado / tiempo agotado probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión
- [ ] Tildes, «ñ», entrada vacía y opción inválida probadas en una terminal real
- [ ] Fin de la entrada (Ctrl+Z) probado: termina sin trazas ni mensajes
- [ ] Paridad con el cliente de consola de referencia (misma entrada, `diff` de la salida), si existe uno

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| Herramienta del proxy | `dotnet-svcutil` desde copias locales del WSDL, un proxy por servicio | `CONVENCION_SOAP.md` §4 (a) |
| URL del servicio | No se usa la URL de los `Reference.cs`: `ConfiguracionServicio` (URL completa por servicio, sobrescribible con `WS_URL_<SERVICIO>`) | R9 y R10 |
| Un canal por llamada | `TraductorRed.LlamarAsync` crea el cliente generado, ejecuta y cierra; si falla, aborta | Un canal de WCF que falla queda inservible |
| Fallo al cerrar el canal | Si la operación ya terminó bien, se descarta (`Abort`) y se devuelve la respuesta | Una operación ya realizada no puede mostrarse como «servidor no disponible» |
| Sufijo `Async` | Los métodos de clientes, servicios y controladores que llaman a la red lo llevan | Convención de .NET |
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
