# <Nombre del proyecto> — cliente web (.NET, ASP.NET Core MVC, REST)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`; el estilo, `docs/CLIENT_STYLES_DOTNET.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servidor REST>, URL base completa `<UrlBasePorDefecto>` (con «/» final), contrato en `docs/ENDPOINTS_REST.md` (tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla método de la interfaz / verbo y ruta / entrada / credenciales / respuesta>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Origen del núcleo:** <cliente de consola del que se copió `dto`, `excepciones`, `modelo`, `servicios` y `api`, y su versión o commit>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración de la URL (`ConfiguracionApi`), transporte (`ITransporte`, `TransporteHttp`), armado y lectura (`TraductorApi`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), composición de MVC (`Aplicacion`: antiforgery, codificación, ubicación de las vistas), `ControladorBase`, layout con sus dos modos, los 15 componentes `ui-*`, estilos (`wwwroot/css/estilos.css`), `Textos` y una pantalla inicial de ejemplo (`Inicio`). Compila y pasa las pruebas de la plantilla; no tiene login, sesión ni pantallas de dominio.

- [ ] `docs/ENDPOINTS_REST.md` del proyecto escrito o copiado del servidor, y `UrlBasePorDefecto` configurada
- [ ] Proyecto renombrado (`.csproj`, `.slnx`) y `@addTagHelper *, <Proyecto>` de `_ViewImports.cshtml` corregido; `Textos.NombreAplicacion`
- [ ] Interfaz propia `IXCliente`, cliente `XClienteApi`, DTO propios y servicios (`: ServicioBase`) de las operaciones del dominio (una fila por operación en §1), registrados en `Aplicacion`
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: mecanismo de autenticación definido en `ENDPOINTS_REST.md`, contexto por sesión de servidor (nunca credenciales compartidas) y login verificado contra el servidor real
- [ ] Modelos, controladores y vistas del dominio construidos
- [ ] `docs/PANTALLAS.md` escrito (orden de los elementos, anchos, estados y decisiones de cada pantalla)
- [ ] Pruebas de los servicios, de los clientes `…Api` y de las pantallas (`ServidorWeb`)
- [ ] Imagen decorativa del proyecto (`wwwroot/img/fondo_app.png`) decidida
- [ ] Servidor apagado / tiempo agotado probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión; dos empleados a la vez no se mezclan
- [ ] Ancho, intermedio y estrecho revisados en un navegador; tildes y «ñ» probadas; sin desplazamiento horizontal
- [ ] Recorrido manual de la guía de verificación y de los criterios de aceptación contra el servidor real

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| Transporte | `HttpClient` del SDK y `System.Text.Json`, sin paquetes extra; un solo `HttpClient` con tiempo límite de 10 s | `ARQUITECTURA_CLIENTE.md` §4.5 |
| Lectura de la envoltura | Siempre, también con HTTP de error; nunca `EnsureSuccessStatusCode` | R7: el `mensaje` del servidor va en el cuerpo |
| Mapeo JSON ↔ DTO | Opciones en `TraductorApi` (camelCase, sin distinguir mayúsculas, `Populate`); DTO sin anotaciones | `ARQUITECTURA_CLIENTE.md` §4.5 |
| Credenciales | Parámetros de la interfaz del cliente; `api` las coloca donde el contrato diga, por petición | R8 |
| Quién llama a la API | El controlador (el servidor web), nunca el navegador; sin CORS | `ENDPOINTS_REST.md` |
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
