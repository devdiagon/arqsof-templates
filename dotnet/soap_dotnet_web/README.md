# Plantilla: cliente web .NET (ASP.NET Core MVC) para servicios SOAP

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita (acceso, sesión, estilos,
componentes de vista, transporte y pruebas) y deja claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** C# 14, `net10.0`, `Nullable` e `ImplicitUsings` habilitados. **Plataforma:** ASP.NET Core 10 (Kestrel o IIS Express). **Interfaz:** controladores MVC + **vistas Razor** (`.cshtml`) escritas a código, sin librerías de UI (ni Bootstrap, ni jQuery, ni framework de JavaScript). **Arquitectura:** MVC por capas.
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` `10.0.652802`) con un **proxy generado desde el WSDL** por `dotnet-svcutil` (§4, paso 1). No se escribe el contrato a mano.
- **El navegador no habla SOAP:** quien llama al servidor es el controlador (el servidor del cliente web), nunca JavaScript. Por eso el servidor SOAP no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (tema **oscuro**, tipografía, medidas, componentes, disposición dividida). Se aplica con **componentes reutilizables** (*tag helpers* `ui-*`) y un único `wwwroot/css/estilos.css`.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla. Con un proxy generado, el generador cumple por sí mismo R3 y R4.
- **Contraparte de escritorio:** `soap_dotnet_escritorio` comparte con esta plantilla las capas `modelo`, `excepciones`, `ws`, `servicio` y los estilos (mismos nombres, mismo código). Cambian `controlador` y `vista`.
- **Verificada:**
  - `dotnet test` → **61 pruebas xUnit en verde**, sin servidor SOAP ni interacción: URL y binding http/https, traducción del fallo de red a `RED`, `ServicioBase`, controlador de acceso (con un autenticador falso), **cada componente `ui-*`** (HTML, clases y escape de todo texto dinámico) y un **recorrido en memoria de la aplicación completa** (`PantallasWebPruebas`: rutas, convención de controladores, vistas Razor, sesión, cookies, token anti-falsificación, acceso, pantalla protegida y cierre de sesión).
  - La aplicación se **ejecutó** (`dotnet run`) y se comprobó con `curl`: `/` sin sesión redirige a `/Acceso/Acceder`, la pantalla de acceso carga con título y componentes resueltos, el acceso responde el mensaje del marcador «Falta conectar el servicio del dominio.» y se sirven `estilos.css`, `app.js` y la imagen.
  - La receta de §4 es la que se siguió y verificó en el cliente web del conversor de unidades (acceso, categorías, unidades y conversión contra un servidor .NET real, con sus errores de negocio).
  - **No se ha hecho:** ver la plantilla en un navegador (el aspecto no se comprobó), una llamada a un servidor real desde la plantilla sin tocar (no tiene proxy ni dominio), la presentación clara, `dotnet publish`, ni la ejecución desde Visual Studio con el perfil `IIS Express`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta** si se va a generar el proxy en ella: `dotnet-svcutil` falla con rutas muy largas (§9).
2. **Renombrar** `soap_dotnet_web.csproj` y `soap_dotnet_web.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. **Además, en `ec/edu/monster/vista/_ViewImports.cshtml` cambiar `@addTagHelper *, soap_dotnet_web` por el nuevo nombre del proyecto** (es el nombre del ensamblado): si no, los componentes `ui-*` salen como texto y las pantallas no se ven bien. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10; la primera vez restaura los paquetes (Internet).
4. **Configurar el servidor SOAP:** en `ec/edu/monster/ws/ConfiguracionServicio.cs`, `UrlPorDefecto` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor). No hay nombre de puerto ni «base + servicio»: ver §8.
5. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la pestaña y de la pantalla de acceso) y, si se quiere, **sustituir la imagen** `wwwroot/img/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6).
6. **Generar el proxy** desde el WSDL del servidor (§4, paso 1) y conectar el servicio en `Program.cs` (§4, paso 6).
7. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
8. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar (F5 en Visual Studio o `dotnet run --launch-profile http`). Con la plantilla sin tocar abre `http://localhost:5193/`, redirige al acceso y este responde que falta conectar el dominio.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Acceso (compartido)** | `controlador/AccesoControlador`, `vista/acceso/Acceder.cshtml`, `modelo/AccesoModelo`, `servicio/IAutenticador` | No; el servicio del dominio implementa `IAutenticador`. Solo la pantalla de destino (`RedirectToAction("Index", "Inicio")`) |
| **Transporte SOAP** | `ws/ClienteWS` (binding por esquema http/https, timeouts, `LlamarAsync` con traducción de fallos de red) | No |
| **URL del servicio** | `ws/ConfiguracionServicio` (`UrlPorDefecto`, variable `WS_URL`, normalización) | `UrlPorDefecto` siempre; el resto no |
| **Proxy generado** | — (`ws/Reference.cs` y `dotnet-svcutil.params.json`, generados desde `wsdl/`) | **Se genera** por proyecto; **no se edita** |
| **Fábrica del cliente del dominio** | — (`ws/IClienteXxx` + `ws/ClienteWSXxx` sobre el proxy) | **Se crea** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`ExigirToken`, `Verificar`, `ExigirRespuesta`, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Base web** | `controlador/ControladorBase` (`VolverAlAcceso`), `ConvencionControlador`, `SesionWeb`, `PersistirSesionFiltro`, `ExigirSesionAttribute` | No |
| **Composición** | `Program.cs` (servicios, sesión, rutas de vistas, filtros, codificación) | Se registra el servicio del dominio (marcado `DOMINIO`) |
| **Estilos y componentes** | `wwwroot/css/estilos.css`, `wwwroot/js/app.js`, `vista/componentes/Ui*TagHelper.cs`, `vista/compartido/_Layout.cshtml`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se escriben clases ni estilos en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` |
| **Pantalla de ejemplo (dominio)** | `controlador/InicioControlador`, `vista/inicio/Index.cshtml` | **Se reemplaza** por las pantallas del dominio (conservar la acción `Error`) |
| **Pruebas** | `pruebas/` (URL, binding, transporte, `ServicioBase`, acceso, componentes, recorrido de la aplicación) | Se conservan; se agregan las del dominio |
| **Dominio (vacío)** | — | Nuevos `ws/`, `servicio/`, `controlador/`, `vista/`, `modelo/` |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec Program.cs`).

## 3. Estructura

```
Program.cs                         punto de composición: servicios, sesión, rutas de vistas, filtros y codificación
wsdl/                              copia del WSDL del servidor (fuente de verdad del contrato); versionada
docs/CLIENT_STYLES_DOTNET.md       guía de estilos
wwwroot/                           css/estilos.css, js/app.js, img/fondo_app.png    lo único público (se sirve tal cual)
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion, AccesoModelo, ErrorModelo                entidades y modelos de vista: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                                código + mensaje; ExigeLogin, ServidorNoDisponible
├── ws/            ClienteWS, ConfiguracionServicio (+ Reference.cs generado, IClienteXxx y ClienteWSXxx del dominio)
├── servicio/      ServicioBase, IAutenticador, AutenticadorPendiente (+ los servicios del dominio)    única capa que conoce los tipos generados
├── controlador/   ControladorBase, AccesoControlador, InicioControlador, ConvencionControlador,
│                  SesionWeb, PersistirSesionFiltro, ExigirSesionAttribute
├── vista/         Textos, _ViewImports, _ViewStart,
│   ├── acceso/        Acceder.cshtml                               una carpeta por controlador, una vista por acción
│   ├── inicio/        Index.cshtml
│   ├── compartido/    _Layout.cshtml, Error.cshtml
│   └── componentes/   UiEncabezado, UiCampo, UiSelector, UiMensaje, UiBoton, UiListaAcciones, UiFormulario, UiCerrarSesion  (tag helpers)
└── pruebas/       xUnit en el mismo proyecto, sin servidor SOAP
```

Dependencias permitidas: `vista (Razor) ← controlador → servicio → ws`; `modelo` y `excepciones` no dependen de nadie.
**Los tipos generados (el «Dto» de la familia) viven en `ws/Reference.cs` y no salen de `servicio/`**; no hay carpeta `dto` ni clases `Dto` a mano.
Los controladores **nunca importan `ws`**: hablan con los servicios y entregan a la vista un modelo de `modelo/`. La vista (`.cshtml`) no tiene lógica de negocio ni llama al servidor.
Si el WSDL cambia solo se tocan `ws` y `servicio`.

**Las vistas no viven en `Views/`** sino en `ec/edu/monster/vista/{controlador}/{vista}.cshtml` y `vista/compartido/` (`Program.cs` configura `RazorViewEngineOptions.ViewLocationFormats`), para respetar los directorios en minúscula de la familia.
**Los controladores se llaman `XxxControlador`** y la ruta usa `Xxx`: `ConvencionControlador` quita el sufijo y `[Controller]` en `ControladorBase` los hace descubribles.

**Una petición, de punta a punta:** navegador → `UseSession` → filtro global (token anti-falsificación en todo POST) → `[ExigirSesion]` (si la pantalla es protegida) → controlador (`*Controlador`) → servicio (por petición, con la `Sesion` del usuario) → `ClienteWS` (SOAP) → el controlador entrega un modelo → vista Razor → `PersistirSesionFiltro` guarda el token en la sesión antes de escribir la respuesta.
El estado del usuario (el token) vive en la **sesión de ASP.NET Core** (`ISession`), cargado en una `Sesion` *scoped*, **nunca** en un servicio *singleton*: lo compartirían todos los usuarios. El cliente SOAP (`ClienteWSXxx`), en cambio, no guarda estado (un canal por llamada) y sí es *singleton*.

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems(token)` que devuelve una lista y su pantalla. Se sigue **siempre el mismo orden**, de abajo hacia arriba.

1. **Proxy generado** — guardar el WSDL y generar:
   ```
   curl -o wsdl/Servicio.wsdl "<URL del servicio>?singleWsdl"      # servidor .NET: un solo archivo con los esquemas
   dotnet tool install dotnet-svcutil --tool-path <carpeta corta>\herr
   dotnet-svcutil wsdl/Servicio.wsdl -n "*,ec.edu.monster.ws" -d ec/edu/monster/ws -o Reference.cs -nl
   ```
   - Un servidor **Java** no entrega `?singleWsdl`: guardar `?wsdl` **y** el XSD que importa (`schemaLocation`) y cambiar el `schemaLocation` a un nombre relativo.
   - La herramienta modifica el `.csproj` (añade `System.ServiceModel.*` `4.10.*`): **dejar solo** `System.ServiceModel.Http` y `Primitives` `10.0.652802`.
   - Generar desde una copia en una ruta corta si la del repositorio es larga (§9); puede hacer falta `DOTNET_ROLL_FORWARD=Major`. Conservar `dotnet-svcutil.params.json` (documenta cómo regenerar; ajustar la ruta del WSDL) y **no editar** `Reference.cs`.
   - El proxy trae `RespuestaBase` (`estado`, `codigo`, `mensaje`), los tipos de cada operación, la interfaz del contrato y `ServicioXxxClient` (derivada de `ClientBase<T>`).
2. **Interfaz y fábrica del cliente** — en `ws/`, una interfaz `IClienteXxx` con las operaciones que usa el cliente (permite el doble de pruebas) y `ClienteWSXxx` que la implementa sobre el proxy:
   ```csharp
   public class ClienteWSXxx : IClienteXxx
   {
       private readonly string _url;
       public ClienteWSXxx(string? url = null) { _url = url ?? ConfiguracionServicio.ObtenerUrl(); }

       public Task<RespuestaLista> ObtenerItemsAsync(string token) =>
           ClienteWS.LlamarAsync(CrearCliente, c => c.obtenerItemsAsync(token));

       private ServicioXxxClient CrearCliente() =>
           new(ClienteWS.CrearBinding(_url), new EndpointAddress(_url));   // URL completa; no usar la del código generado
   }
   ```
   Un cliente por llamada (un cliente en estado *Faulted* no se reutiliza); `LlamarAsync` lo cierra, o hace `Abort()` y convierte el fallo de red en `ExcepcionServicio` con código `RED`.
3. **Servicio** — `servicio/ServicioXxx(IClienteXxx cliente, Sesion sesion) : ServicioBase(sesion), IAutenticador`. Es la **única** capa que ve los tipos generados:
   ```csharp
   public async Task IniciarSesionAsync(string usuario, string contrasenia)        // IAutenticador
   {
       var r = ExigirRespuesta(await cliente.LoginAsync(usuario, contrasenia));
       Verificar(r.estado, r.codigo, r.mensaje);
       if (string.IsNullOrEmpty(r.token)) throw new ExcepcionServicio("El servidor no entregó un token de sesión", "ERR-500");
       Sesion.Token = r.token;
   }

   public async Task<IReadOnlyList<string>> ObtenerItemsAsync()
   {
       var token = ExigirToken();
       var r = ExigirRespuesta(await cliente.ObtenerItemsAsync(token));
       Verificar(r.estado, r.codigo, r.mensaje);
       return r.elementos?.ToList() ?? [];     // las listas pueden venir ausentes
   }
   ```
   - `ExigirToken()` lanza `AUTH-002` sin llamar al servidor si la `Sesion` no tiene token. `Verificar(...)` deja pasar si `estado` es `OK`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor y descarta el token si `ExigeLogin`.
   - Aquí van las reglas **del cliente** (formato, `NormalizarValor`: coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
4. **Modelo de vista y controlador** — `modelo/ItemsModelo` (solo propiedades) y `controlador/ItemsControlador(ServicioXxx servicio) : ControladorBase` con `[ExigirSesion]` (molde: `AccesoControlador` e `InicioControlador`):
   ```csharp
   [ExigirSesion]
   public class ItemsControlador(ServicioXxx servicio) : ControladorBase
   {
       public async Task<IActionResult> Index()
       {
           try { return View(new ItemsModelo { Items = await servicio.ObtenerItemsAsync() }); }
           catch (ExcepcionServicio ex) when (ex.ExigeLogin) { return VolverAlAcceso(ex); }   // vuelve al acceso con el mensaje
           catch (ExcepcionServicio ex) { return View(new ItemsModelo { Mensaje = ex.Message }); }   // solo el mensaje, nunca el código
           catch (Exception) { return View(new ItemsModelo { Mensaje = "Ocurrió un error inesperado. Intente de nuevo." }); }
       }
   }
   ```
   - Un `POST` valida lo trivial (campo vacío), llama al servicio y vuelve a mostrar **la misma vista** con el resultado o el error, **conservando lo capturado** (devolver los valores en el modelo). El token anti-falsificación se valida solo en todo POST.
   - Un fallo de red no cierra la aplicación: se muestra el mensaje y la pantalla sigue usable (con «Reintentar» si no hay datos). Si una pantalla necesita recargar un catálogo para volver a dibujarse y no puede, conservar las opciones elegidas por el usuario.
5. **Vista** — `vista/items/Index.cshtml` (carpeta con el nombre del controlador), **solo con componentes `ui-*`** y `@model`. Sin `class`, sin `style`, sin lógica; muestra lo que dejó el controlador. Moldes: `acceso/Acceder.cshtml` e `inicio/Index.cshtml`.
   ```html
   @model ItemsModelo
   @{ ViewData["Titulo"] = "Elementos"; }
   <ui-encabezado>Elementos</ui-encabezado>
   <ui-mensaje mensaje="@Model.Mensaje" />
   <ui-lista-acciones elementos="@Model.Items" controlador="Detalle" accion="Ver" parametro="clave" />
   <ui-cerrar-sesion />
   ```
   Un formulario es `<ui-formulario accion="Guardar" controlador="Items">…</ui-formulario>`; los enlaces a otras pantallas, `<ui-boton href="@Url.Action(...)">`.
6. **Composición** — en `Program.cs`, en lugar de `AutenticadorPendiente` (que se elimina):
   ```csharp
   builder.Services.AddSingleton<IClienteXxx>(_ => new ClienteWSXxx());
   builder.Services.AddScoped<ServicioXxx>();
   builder.Services.AddScoped<IAutenticador>(sp => sp.GetRequiredService<ServicioXxx>());
   ```
   Cambiar la pantalla de destino del acceso si ya no es `Inicio`, y reemplazar `InicioControlador`/`inicio/Index.cshtml` por las pantallas del dominio (**conservar la acción `Error`**: la usa `UseExceptionHandler`).
7. **Pruebas** (en `ec/edu/monster/pruebas/`):
   - Un doble escrito a mano de `IClienteXxx` que devuelve respuestas simuladas (con los tipos del proxy), para el servicio: OK, error de negocio, red, sesión vencida, coma enviada como punto, sin sesión no se consulta nada.
   - **Una prueba de recorrido por pantalla** en `PantallasWebPruebas`: reemplazar `IClienteXxx` por el doble en `ConfigureServices` y comprobar el HTML (datos, mensajes sin código, escape, redirección al acceso por token vencido, formulario conservado ante error). **Es la que detecta una vista o un componente inválido**, que compila pero falla al abrir la pantalla.
   - Una **prueba de humo** contra un servidor falso local (CoreWCF) con el mismo contrato detecta un WSDL desactualizado (opcional).

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- **El token** vive en `Sesion` (en la sesión de ASP.NET Core, en memoria del servidor web), no se guarda en disco, ni en la cookie (la cookie solo lleva el identificador de la sesión), ni en logs, ni en la página. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al acceso; no se cuenta el tiempo en el cliente.
  `[ExigirSesion]` solo comprueba que *haya* token; si el servidor ya lo venció, lo detecta el servicio al llamar.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual). El campo numérico de la vista es `type="text"` con `inputmode="decimal"`, no `type="number"`.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Escapar todo texto dinámico**: Razor y los componentes ya lo hacen; **no** usar `Html.Raw` con datos del servidor ni del usuario. La contraseña nunca se devuelve a la página.
- Todo `POST` va con token anti-falsificación (`<ui-formulario>` y `<ui-cerrar-sesion>` lo incluyen; es obligatorio por el filtro global). Cerrar sesión es un `POST`.
- **Nunca red en el hilo de la petición sin `async/await`:** usar las operaciones `…Async`. Un fallo de red no cierra la aplicación.
- Cada `POST` que cambie algo debería terminar en redirección (Post/Redirect/Get) si el resultado no es la página de respuesta del mismo formulario; el acceso y el cierre de sesión ya lo hacen.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo usan los componentes** `ui-*`; no escriben clases, estilos ni colores. Componentes disponibles:

| Etiqueta | Para qué |
|---|---|
| `_Layout` | Estructura de toda página: cabecera HTML, imagen a la izquierda y columna de contenido (420, centrada) a la derecha. `ViewData["Titulo"]` es obligatorio |
| `<ui-formulario accion controlador>` | Formulario POST con token anti-falsificación; al enviarlo `app.js` deshabilita los controles y muestra el progreso |
| `<ui-encabezado>` | Título de pantalla (`aplicacion="true"` para el título de la aplicación en el acceso) |
| `<ui-campo id etiqueta>`, `<ui-selector id etiqueta opciones seleccionada>` | Etiqueta + campo de texto/contraseña, etiqueta + lista desplegable (`invalido="true"` marca el borde de error) |
| `<ui-mensaje mensaje>` | Mensaje o resultado (`tipo`: `error`, `exito`, `resultado`); no se dibuja si no hay texto |
| `<ui-boton>` | Botón primario (acción principal) o `tipo="secundario"` (salida o regreso); con `href` es un enlace; `completo` y `salida` ajustan ancho y separación |
| `<ui-lista-acciones elementos controlador accion parametro>` | Una columna de botones primarios generados con datos del servidor; cada uno enlaza a `controlador/accion?parametro=elemento` |
| `<ui-cerrar-sesion />` | Botón secundario «Cerrar sesión» (POST) separado 32 |

- **Tema oscuro** fijo (`<body class="tema-oscuro">` en `_Layout`); no hay selector. Los tokens son variables CSS en `estilos.css`. Para la presentación clara, agregar el bloque `.tema-claro` con los valores de `docs/CLIENT_STYLES_DOTNET.md` §2.3 y cambiar la clase del `<body>` (la clara no usa imagen).
- **Disposición:** ventana dividida: **imagen a la izquierda** (560, fija, completa y centrada) y la pantalla a la derecha (columna de máximo 420, centrada); bajo 900 px, columna única sin imagen y sin scroll horizontal. La imagen es del **layout**, no de cada vista. Es la misma disposición que `soap_dotnet_escritorio` (el documento de estilos la define con las zonas invertidas, §7.0).
- **Las imágenes** van en `wwwroot/img` y se referencian desde el CSS con ruta relativa (`../img/...`).
- **Componente nuevo:** crear `vista/componentes/UiXxxTagHelper.cs` (con `AppendHtml`/`TagBuilder`, que **escapan** el texto) y su CSS en `estilos.css`, con tokens (`var(--…)`), nunca con colores sueltos. Agregar su prueba en `ComponentesPruebas` (incluido el escape) y probarlo en un navegador a 1100 px y a menos de 900 px.
- Una vista o un componente inválido **compila** y falla al abrir la pantalla (HTTP 500) o, si falta el `@addTagHelper`, se ve como etiqueta literal: probar cada pantalla nueva con `PantallasWebPruebas` y abrirla.

## 6. Servidor y ejecución

- **Servidor SOAP por defecto:** `ConfiguracionServicio.UrlPorDefecto` (URL completa). Sin recompilar: variable de entorno `WS_URL`
  (PowerShell: `$env:WS_URL = "http://<ip>:57592/Ruta"; dotnet run --launch-profile http`).
- **HTTP y HTTPS:** el esquema de la URL decide el modo de seguridad del binding (`ClienteWS.CrearBinding`): `http://` sin seguridad, `https://` con seguridad de transporte. **HTTPS exige un certificado de servidor válido y de confianza** (en desarrollo, `dotnet dev-certs https --trust`, y que el nombre al que se conecta esté en el certificado).
  La validación del certificado **no** se desactiva en el cliente. Si falla, el cliente solo muestra «No se pudo comunicar con el servidor» (no distingue la causa).
- **El WSDL no se usa en ejecución:** un cliente WCF no lo descarga; usa el binding y la URL indicados. Con el servidor SOAP apagado la aplicación arranca igual y la llamada falla con `RED` tras el tiempo límite (10 s).
- **Entorno de desarrollo de la familia:** IIS Express por HTTP (`http://localhost:57592/<Ruta>`); el perfil `http` (Kestrel) del servidor usa el puerto `5267`. Reglas de red y firewall: `IIS_CONFIG.md` del proyecto del servidor.
- **Puerto del cliente web:** `5193` (perfil `http` de `Properties/launchSettings.json`); debe ser **distinto** del del servidor (`5267` / `57592`). Cambiarlo ahí si hay otro proyecto abierto a la vez.
- **Visual Studio:** *Run* (F5) con el perfil `http`. Detener la ejecución antes de compilar desde la terminal (el `.dll` queda bloqueado).
- **Terminal:** `dotnet run --launch-profile http`, y abrir `http://localhost:5193/`.
- **Sesión:** la cookie `.Cliente.Sesion` (`HttpOnly`) dura 20 minutos de inactividad (`Program.cs`); es independiente del token del servidor (que vence a su ritmo y se reconoce por el código `AUTH-003`). Si la aplicación se reinicia, las sesiones en memoria se pierden y el usuario vuelve al acceso.
- **Publicación:** `dotnet publish -c Release`. **Probarla** antes de darla por buena (las vistas se compilan dentro del ensamblado).

## 7. Comandos y verificación

```
dotnet test                              # pruebas locales (sin servidor SOAP ni interacción); detener la aplicación antes
dotnet run --launch-profile http         # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con la aplicación en marcha (la vista se revisa a mano; los controladores y los componentes, con pruebas):
1. Sin sesión, abrir una ruta protegida (p. ej. `/`): debe redirigir al acceso.
2. Acceso con credenciales incorrectas: muestra el mensaje del servidor (sin código) y conserva el usuario; correcto: entra a la primera pantalla; «Cerrar sesión» vuelve al acceso y «Atrás» no muestra la pantalla protegida.
3. Error de negocio: se muestra el mensaje y la pantalla sigue usable, con lo capturado.
4. Con el servidor SOAP apagado: aparece «No se pudo comunicar con el servidor» (≈ 10 s) y la aplicación sigue respondiendo.
5. Token vencido (reiniciar el servidor SOAP con la sesión abierta): vuelve al acceso con el mensaje.
6. Ventana ancha (≥ 900 px: imagen a la izquierda) y estrecha (columna única, sin imagen, sin scroll horizontal); tildes y «ñ» en pantalla.

Un agente que no pueda abrir el navegador debe decir explícitamente qué verificó (pruebas, peticiones HTTP con `curl`, contra qué servidor) y que **no vio la aplicación en un navegador real** si es el caso.
Con `curl` en Windows, los textos con tildes deben enviarse codificados en UTF-8 (`--data-raw` con `%C3%B3`…); la consola puede alterarlos y producir falsos errores de «unidad no soportada».

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws` y `servicio`. Es la convención común de la familia (`CONVENCION_SOAP.md`):

- SOAP **1.1**, *document/literal wrapped*, `BasicHttpBinding`. Elementos **calificados** con el namespace del servicio y `SOAPAction` = `TNS + Servicio + "/" + operación`: con el proxy generado los cumple el generador (R3, R4); un servidor .NET rechaza la acción vacía con HTTP 500.
- **El cliente no depende del nombre del puerto ni del binding** (R10): usa el proxy con una URL explícita. **La URL completa** del servicio se recibe por configuración; no se asume puerto ni «base + servicio» (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Éxito y error usan el mismo tipo.
- **Lo que el código no puede imponer** (acuerdo con el servidor): todos los datos viajan como texto (R6); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R7); los nombres de operaciones, parámetros y campos son fijos (R5/R8).
  Si el servidor cambia su contrato hay que **regenerar el proxy** y revisar `servicio`.
- Los campos que pueden faltar (`token`, listas) llegan `null` en un error: no darlos por existentes.
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (comportamientos del cliente WCF).
- Para un servicio **REST** reutilizar de esta plantilla todo salvo `ws/` (la capa `servicio` solo depende de `IClienteXxx`).

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| Las pantallas salen sin estilo de componentes: se ven `<ui-campo …>` o `asp-append-version` literales en el HTML | Falta o es incorrecto el `@addTagHelper` de `vista/_ViewImports.cshtml`: debe haber uno para `Microsoft.AspNetCore.Mvc.TagHelpers` y otro con **el nombre del ensamblado** (el del `.csproj` renombrado) |
| 404 en una pantalla recién creada, o «The view … was not found» | La vista no está en `vista/{controlador}/{Acción}.cshtml` (o `vista/compartido/`), o el controlador no termina en `Controlador` ni hereda de `ControladorBase` |
| Error CS0616 en un tag helper nuevo (`ViewContext` no es una clase de atributos) | Falta `using Microsoft.AspNetCore.Mvc.ViewFeatures;` |
| 400 al enviar un formulario | Falta el token anti-falsificación: usar `<ui-formulario>`, no un `<form>` a mano |
| La pantalla protegida vuelve siempre al acceso | La sesión no persistió: `PersistirSesionFiltro` debe estar registrado en `Program.cs` y `UseSession()` antes de los endpoints; las cookies deben estar permitidas |
| El usuario ve la sesión de otro | El token se guardó en un *singleton* o en un campo estático; debe vivir solo en la `Sesion` por petición (`SesionWeb`) |
| `UseExceptionHandler` muestra 405/400 en vez de la página de error | La acción `Error` perdió `[IgnoreAntiforgeryToken]` o se le puso un verbo; se reejecuta con el verbo original |
| Tildes o «ñ» salen como entidades (`&#xF1;`) | Se quitó la configuración de `WebEncoderOptions` en `Program.cs` |
| «No se pudo comunicar con el servidor» | Servidor SOAP apagado, URL distinta de la real, firewall, o (HTTPS) certificado sin confiar/vencido/de otro host. Abrir `<URL>?wsdl` en el navegador |
| Por HTTPS falla y por HTTP funciona | Certificado de desarrollo sin confiar: `dotnet dev-certs https --trust`; consultar por el mismo nombre que figura en el certificado |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín |
| Respuesta con campos vacíos / `null` | El servidor no califica los campos (R3) o el proxy está desactualizado: regenerar desde el WSDL actual |
| HTTP 500 *ActionNotSupported* | Se envía `SOAPAction` vacía: el proxy no es el generado o se alteró el atributo `Action` |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `dotnet-svcutil` falla (`MSB3030`, ruta demasiado larga, o pide otro runtime) | Generar en una copia con ruta corta (p. ej. `C:\Temp\gen`) y copiar `Reference.cs`; definir `DOTNET_ROLL_FORWARD=Major` |
| Avisos `NU1902`/`NU1903` de `System.Security.Cryptography.*` | Dependencia transitiva de los paquetes WCF; no impide funcionar. Revisar si hay una versión corregida |
| `MSB3027`/`MSB3021`: no se puede copiar el `.dll` | Hay una instancia de la aplicación en ejecución: cerrarla antes de compilar |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el `.csproj` |
| Pruebas: «Solution root could not be located» o `DirectoryNotFoundException` al crear la aplicación | Falta el `.slnx` en la carpeta del proyecto, o `PantallasWebPruebas` no fija el *content root* (`UseContentRoot` hacia la carpeta del `.csproj`) |
