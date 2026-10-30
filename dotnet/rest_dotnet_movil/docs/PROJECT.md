# <Nombre del proyecto> — cliente móvil (.NET MAUI, REST)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`; el estilo, `docs/CLIENT_STYLES_DOTNET.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servidor REST>, tecnología (Java / .NET), URL base `http://<host>:<Puerto>/<Ruta>/` con el host por plataforma (emulador `10.0.2.2`, teléfono `IpPc`, Windows `localhost`), contrato en `docs/ENDPOINTS_REST.md` (cumple `CONVENCION_REST.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla método de la interfaz / verbo y ruta / entrada / credenciales / respuesta>
- **Pantallas y textos** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>; pantallas en `docs/PANTALLAS.md`
- **Origen del núcleo:** <cliente del que se copió `dto`, `excepciones`, `modelo`, `servicios`, `controlador` y `api`, y su versión o commit>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración de la URL por plataforma (`ConfiguracionApi`), transporte (`ITransporte`, `TransporteHttp`), armado y lectura (`TraductorApi`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), molde de controlador (`ControladorBase`, `Comando`), estilos (`Resources/Styles`), componentes (`CampoTexto`, `ZonaEstado`), página principal sin `Shell` y `Textos`. Compila para Android y Windows y pasa las pruebas de la plantilla (destino de Windows); no tiene login, sesión ni pantallas de dominio.

- [ ] `docs/ENDPOINTS_REST.md` del proyecto escrito o copiado del servidor
- [ ] `ConfiguracionApi`: `Puerto`, `Ruta` e `IpPc` configurados; `ApplicationTitle`, `ApplicationId` y `Textos.NombreAplicacion` actualizados
- [ ] Interfaz propia `IXCliente`, cliente `XClienteApi`, DTO propios y servicios (`: ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: mecanismo de autenticación definido en `ENDPOINTS_REST.md`, sesión en `modelo`, controlador y pantalla de acceso, y login verificado contra el servidor real
- [ ] Controladores y vistas del dominio construidos y conectados en `App.xaml.cs`
- [ ] `docs/PANTALLAS.md` escrito (orden de los elementos, estados y decisiones de cada pantalla), si el proyecto lo decide
- [ ] Pruebas de los servicios, de los clientes `…Api` y de los controladores
- [ ] Servidor accesible desde el emulador (binding comodín, `netsh`, firewall) y, si se usa, desde un teléfono físico
- [ ] Servidor apagado / tiempo agotado probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión
- [ ] Tildes y «ñ», teclado numérico y ocultar el teclado probados en un dispositivo
- [ ] Recorrido en **Release** (recorte de código) de una operación correcta y una con error
- [ ] Recorrido manual de la guía de verificación y de los criterios de aceptación contra el servidor real

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| Transporte | `HttpClient` del SDK y `System.Text.Json`, sin paquetes extra; un solo `HttpClient` con tiempo límite de 10 s | `ARQUITECTURA_CLIENTE.md` §4.5 |
| URL por plataforma | `ConfiguracionApi` propia del móvil: emulador `10.0.2.2`, teléfono `IpPc`, Windows `localhost`; `API_BASE_URL` solo rige en Windows | `ARQUITECTURA_CLIENTE.md` §4.9 |
| Lectura de la envoltura | Siempre, también con HTTP de error; nunca `EnsureSuccessStatusCode` | R7: el `mensaje` del servidor va en el cuerpo |
| Mapeo JSON ↔ DTO | Opciones en `TraductorApi` (camelCase, sin distinguir mayúsculas, `Populate`); DTO sin anotaciones | `ARQUITECTURA_CLIENTE.md` §4.5 |
| Credenciales | Parámetros de la interfaz del cliente; `api` las coloca donde el contrato diga, por petición | R8 |
| Plataformas | Solo Android y Windows | `ENTORNO_DOTNET.md` §2.1 |
| Pruebas | En el mismo proyecto, solo en el destino de Windows | Con el SDK de pruebas en Android la compilación falla (`XAPRAS7009`) |
| Sufijo `Async` | Los métodos de clientes, servicios y controladores que llaman a la red lo llevan | Convención de .NET |
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, `IpPc`, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
