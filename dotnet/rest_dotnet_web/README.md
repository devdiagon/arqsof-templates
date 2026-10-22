# Plantilla: cliente web .NET (ASP.NET Core MVC) para APIs REST

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos (`CONVENCION_REST.md`). Contiene lo que **todo** cliente necesita (acceso, sesión, estilos,
componentes de vista, transporte HTTP y pruebas) y deja claramente marcado lo que cambia con el **dominio**. Es la contraparte REST de `soap_dotnet_web` y la versión web de `rest_dotnet_consola` y `rest_dotnet_escritorio`.

- **Lenguaje:** C# 14, `net10.0`, `Nullable` e `ImplicitUsings` habilitados. **Plataforma:** ASP.NET Core 10 (Kestrel o IIS Express). **Interfaz:** controladores MVC + **vistas Razor** (`.cshtml`) escritas a código, sin librerías de UI (ni Bootstrap, ni jQuery, ni framework de JavaScript). **Arquitectura:** MVC por capas.
- **Transporte:** `HttpClient` + `System.Net.Http.Json` + `System.Text.Json`, todo incluido en el SDK: **ningún paquete** en producción. No hay proxy ni WSDL: los DTO se escriben a mano. Las capas `api/`, `dto/RespuestaBaseDto`, `excepciones/`, `modelo/Sesion` y `servicio/ServicioBase` son **las mismas** de `rest_dotnet_escritorio` y `rest_dotnet_consola` (copiarlas entre plantillas, no reescribirlas).
- **El navegador no habla con la API:** quien la llama es el controlador (el servidor del cliente web), nunca JavaScript. Por eso la API no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md`, en su **presentación clara** y con la **pantalla a la izquierda y la imagen a la derecha** (§5). Se aplica con **componentes reutilizables** (*tag helpers* `ui-*`) y un único `wwwroot/css/estilos.css`.
- **Convención REST:** sigue `CONVENCION_REST.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla; cambiar de servidor es cambiar solo la URL base.
- **Contraparte de escritorio:** `rest_dotnet_escritorio` comparte con esta plantilla las capas `modelo`, `excepciones`, `api`, `dto` (base), `servicio` y los estilos (mismos nombres, mismo código). Cambian `controlador` y `vista`.
- **Verificada:**
  - `dotnet test` → **86 pruebas xUnit en verde**, sin servidor ni interacción: URL base, transporte con un `HttpMessageHandler` falso, traducción de fallos de red, `ServicioBase` (códigos HTTP, token, cuerpos que no son JSON), controlador de acceso (con un autenticador falso), **cada componente `ui-*`** (HTML, clases y escape de todo texto dinámico), la **composición de `Program.cs`** (`ComposicionPruebas`: transporte tipado y `Sesion` por petición) y un **recorrido en memoria de la aplicación completa** (`PantallasWebPruebas`: rutas, convención de controladores, vistas Razor, sesión, cookies, token anti-falsificación, acceso, pantalla protegida y cierre de sesión).
  - La aplicación se **ejecutó** (`dotnet run`) y se comprobó con `curl`: `/` sin sesión redirige a `/Acceso/Acceder`, la pantalla de acceso carga con título, `<body class="tema-claro">`, la zona de imagen y los componentes resueltos, el acceso responde el mensaje del marcador «Falta conectar el servicio del dominio.» y se sirven `estilos.css`, `app.js` y la imagen.
  - La receta de §4 es la que se siguió y verificó en el cliente web del conversor de unidades (acceso, categorías, unidades y conversión contra un servidor .NET real en IIS Express, con sus errores de negocio y la sesión protegida).
  - **No se ha hecho:** ver la plantilla en un navegador (el aspecto no se comprobó), una llamada a un servidor real desde la plantilla sin tocar (no tiene DTO ni dominio), el vencimiento real del token, `dotnet publish`, ni la ejecución desde Visual Studio con el perfil `IIS Express`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**.
2. **Renombrar** `rest_dotnet_web.csproj` y `rest_dotnet_web.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. **Además, en `ec/edu/monster/vista/_ViewImports.cshtml` cambiar `@addTagHelper *, rest_dotnet_web` por el nuevo nombre del proyecto** (es el nombre del ensamblado): si no, los componentes `ui-*` salen como texto y las pantallas no se ven bien. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10; la primera vez restaura los paquetes de prueba (Internet).
4. **Configurar el servidor:** en `ec/edu/monster/api/ConfiguracionApi.cs`, `UrlBasePorDefecto` = **URL base de la API** (host, puerto y prefijo, **con «/» final**; p. ej. `http://localhost:57592/api/`).
5. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la pestaña y de la pantalla de acceso) y, si se quiere, **sustituir la imagen** `wwwroot/img/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6).
6. **Crear los DTO y el servicio del dominio** (§4) y conectarlos en `Program.cs`.
7. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
8. **Probar el servidor** (una petición con curl, §7) y ejecutar (F5 en Visual Studio o `dotnet run --launch-profile http`). Con la plantilla sin tocar abre `http://localhost:5092/`, redirige al acceso y este responde que falta conectar el dominio.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Acceso (compartido)** | `controlador/AccesoControlador`, `vista/acceso/Acceder.cshtml`, `modelo/AccesoModelo`, `servicio/IAutenticador` | No; el servicio del dominio implementa `IAutenticador`. Solo la pantalla de destino (`RedirectToAction("Index", "Inicio")`) |
| **Transporte HTTP/JSON** | `api/ClienteApi`, `api/IClienteApi`, `api/RespuestaHttp` (Bearer por petición, JSON UTF-8, fallos de red → `ServidorNoDisponible`) | No |
| **URL base** | `api/ConfiguracionApi` (`UrlBasePorDefecto`, variable `API_BASE_URL`, normalización con «/» final, timeout 10 s) | `UrlBasePorDefecto` siempre; el resto no |
| **Respuesta común** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`) | No; los DTO del dominio la extienden |
| **DTO del dominio** | — (`dto/SolicitudXxxDto`, `dto/RespuestaXxxDto`) | **Se crean** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`Codigo`, `EstadoHttp`, `ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`EnviarAsync<T>`, `ExigirToken`, decisión por código HTTP, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Base web** | `controlador/ControladorBase` (`VolverAlAcceso`), `ConvencionControlador`, `SesionWeb`, `PersistirSesionFiltro`, `ExigirSesionAttribute` | No |
| **Composición** | `Program.cs` (transporte tipado, sesión, rutas de vistas, filtros, codificación) | Se registra el servicio del dominio (marcado `DOMINIO`) |
| **Estilos y componentes** | `wwwroot/css/estilos.css`, `wwwroot/js/app.js`, `vista/componentes/Ui*TagHelper.cs`, `vista/compartido/_Layout.cshtml`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se escriben clases ni estilos en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` |
| **Marcador de acceso** | `servicio/AutenticadorPendiente` | **Se elimina** al registrar el servicio del dominio |
| **Pantalla de ejemplo (dominio)** | `controlador/InicioControlador`, `vista/inicio/Index.cshtml` | **Se reemplaza** por las pantallas del dominio (conservar la acción `Error`) |
| **Pruebas** | `pruebas/` (URL, transporte, `ServicioBase`, doble `ClienteApiFalso`, acceso, componentes, composición, recorrido de la aplicación) | Se conservan; se agregan las del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/`, `modelo/` |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec Program.cs`).

## 3. Estructura

```
Program.cs                         punto de composición: transporte, sesión, rutas de vistas, filtros y codificación
docs/CLIENT_STYLES_DOTNET.md       guía de estilos
CONVENCION_REST.md                 convención común de los servidores REST de la familia
wwwroot/                           css/estilos.css, js/app.js, img/fondo_app.png    lo único público (se sirve tal cual)
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion, AccesoModelo, ErrorModelo                entidades y modelos de vista: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                                código + mensaje + estado HTTP; ExigeLogin, ServidorNoDisponible
├── api/           ClienteApi, IClienteApi, RespuestaHttp, ConfiguracionApi     transporte HTTP: URL base, timeout, cabeceras, código + cuerpo
├── dto/           RespuestaBaseDto (+ los DTO del dominio)         cuerpos JSON del contrato, escritos a mano; no salen de servicio/
├── servicio/      ServicioBase, IAutenticador, AutenticadorPendiente (+ los servicios del dominio)    única capa que conoce api y dto
├── controlador/   ControladorBase, AccesoControlador, InicioControlador, ConvencionControlador,
│                  SesionWeb, PersistirSesionFiltro, ExigirSesionAttribute
├── vista/         Textos, _ViewImports, _ViewStart,
│   ├── acceso/        Acceder.cshtml                               una carpeta por controlador, una vista por acción
│   ├── inicio/        Index.cshtml
│   ├── compartido/    _Layout.cshtml, Error.cshtml
│   └── componentes/   UiEncabezado, UiCampo, UiSelector, UiMensaje, UiBoton, UiListaAcciones, UiFormulario, UiCerrarSesion  (tag helpers)
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo
```

Dependencias permitidas: `vista (Razor) ← controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**Los DTO no salen de `servicio/`**: el servicio los traduce a `modelo` (`record`) o a tipos simples. Los controladores **nunca importan `api` ni `dto`**: hablan con los servicios y entregan a la vista un modelo de `modelo/`. La vista (`.cshtml`) no tiene lógica de negocio ni llama al servidor.
Si el contrato cambia (rutas, campos, códigos) solo se tocan `servicio`, `api` y `dto`.

**Las vistas no viven en `Views/`** sino en `ec/edu/monster/vista/{controlador}/{vista}.cshtml` y `vista/compartido/` (`Program.cs` configura `RazorViewEngineOptions.ViewLocationFormats`), para respetar los directorios en minúscula de la familia.
**Los controladores se llaman `XxxControlador`** y la ruta usa `Xxx`: `ConvencionControlador` quita el sufijo y `[Controller]` en `ControladorBase` los hace descubribles.

**Una petición, de punta a punta:** navegador → `UseSession` → filtro global (token anti-falsificación en todo POST) → `[ExigirSesion]` (si la pantalla es protegida) → controlador (`*Controlador`) → servicio (por petición, con la `Sesion` del usuario) → `ClienteApi` (HTTP/JSON, `Authorization: Bearer`) → el controlador entrega un modelo → vista Razor → `PersistirSesionFiltro` guarda el token en la sesión antes de escribir la respuesta.
El estado del usuario (el token) vive en la **sesión de ASP.NET Core** (`ISession`), cargado en una `Sesion` *scoped*, **nunca** en un servicio *singleton*: lo compartirían todos los usuarios. Por la misma razón el token **no** va en `DefaultRequestHeaders` del `HttpClient` (que es compartido): `ClienteApi` lo pone en cada petición.

**Ciclo de vida en `Program.cs`:** `AddHttpClient<IClienteApi, ClienteApi>` (el `HttpClient` lo gestiona `IHttpClientFactory`, no se crea uno por petición; el cliente es *transient* y sin estado), `Sesion` y el servicio del dominio *scoped*.

## 4. Receta: agregar un endpoint y su pantalla

Ejemplo: `POST /auth/login` (público) y `GET /items` que devuelve una lista (protegido), con su pantalla. Se sigue **siempre el mismo orden**, de abajo hacia arriba.

1. **DTO** — en `dto/`, uno por cuerpo del contrato, con `[JsonPropertyName]` en cada propiedad (el contrato no depende de la política *camelCase*) y tipos que toleren `null`:
   ```csharp
   public class SolicitudLoginDto  { [JsonPropertyName("usuario")] public string? Usuario { get; set; }
                                     [JsonPropertyName("contrasenia")] public string? Contrasenia { get; set; } }
   public class RespuestaLoginDto : RespuestaBaseDto { [JsonPropertyName("token")] public string? Token { get; set; } }
   public class RespuestaListaDto : RespuestaBaseDto { [JsonPropertyName("elementos")] public List<string>? Elementos { get; set; } = []; }
   ```
2. **Servicio** — `servicio/ServicioXxx : ServicioBase, IAutenticador` (recibe `IClienteApi` y `Sesion`). `EnviarAsync<T>` exige el token, lo envía en la cabecera, decide por el código HTTP y lanza `ExcepcionServicio` en cualquier error. Es la **única** capa que ve los DTO:
   ```csharp
   public class ServicioXxx(IClienteApi cliente, Sesion sesion) : ServicioBase(cliente, sesion), IAutenticador
   {
       public async Task IniciarSesionAsync(string usuario, string contrasenia)        // IAutenticador
       {
           var r = await EnviarAsync<RespuestaLoginDto>(HttpMethod.Post, "auth/login",
               new SolicitudLoginDto { Usuario = usuario, Contrasenia = contrasenia }, requiereToken: false);
           Sesion.Token = !string.IsNullOrEmpty(r.Token) ? r.Token
               : throw new ExcepcionServicio("El servidor no entregó un token de sesión", "ERR-500", 200);
       }

       public async Task<IReadOnlyList<string>> ObtenerItemsAsync()
       {
           var r = await EnviarAsync<RespuestaListaDto>(HttpMethod.Get, "items");
           return r.Elementos ?? [];     // las listas pueden venir ausentes
       }
   }
   ```
   - Aquí van las reglas **del cliente** (campo vacío, formato, `NormalizarValor`: coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
   - Un valor de ruta variable se codifica con `Uri.EscapeDataString` (`$"items/{Uri.EscapeDataString(nombre)}"`).
3. **Modelo de vista y controlador** — `modelo/ItemsModelo` (solo propiedades) y `controlador/ItemsControlador(ServicioXxx servicio) : ControladorBase` con `[ExigirSesion]` (molde: `AccesoControlador` e `InicioControlador`):
   ```csharp
   [ExigirSesion]
   public class ItemsControlador(ServicioXxx servicio) : ControladorBase
   {
       public async Task<IActionResult> Index()
       {
           try { return View(new ItemsModelo { Items = await servicio.ObtenerItemsAsync() }); }
           catch (ExcepcionServicio ex) when (ex.ExigeLogin) { return VolverAlAcceso(ex); }   // token inválido/vencido: vuelve al acceso con el mensaje
           catch (ExcepcionServicio ex) { return View(new ItemsModelo { Mensaje = ex.Message }); }   // 400, 404, red…: solo el mensaje, nunca el código
           catch (Exception) { return View(new ItemsModelo { Mensaje = "Ocurrió un error inesperado. Intente de nuevo." }); }
       }
   }
   ```
   - Un `POST` valida lo trivial (campo vacío), llama al servicio y vuelve a mostrar **la misma vista** con el resultado o el error, **conservando lo capturado** (devolver los valores en el modelo). El token anti-falsificación se valida solo en todo POST.
   - Un fallo de red (`ServidorNoDisponible`) o un error de negocio (`400`, `404`) no cierra la aplicación ni la sesión: se muestra el mensaje y la pantalla sigue usable (con «Reintentar» si no hay datos). Si una pantalla necesita recargar un catálogo para volver a dibujarse y no puede, conservar las opciones elegidas por el usuario.
4. **Vista** — `vista/items/Index.cshtml` (carpeta con el nombre del controlador), **solo con componentes `ui-*`** y `@model`. Sin `class`, sin `style`, sin lógica; muestra lo que dejó el controlador. Moldes: `acceso/Acceder.cshtml` e `inicio/Index.cshtml`.
   ```html
   @model ItemsModelo
   @{ ViewData["Titulo"] = "Elementos"; }
   <ui-encabezado>Elementos</ui-encabezado>
   <ui-mensaje mensaje="@Model.Mensaje" />
   <ui-lista-acciones elementos="@Model.Items" controlador="Detalle" accion="Ver" parametro="clave" />
   <ui-cerrar-sesion />
   ```
   Un formulario es `<ui-formulario accion="Guardar" controlador="Items">…</ui-formulario>`; los enlaces a otras pantallas, `<ui-boton href="@Url.Action(...)">`.
5. **Composición** — en `Program.cs`, en lugar de `AutenticadorPendiente` (que se elimina):
   ```csharp
   builder.Services.AddScoped<ServicioXxx>();
   builder.Services.AddScoped<IAutenticador>(sp => sp.GetRequiredService<ServicioXxx>());
   ```
   El transporte (`AddHttpClient<IClienteApi, ClienteApi>`) ya está registrado. Cambiar la pantalla de destino del acceso si ya no es `Inicio`, y reemplazar `InicioControlador`/`inicio/Index.cshtml` por las pantallas del dominio (**conservar la acción `Error`**: la usa `UseExceptionHandler`).
6. **Pruebas** (en `ec/edu/monster/pruebas/`):
   - El servicio con `ClienteApiFalso` (`Responder` por ruta, `Siempre`, `Ok`, `Error`): `200`, `400`, `401` con `AUTH-002/003`, `404`, error de plataforma, cuerpo que no es JSON, servidor no disponible, coma enviada como punto, sin sesión no se llama a la API.
   - **Una prueba de recorrido por pantalla** en `PantallasWebPruebas`: reemplazar `IClienteApi` por el doble en `ConfigureServices` (`RemoveAll<IClienteApi>()` + `AddSingleton<IClienteApi>(doble)`) y comprobar el HTML (datos, mensajes sin código, escape, redirección al acceso por token vencido, formulario conservado ante error). **Es la que detecta una vista o un componente inválido**, que compila pero falla al abrir la pantalla.
   - Los componentes `ui-*` nuevos, en `ComponentesPruebas` (incluido el escape).

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP y luego por el código propio.** Nunca `EnsureSuccessStatusCode()`: descartaría el JSON de error. `ServicioBase` ya lo cumple.
- **El token** vive en `Sesion` (en la sesión de ASP.NET Core, en memoria del servidor web), no se guarda en disco, ni en la cookie (la cookie solo lleva el identificador de la sesión), ni en logs, ni en la página. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al acceso; no se cuenta el tiempo en el cliente. Va en `Authorization: Bearer` **por petición**, nunca en `DefaultRequestHeaders`.
  `[ExigirSesion]` solo comprueba que *haya* token; si el servidor ya lo venció, lo detecta el servicio al llamar.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual). El campo numérico de la vista es `type="text"` con `inputmode="decimal"`, no `type="number"`.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Escapar todo texto dinámico**: Razor y los componentes ya lo hacen; **no** usar `Html.Raw` con datos del servidor ni del usuario. La contraseña nunca se devuelve a la página.
- Todo `POST` va con token anti-falsificación (`<ui-formulario>` y `<ui-cerrar-sesion>` lo incluyen; es obligatorio por el filtro global). Cerrar sesión es un `POST`.
- **Nunca red en el hilo de la petición sin `async/await`.** Un fallo de red no cierra la aplicación.
- Cada `POST` que cambie algo debería terminar en redirección (Post/Redirect/Get) si el resultado no es la página de respuesta del mismo formulario; el acceso y el cierre de sesión ya lo hacen.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo usan los componentes** `ui-*`; no escriben clases, estilos ni colores. Componentes disponibles:

| Etiqueta | Para qué |
|---|---|
| `_Layout` | Estructura de toda página: cabecera HTML, columna de contenido (420, centrada) a la izquierda e imagen a la derecha. `ViewData["Titulo"]` es obligatorio |
| `<ui-formulario accion controlador>` | Formulario POST con token anti-falsificación; al enviarlo `app.js` deshabilita los controles y muestra el progreso |
| `<ui-encabezado>` | Título de pantalla (`aplicacion="true"` para el título de la aplicación en el acceso) |
| `<ui-campo id etiqueta>`, `<ui-selector id etiqueta opciones seleccionada>` | Etiqueta + campo de texto/contraseña, etiqueta + lista desplegable (`invalido="true"` marca el borde de error) |
| `<ui-mensaje mensaje>` | Mensaje o resultado (`tipo`: `error`, `exito`, `resultado`); no se dibuja si no hay texto |
| `<ui-boton>` | Botón primario (acción principal) o `tipo="secundario"` (salida o regreso); con `href` es un enlace; `completo` y `salida` ajustan ancho y separación |
| `<ui-lista-acciones elementos controlador accion parametro>` | Una columna de botones primarios generados con datos del servidor; cada uno enlaza a `controlador/accion?parametro=elemento` |
| `<ui-cerrar-sesion />` | Botón secundario «Cerrar sesión» (POST) separado 32 |

- **Tema claro** fijo (`<body class="tema-claro">` en `_Layout`); no hay selector. Los tokens son variables CSS en `estilos.css` (valores de `docs/CLIENT_STYLES_DOTNET.md` §2.3). Para la presentación oscura, agregar el bloque `.tema-oscuro` con los valores de §2.2, cambiar la clase del `<body>` y dar a `.zona-imagen` el fondo `#1A1426` de los bordes de la imagen. En la clara, paneles y campos llevan un borde de 1 px (`--borde`, `--campo-borde`) para distinguirlos del fondo.
- **Disposición:** ventana dividida: **la pantalla a la izquierda** (560, fija; columna de máximo 420, centrada) y **la imagen a la derecha** (el resto, completa y centrada con `contain`, sin deformar); bajo 900 px, columna única sin imagen y sin scroll horizontal. La imagen es del **layout**, no de cada vista. Es la misma disposición que `rest_dotnet_escritorio` (el documento de estilos la define con las zonas invertidas, §7.0).
- **Las imágenes** van en `wwwroot/img` y se referencian desde el CSS con ruta relativa (`../img/...`). La imagen de ejemplo es un PNG con transparencia: sobre el fondo claro no necesita un color de fondo propio.
- **Componente nuevo:** crear `vista/componentes/UiXxxTagHelper.cs` (con `AppendHtml`/`TagBuilder`, que **escapan** el texto) y su CSS en `estilos.css`, con tokens (`var(--…)`), nunca con colores sueltos. Agregar su prueba en `ComponentesPruebas` (incluido el escape) y probarlo en un navegador a 1100 px y a menos de 900 px.
- Una vista o un componente inválido **compila** y falla al abrir la pantalla (HTTP 500) o, si falta el `@addTagHelper`, se ve como etiqueta literal: probar cada pantalla nueva con `PantallasWebPruebas` y abrirla.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.UrlBasePorDefecto` (URL base con «/» final). Sin recompilar: variable de entorno `API_BASE_URL`
  (PowerShell: `$env:API_BASE_URL = "http://<ip>:57592/api/"; dotnet run --launch-profile http`). La barra final se agrega sola si falta.
- **HTTP y HTTPS:** el esquema de la URL base lo decide todo; no hay binding. **HTTPS exige un certificado de servidor válido y de confianza** (en desarrollo, `dotnet dev-certs https --trust`, y que el nombre al que se conecta, `localhost` o la IP, esté en el certificado).
  La validación del certificado **no** se desactiva en el cliente. Si falla, el cliente solo muestra «Servidor no disponible, intente más tarde» (la excepción original queda en `InnerException`).
- **Cambiar de servidor** (por ejemplo, de .NET a Java) es cambiar la URL base, siempre que ambos cumplan `CONVENCION_REST.md`.
- **Entorno de desarrollo de la familia:** IIS Express por HTTP (`http://localhost:57592/api/`); el perfil `http` (Kestrel) del servidor usa el puerto `5267`. Reglas de red y firewall: `IIS_CONFIG.md` del proyecto del servidor.
- **Puerto del cliente web:** `5092` (perfil `http` de `Properties/launchSettings.json`); debe ser **distinto** del del servidor (`5267` / `57592`). Cambiarlo ahí si hay otro proyecto abierto a la vez.
- **Visual Studio:** *Run* (F5) con el perfil `http`. Detener la ejecución antes de compilar desde la terminal (el `.dll` queda bloqueado).
- **Terminal:** `dotnet run --launch-profile http`, y abrir `http://localhost:5092/`.
- **Sesión:** la cookie `.Cliente.Sesion` (`HttpOnly`) dura 20 minutos de inactividad (`Program.cs`); es independiente del token del servidor (que vence a su ritmo, 5 minutos en esta familia, y se reconoce por el código `AUTH-003`). Si la aplicación se reinicia, las sesiones en memoria se pierden y el usuario vuelve al acceso.
- **Publicación:** `dotnet publish -c Release`. **Probarla** antes de darla por buena (las vistas se compilan dentro del ensamblado).

## 7. Comandos y verificación

```
dotnet test                              # pruebas locales (sin servidor ni interacción); detener la aplicación antes
dotnet run --launch-profile http         # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación (una ruta protegida sin token debe responder `401` con un JSON, lo que confirma que la API está arriba):
```
curl -i http://localhost:57592/api/<ruta-protegida>
curl -s -X POST http://localhost:57592/api/auth/login -H "Content-Type: application/json" -d '{"usuario":"…","contrasenia":"…"}'
```

Verificación con la aplicación en marcha (la vista se revisa a mano; los controladores y los componentes, con pruebas):
1. Sin sesión, abrir una ruta protegida (p. ej. `/`): debe redirigir al acceso.
2. Acceso con credenciales incorrectas: muestra el mensaje del servidor (sin código) y conserva el usuario; correcto: entra a la primera pantalla; «Cerrar sesión» vuelve al acceso y «Atrás» no muestra la pantalla protegida.
3. Error de negocio (`400`/`404`): se muestra el mensaje y la pantalla sigue usable, con lo capturado y la sesión abierta.
4. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» (≈ 10 s) y la aplicación sigue respondiendo.
5. Token vencido (esperar 5 minutos o reiniciar el servidor con la sesión abierta): vuelve al acceso con el mensaje.
6. Ventana ancha (≥ 900 px: imagen a la derecha) y estrecha (columna única, sin imagen, sin scroll horizontal); tildes y «ñ» en pantalla.

Un agente que no pueda abrir el navegador debe decir explícitamente qué verificó (pruebas, peticiones HTTP con `curl`, contra qué servidor) y que **no vio la aplicación en un navegador real** si es el caso.
Con `curl` en Windows (Git Bash), los textos con tildes deben enviarse codificados en UTF-8 (`--data-urlencode` o `%C3%B3`…); la consola puede alterarlos y producir falsos errores de «unidad no soportada» o «categoría no soportada».

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicio`. Es la convención común de la familia (`CONVENCION_REST.md`):

- HTTP + JSON (`application/json; charset=utf-8`), verbos `GET` (consultas) y `POST` (login y operaciones). **La URL base** se recibe completa por configuración; no se asume puerto ni prefijo.
- Cada respuesta, de éxito o de error, es un objeto con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los errores de la plataforma (ruta inexistente, método no permitido, `Content-Type` incorrecto, error interno) traen **solo** esos tres campos.
- **Código HTTP + código propio:** `200` éxito; `400` datos inválidos; `401` credenciales (`AUTH-001`) o token (`AUTH-002` inválido / `AUTH-003` vencido); `404` recurso inexistente; `405`/`415` error de programación del cliente; `500` error del servidor.
- El token viaja en `Authorization: Bearer <token>`, no como parámetro ni cuerpo. Existe un login público (`POST /auth/login`) que devuelve `token`.
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Todos los datos viajan como **texto**; los decimales con punto. Los campos que pueden faltar (`token`, listas) llegan `null` o ausentes en un error: no darlos por existentes.
- Si el servidor usa otro esquema de autenticación (cookies, OAuth/JWT con renovación), ampliar `ClienteApi`/`ServicioBase`.
- Para un servicio **SOAP** usar `soap_dotnet_web`, no esta (la capa `servicio` es la que cambia).

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| Las pantallas salen sin estilo de componentes: se ven `<ui-campo …>` o `asp-append-version` literales en el HTML | Falta o es incorrecto el `@addTagHelper` de `vista/_ViewImports.cshtml`: debe haber uno para `Microsoft.AspNetCore.Mvc.TagHelpers` y otro con **el nombre del ensamblado** (el del `.csproj` renombrado) |
| 404 en una pantalla recién creada, o «The view … was not found» | La vista no está en `vista/{controlador}/{Acción}.cshtml` (o `vista/compartido/`), o el controlador no termina en `Controlador` ni hereda de `ControladorBase` |
| Error CS0616 en un tag helper nuevo (`ViewContext` no es una clase de atributos) | Falta `using Microsoft.AspNetCore.Mvc.ViewFeatures;` |
| 400 al enviar un formulario | Falta el token anti-falsificación: usar `<ui-formulario>`, no un `<form>` a mano |
| La pantalla protegida vuelve siempre al acceso | La sesión no persistió: `PersistirSesionFiltro` debe estar registrado en `Program.cs` y `UseSession()` antes de los endpoints; las cookies deben estar permitidas |
| El usuario ve la sesión de otro | El token se guardó en un *singleton*, en un campo estático o en `DefaultRequestHeaders` del `HttpClient`; debe vivir solo en la `Sesion` por petición (`SesionWeb`) y enviarse por petición |
| `UseExceptionHandler` muestra 405/400 en vez de la página de error | La acción `Error` perdió `[IgnoreAntiforgeryToken]` o se le puso un verbo; se reejecuta con el verbo original |
| Tildes o «ñ» salen como entidades (`&#xF1;`) | Se quitó la configuración de `WebEncoderOptions` en `Program.cs` |
| «Servidor no disponible, intente más tarde» | Servidor apagado, URL base distinta de la real, firewall, o (HTTPS) certificado sin confiar/vencido/de otro host. Probar la URL con curl; ver la `InnerException` |
| Por HTTPS falla y por HTTP funciona | Certificado de desarrollo sin confiar: `dotnet dev-certs https --trust`; consultar por el mismo nombre que figura en el certificado |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín |
| Otro equipo recibe timeout y desde el PC del servidor con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| Las rutas dan `404` o se concatenan mal | La URL base no termina en «/» (`ConfiguracionApi` lo corrige) o la ruta lleva barra inicial: usar `"auth/login"`, no `"/auth/login"` |
| Campos `null` en una respuesta correcta | `[JsonPropertyName]` distinto del nombre del contrato, o el servidor cambió el campo |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `415` / `ERR-415` | El cuerpo no se envió como JSON: usar `ClienteApi` (no armar `HttpRequestMessage` en otra clase) |
| Muestra «El servidor respondió con un error inesperado (HTTP nnn)» | El cuerpo del error no era JSON (página de IIS Express, proxy): revisar la URL y el servidor |
| `MSB3027`/`MSB3021`: no se puede copiar el `.dll` | Hay una instancia de la aplicación en ejecución: cerrarla antes de compilar |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el `.csproj` |
| Pruebas: «Solution root could not be located» o `DirectoryNotFoundException` al crear la aplicación | Falta el `.slnx` en la carpeta del proyecto, o la prueba no fija el *content root* (`UseContentRoot` hacia la carpeta del `.csproj`, como en `PantallasWebPruebas` y `ComposicionPruebas`) |
