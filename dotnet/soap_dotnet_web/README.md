# Plantilla: cliente web .NET (ASP.NET Core MVC) para servicios SOAP

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
con el esquema de respuesta de esta familia de proyectos. Trae el transporte, los tipos comunes, la verificación de la envoltura,
el layout con sus estilos y los componentes de vista, y deja marcado lo que cambia con el **dominio**.
No incluye pantallas de dominio ni autenticación: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0` (`Nullable` e `ImplicitUsings` habilitados). **Plataforma:** ASP.NET Core 10 (Kestrel o IIS Express). **Interfaz:** controladores MVC + **vistas Razor** (`.cshtml`) escritas a código, sin librerías de UI (ni Bootstrap, ni jQuery, ni framework de JavaScript). **Arquitectura:** capas, con MVC.
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` 10.0.652802) con **proxy generado**: `dotnet-svcutil` genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto (`wsdl/`), y `ws/ConfiguracionServicio` solo fija la URL completa, el binding y los tiempos límite. El WSDL **no se descarga en ejecución**.
- **El navegador no habla SOAP:** quien llama al servidor es el controlador (el servidor del cliente web), nunca JavaScript. Por eso el servidor SOAP no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (presentación oscura, tipografía, medidas, componentes, disposición). Se aplica con **componentes reutilizables** (*tag helpers* `ui-*`) y un único `wwwroot/css/estilos.css`.
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (copia en `docs/`, con el TNS fijo `http://ws.monster.edu.ec/` de R2): así entiende a un servidor Java o .NET que la cumpla. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Cómo se identifica quien opera lo define cada servidor y cada proyecto (§8); la plantilla no trae ninguna pantalla ni estado de usuario.
- **Núcleo:** `dto`, `excepciones`, `servicios/ServicioBase` y `ws` (`ConfiguracionServicio`, `TraductorRed`) son los mismos archivos que la plantilla `soap_dotnet_consola`: un proyecto con consola y web los copia **sin editarlos** (`COMPARTIR_CLIENTE.md`).
- **Verificada:** `dotnet test` → 77 pruebas xUnit en verde, sin servidor SOAP ni navegador: configuración de la URL y del binding, verificación de la envoltura (`ServicioBase`), traducción de fallos de red (`TraductorRed`, con un canal simulado), excepción del cliente, base de los controladores (`ControladorBase`: texto, vacíos y resultado de una sola lectura), **cada componente `ui-*`** (HTML, clases y escape de todo texto dinámico) y **la aplicación arrancada dentro de la prueba** (`PantallasWebPruebas`: rutas, layout en sus dos modos, vistas Razor, tag helpers resueltos, recursos estáticos, página de error y token anti-falsificación). `dotnet build` sin errores ni advertencias.
  Las recetas de §4, los componentes y los estilos son los que se siguieron y verificaron al construir un cliente web .NET sobre esta base (probado a mano en un navegador contra su servidor .NET).
  **No se ha hecho:** ver la plantilla en un navegador (el aspecto no se comprobó), una llamada a un servidor real (la plantilla no trae proxy ni dominio), la presentación clara, `dotnet publish`, ni la ejecución desde Visual Studio con el perfil `IIS Express`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta** para generar los proxies: `dotnet-svcutil` falla con rutas largas (§4, paso 1).
2. **Renombrar** `soap_dotnet_web.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. **Corregir también `@addTagHelper *, soap_dotnet_web` en `ec/edu/monster/vista/_ViewImports.cshtml`** con el nuevo nombre del proyecto (es el nombre del ensamblado). El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.ws`…), sin intermedio de módulo. Las carpetas de código van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); `wwwroot/`, `wsdl/` y `docs/` también; los archivos, en PascalCase.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10; la primera vez restaura los paquetes (Internet).
4. **Configurar el servidor:** en `ws/ConfiguracionServicio.cs`, `UrlPorDefecto` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor). Con el servidor **ya actualizado y en marcha**, guardar su WSDL y sus esquemas en `wsdl/` y generar el proxy (§4, paso 1).
   **Si el servidor publica varios servicios** (cada uno con su WSDL y su **URL completa**; R9 prohíbe asumir «base + nombre del servicio»):
   - Guardar el WSDL y los esquemas de cada uno en `wsdl/` y generar **un proxy por servicio, cada uno en su propio espacio de nombres** (p. ej. `ec.edu.monster.ws.generado.consulta`): los esquemas de un mismo servidor suelen definir los mismos tipos (como `RespuestaBase`) y en un espacio común las clases generadas chocan.
   - En `ConfiguracionServicio`, una constante con la URL completa de cada servicio y un método que la lea con `UrlConfigurada(servicio, urlPorDefecto)`:
     ```csharp
     public const string UrlConsultaPorDefecto = "http://localhost:5267/Consulta";
     public static string UrlConsulta() => UrlConfigurada("consulta", UrlConsultaPorDefecto);
     ```
   - Cada servicio se sobrescribe sin recompilar con la variable de entorno `WS_URL_CONSULTA` (el nombre del servicio, en mayúsculas). Los nombres de los servicios y de sus URL se registran en el `docs/PROJECT.md` del proyecto.
5. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la pestaña) y, si se quiere, **sustituir la imagen** `wwwroot/img/fondo_app.png` (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6). La imagen es un **PNG** (admite transparencia); si se cambia por otra, **se conserva el nombre `fondo_app.png`** para no tocar el CSS. Una imagen horizontal se muestra compacta al 60 % de su contenedor (`--margen-imagen: 20%` en `estilos.css`).
6. **Completar `docs/PROJECT.md`** (viene como esqueleto) y mantenerlo al día.
7. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar (F5 en Visual Studio o `dotnet run`). Con la plantilla sin tocar abre la pantalla `Inicio`.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración del transporte** | `ws/ConfiguracionServicio` (`CrearBinding`: binding y tiempos límite; `UrlConfigurada()` y `UrlConfigurada(servicio, urlPorDefecto)`; nada del dominio) | `UrlPorDefecto` siempre (o una constante por servicio, §1 paso 4); el resto no |
| **Traducción de fallos de red** | `ws/TraductorRed` (`CommunicationException` y `TimeoutException` → `ExcepcionServicio` con código `RED`; un canal por llamada) | No |
| **Proxy generado** | `wsdl/` (copias del WSDL y los esquemas) → `dotnet-svcutil` → `ws/generado/<servicio>/Reference.cs` | **Se genera** por proyecto y servicio (§4, paso 1) |
| **Interfaz y envoltorio** | — (`ws/IXCliente.cs` y `ws/XClienteWS.cs`) | **Se crean** por proyecto, una firma por operación del servidor (§4, paso 2) |
| **DTO base** | `dto/RespuestaBaseDto` (`Estado`, `Codigo`, `Mensaje`, `EsOk`) | No; se agregan DTO del dominio al lado |
| **Verificación de la envoltura** | `servicios/ServicioBase` (`Verificar`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (código + mensaje; códigos propios `RED` y `SRV-000`) | No |
| **Composición** | `Program.cs` y `Aplicacion.cs` (`Construir`: antiforgery, codificación, ubicación de las vistas, `TODO` del registro de clientes y servicios) | `Aplicacion` se completa con los clientes y servicios del dominio |
| **Base de los controladores** | `controlador/ControladorBase` (`Texto`, `Vacio`, `GuardarFlash`/`TomarFlash` para Post/Redirect/Get) | No; los controladores del dominio la extienden |
| **Layout y vistas base** | `vista/_Layout.cshtml` (dos modos y tres anchos), `_ViewImports.cshtml`, `_ViewStart.cshtml`, `Error.cshtml` | No (`_ViewImports`: el nombre del ensamblado, §1 paso 2) |
| **Componentes** | `vista/componentes/Ui*TagHelper` (15 *tag helpers* `ui-*`) | No |
| **Estilos y JavaScript** | `wwwroot/css/estilos.css`, `wwwroot/js/app.js`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos** | `vista/Textos` | `NombreAplicacion` y los textos del dominio |
| **Pantalla de ejemplo** | `controlador/InicioControlador`, `vista/Inicio.cshtml` | **Se reemplaza** por las pantallas del dominio |
| **Servicios, controladores, modelos y vistas** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec`).

## 3. Estructura

```
Program.cs                      arranca la aplicación (Aplicacion.Construir)
wwwroot/       css/estilos.css, js/app.js, img/fondo_app.png, favicon.ico     recursos estáticos
wsdl/                           copias del WSDL y de los esquemas de cada servicio (de aquí se genera el proxy)
docs/                           CLIENT_STYLES_DOTNET.md, CONVENCION_SOAP.md y CONVENCION_PRUEBAS.md (copias vigentes) y PROJECT.md (esqueleto del proyecto)
README.md                       esta guía: el único documento que queda en la raíz
ec/edu/monster/
├── Aplicacion.cs               punto de composición (clientes SOAP, servicios, MVC)
├── modelo/                     datos propios del cliente que no son de una pantalla; vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── ws/            ConfiguracionServicio, TraductorRed (+ IXCliente y XClienteWS del dominio)
│   └── generado/  <servicio>/Reference.cs                 proxy de dotnet-svcutil: se regenera, no se edita
├── controlador/   ControladorBase, InicioControlador, ErrorControlador (+ uno por pantalla)   rutas con atributos
├── vista/         _Layout, _ViewImports, _ViewStart, Inicio, Error, Textos, Modelos (+ una vista y un modelo por pantalla)
│   └── componentes/  Ui*TagHelper                         los componentes ui-*
└── pruebas/                                               xUnit + Moq, en el mismo proyecto (ver §4, paso 8)
```

Dependencias permitidas: `vista → controlador → servicios → ws`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `ws` ni `dto`**: recibe de su controlador un modelo con el texto ya listo para mostrar y usa `Textos`.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo la implementación de `ws` conoce el protocolo, los tipos generados y las excepciones de WCF.
Si el WSDL cambia solo se tocan `ws`, `dto` y `servicios`. **Quien llama al servidor es el controlador**, nunca el navegador.

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems()` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **WSDL y proxy** — con el servidor en marcha, guardar por servicio el WSDL y sus esquemas: `<URL>?wsdl`, `<URL>?xsd=xsd0` y `<URL>?xsd=xsd1` (un servidor CoreWCF; uno Java publica un solo `.xsd`), como `wsdl/<Servicio>.wsdl`, `<Servicio>.xsd0.xsd`, `<Servicio>.xsd1.xsd`.
   En cada `.wsdl`, **reescribir los `schemaLocation` absolutos a los nombres de archivo locales**. Instalar la herramienta una vez (`dotnet tool install -g dotnet-svcutil`, 8.0.0; con el SDK 10 conviene `DOTNET_ROLL_FORWARD=LatestMajor`) y, **en una carpeta de ruta corta** con un proyecto `net10.0` descartable, por cada servicio:
   ```
   dotnet-svcutil wsdl/Consulta.wsdl -d . -o gen/consulta/Reference.cs -n "*,ec.edu.monster.ws.generado.consulta" --noLogo
   ```
   Copiar cada `Reference.cs` a `ec/edu/monster/ws/generado/<servicio>/`. **No dejar que `dotnet-svcutil` modifique este proyecto** (añade paquetes `System.ServiceModel.*` 4.10 que no se usan). Lo generado: la clase `…Client` con constructor `(Binding, EndpointAddress)`, métodos `<operación>Async` con el nombre de la operación en minúscula inicial (`obtenerItemsAsync`), tipos con propiedades en minúscula (`estado`, `codigo`, `mensaje`) y las listas como clases que heredan de `List<T>` (se usa `Count`, no `Length`).
   **El código generado no se edita.** Cuando el servidor cambia su contrato: volver a guardar el WSDL y repetir.
2. **Interfaz y envoltorio** — `ws/IXCliente.cs` (interfaz propia con las operaciones que usa el cliente, retorno `Task<DTO propio?>`) y `ws/XClienteWS.cs`, que implementa la interfaz sobre el cliente generado, copia cada respuesta generada a un DTO propio y **traduce los fallos de red con `TraductorRed`**. **El cliente no usa la URL de `Reference.cs`**: se crea con su propio binding y la URL configurada (R9, R10):
   ```csharp
   public sealed class XClienteWS : IXCliente
   {
       private readonly Func<ConsultaClient> _crearProxy;          // clase generada por dotnet-svcutil

       public XClienteWS(Func<ConsultaClient> crearProxy) { _crearProxy = crearProxy; }

       public static IXCliente Crear()
       {
           string url = ConfiguracionServicio.UrlConfigurada("consulta", "http://localhost:5267/Consulta");
           return new XClienteWS(() => new ConsultaClient(ConfiguracionServicio.CrearBinding(url), new EndpointAddress(url)));
       }

       public Task<RespuestaListaDto?> ObtenerItemsAsync() =>
           TraductorRed.LlamarAsync(_crearProxy, async proxy => Convertir(await proxy.obtenerItemsAsync()));

       internal static RespuestaListaDto? Convertir(RespuestaLista? r)     // internal: lo prueban las pruebas del mismo proyecto
       {
           if (r is null) { return null; }                                 // ServicioBase la trata como SRV-000
           var dto = new RespuestaListaDto { Estado = r.estado, Codigo = r.codigo, Mensaje = r.mensaje };
           if (r.elementos is not null) { foreach (var e in r.elementos) { dto.Elementos.Add(e); } }
           return dto;
       }
   }
   ```
   Los nombres exactos (`ConsultaClient`, `obtenerItemsAsync`, `RespuestaLista`) los decide `dotnet-svcutil` y solo se usan en este archivo. Con varios servicios, cada `Reference.cs` está en su espacio de nombres: importar solo el del servicio en cada envoltorio.
3. **DTO propio** — clase `…Dto` en `dto` que extienda `RespuestaBaseDto`, con propiedades `{ get; set; }`, todas texto y `string?`; una lista se inicializa vacía (`public List<string> Elementos { get; } = [];`): **nunca nula**.
4. **Servicio** — `servicios/XServicio : ServicioBase` (con sufijo `Async` en lo que llama a la red; los métodos públicos, `virtual`: Moq los necesita):
   ```csharp
   public virtual async Task<IReadOnlyList<string>> ItemsAsync()
   {
       RespuestaListaDto respuesta = Verificar(await _cliente.ObtenerItemsAsync());
       return respuesta.Elementos;
   }
   ```
   `Verificar(...)` devuelve la respuesta si `EsOk`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`. **No captura excepciones de red** (ya llegan como `RED`). Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante). Las reglas del dominio las decide el servidor y el cliente muestra su mensaje.
5. **Modelo de la pantalla** — un `record` en `vista/Modelos.cs` con el texto ya listo para mostrar: lo escrito, el mensaje, el campo con error y, si hay resultado, sus datos. Es lo único que la vista recibe.
6. **Controlador** — `controlador/XControlador : ControladorBase`, con las rutas por atributos (`[HttpGet("consulta")]`, `[HttpPost("consulta")]`) y el servicio por constructor (`ILogger<T>` para registrar lo inesperado). Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar los campos vacíos **antes** de llamar, en el orden de la pantalla (se informa el primero); mostrar solo el mensaje del error, nunca el código; conservar lo escrito ante un error salvo las contraseñas; no conocer la pantalla siguiente:
   ```csharp
   [HttpPost("consulta")]
   public async Task<IActionResult> Consultar([FromForm] string? codigo)
   {
       var modelo = Inicial with { Codigo = Texto(codigo) };
       if (Vacio(modelo.Codigo))
           return View("Consulta", modelo with { Error = Textos.CampoObligatorio(Textos.Codigo), Invalido = "codigo" });
       try { modelo = modelo with { Items = await _servicio.ItemsAsync() }; }
       catch (ExcepcionServicio e) { modelo = modelo with { Error = e.Message }; }
       catch (Exception e) { _registro.LogError(e, "…"); modelo = modelo with { Error = Textos.ErrorInesperado }; }
       return View("Consulta", modelo);          // una consulta devuelve la página directamente
   }
   ```
   **Una operación que cambia datos usa Post/Redirect/Get**: el `POST` valida, llama al servicio, `GuardarFlash(modelo)` y `Redirect("~/ruta")`; el `GET` hace `View("…", TomarFlash<Modelo>() ?? Inicial)`. Así F5 no repite la operación y el resultado se lee una sola vez. `GuardarFlash` nunca debe llevar una contraseña. Si el servidor no debe recibir un envío repetido mientras el anterior sigue en curso, el controlador lo descarta (`app.js` solo deshabilita los controles en el navegador).
7. **Vista** — `vista/Consulta.cshtml` (`@model` del paso 5), solo `ui-*`: sin `class`, `style` ni lógica. `ViewData["Titulo"]` (obligatorio), `["Modo"]` y `["Ancho"]` dan la disposición. Patrón de toda pantalla: `<ui-formulario accion="/consulta" ocupado="@Textos.Consultando">` con `<ui-encabezado>`, los `<ui-campo>` (con `invalido` y `autofocus`), `<ui-fila-botones>` (el principal y `Cancelar` con `cancelar="true"`) y `<ui-mensaje mensaje="@Model.Error">`; el resultado (`<ui-tarjetas>`, `<ui-tabla>`) va **después** del formulario. Ver `Inicio.cshtml` y `docs/CLIENT_STYLES_DOTNET.md` §5 y §7.2.
8. **Registro** — en `Aplicacion.Construir`: `AddSingleton(_ => XClienteWS.Crear())` y `AddSingleton<XServicio>()`. La ruta ya la declara el controlador.
9. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto).
   - **Envoltorio:** `Convertir` se prueba campo a campo con los tipos generados.
   - **Pantallas:** `ServidorWeb` arranca la aplicación en la prueba, en un puerto libre; `Aplicacion.Construir(args, servicios => …)` permite sustituir el cliente SOAP por uno simulado (`RemoveAll<IXCliente>()` y `AddSingleton(simulado)`). Se recorre cada ruta con `HttpClient` (`AllowAutoRedirect = false`): éxito, campos vacíos, error de negocio, redirección y resultado de una sola lectura. Un `POST` necesita el token anti-falsificación: se toma del formulario de la página (`__RequestVerificationToken`) y las cookies del mismo `HttpClient`. Revisar también que no quede ningún `<ui-` sin resolver.
   - El contrato y el envoltorio de protocolo **no** se prueban con código: se verifican a mano con `docs/CONVENCION_PRUEBAS.md` §6.3.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, tiempo agotado y `Fault` son `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- Un fallo de red no cierra la aplicación: se muestra el mensaje en la zona de estado de la pantalla.
- **Un fallo al cerrar el canal después de una operación exitosa no es un error de la operación** (`TraductorRed` lo descarta con `Abort`).
- Todo texto dinámico sale escapado (`@Modelo.Mensaje`); **nunca** `@Html.Raw` con datos del servidor.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo usan los componentes `ui-*`**; no colorean ni dimensionan elementos sueltos.

| Componente | Para qué |
|---|---|
| `<ui-formulario accion ocupado>` | Formulario `POST` de una pantalla con el indicador de progreso y el token anti-falsificación |
| `<ui-encabezado>` | Título de pantalla (`aplicacion="true"`: el de la aplicación, centrado; `plano="true"`: texto grande sin panel) |
| `<ui-campo>`, `<ui-selector>` | Etiqueta + campo o lista desplegable; `invalido` marca el borde de error |
| `<ui-boton>` | Primario (acción principal) o secundario; con `href` es un enlace (`cancelar="true"`: lo activa `Esc`), sin él envía el formulario |
| `<ui-fila-botones>`, `<ui-fila-campos>`, `<ui-campo-con-acciones>` | Disposición: botones centrados, dos campos con una flecha, un campo con sus botones |
| `<ui-cuadricula-acciones>`, `<ui-lista-acciones>` | Botones grandes en dos columnas; una columna de botones generada con datos del servidor |
| `<ui-mensaje>` | Mensaje de error, éxito o resultado; no se dibuja vacío |
| `<ui-tarjetas>`, `<ui-tarjeta>` | Resumen de un cambio de un valor, con los textos ya preparados |
| `<ui-tabla>` | Tabla con encabezado fijo y columnas numéricas a la derecha |
| `<ui-progreso>` | Indicador de espera (lo incluye `<ui-formulario>`) |

- **Disposición:** dos modos en `_Layout.cshtml`. `ViewData["Modo"] = "acceso"` (por defecto) es dividida: pantalla a la izquierda (columna de 420) e imagen a la derecha. `"aplicacion"` usa el ancho completo y la imagen es una capa fija de fondo: completa, centrada, con opacidad 40 %, con los paneles sobre ella. `ViewData["Ancho"]`: `formulario` (480, por defecto), `amplio` (640) o `completo` (sin máximo; la pantalla con tabla va arriba). Bajo 900 px no hay imagen; bajo 600 px las cuadrículas y las filas pasan a una columna. **La página nunca se desplaza en horizontal**: la tabla se desplaza dentro de su contenedor.
- **JavaScript:** un único `wwwroot/js/app.js`: al enviar un formulario o pulsar un botón-enlace marca la pantalla como ocupada (controles deshabilitados e indicador visible), la libera al volver con «Atrás» y `Esc` activa `Cancelar`.
- **Presentación clara:** cambiar la clase del `<body>` en `_Layout.cshtml` a `tema-claro` (la imagen no se usa).
- Los textos con tildes, «…» y «→» salen sin entidades (`WebEncoderOptions` en `Aplicacion`); `<`, `>`, `&`, las comillas y «+» siguen escapándose.
- Antes de construir las pantallas de un proyecto conviene especificarlas en su `docs/PANTALLAS.md` (orden de los elementos, anchos, estados y decisiones).

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionServicio.UrlPorDefecto` (URL completa). Sin recompilar: la variable de entorno `WS_URL`. Con varios servicios, una por servicio: `WS_URL_<SERVICIO>` (§1, paso 4). En PowerShell: `$env:WS_URL_CONSULTA = 'http://host:5267/Consulta'` antes de `dotnet run`.
- **Servidor .NET local:** con el perfil `http` (Kestrel) el puerto por defecto es 5267; con `IIS Express`, 57592 (solo HTTP; ver `context/entornos/IIS_CONFIG.md`). Si el servidor corre en IIS Express, es Visual Studio quien lo mantiene vivo: si se detiene, el servidor está apagado.
- **Esta aplicación** escucha en `http://localhost:5193` (`Properties/launchSettings.json`, perfil `http`).
- **El WSDL está empaquetado** (`wsdl/`): crear el cliente no abre ninguna conexión, así que con el servidor SOAP apagado la aplicación arranca y el fallo llega al **llamar** una operación, donde `TraductorRed` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **Visual Studio:** *Run* ejecuta el proyecto; **detener la depuración antes de volver a compilar** (si no, `MSB3021`/`MSB3027`: el `.exe` queda bloqueado).

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor SOAP ni navegador)
dotnet build                 # compilar
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con la aplicación en marcha, en un navegador:
1. Operación correcta: el resultado se muestra con el formato esperado, y recargar la página (F5) no repite una operación que cambia datos.
2. Error de negocio: se muestra el mensaje del servidor y se conserva lo escrito.
3. Con el servidor SOAP apagado: aparece «Servidor no disponible, intente más tarde» y la aplicación sigue respondiendo.
4. Doble clic en una acción: se envía **una** petición (controles deshabilitados mientras espera).
5. Ventana ancha, intermedia (< 900 px) y estrecha (< 600 px); sin barra de desplazamiento horizontal; tildes y «ñ» en campos y mensajes; `Esc` como `Cancelar`.

Un agente que no pueda abrir un navegador debe decir explícitamente qué verificó (pruebas, recorrido en memoria) y que **no vio la página real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_SOAP.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `servicios`:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (`elementFormDefault="qualified"`): si la copia del esquema no lo declara, los campos llegan `null` sin error (R3).
- **Namespace (TNS) fijo `http://ws.monster.edu.ec/`** en todos los servicios del equipo (R2). **SOAPAction = `TNS + Servicio + "/" + operación`**, que viene en el WSDL (R4).
- **El cliente no depende del nombre del puerto ni del binding** (R10) y recibe la **URL completa** por configuración (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los campos propios nunca se llaman `estado`, `codigo` ni `mensaje`.
- Todos los datos viajan como texto (R7); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R8); los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5).
- Un WSDL de CoreWCF trae el esquema de serialización de Microsoft (`xsd1`) y una lista como clase que hereda de `List<T>`; un WSDL Java, un solo `.xsd`.
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (credenciales en cada operación, un token, nada) lo define cada servidor, y su cliente lo implementa en `servicios`.
- Para un servicio **REST** usar la plantilla `rest_dotnet_web`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| Un componente aparece como etiqueta sin resolver (`<ui-campo …>` en el HTML) | `@addTagHelper *, <Proyecto>` de `vista/_ViewImports.cshtml` no coincide con el nombre del ensamblado (§1, paso 2) |
| `The view '…' was not found` | La vista no está en `ec/edu/monster/vista/` o su nombre no coincide con el de `View("…")`: la aplicación solo busca allí (`ViewLocationFormats`) |
| El controlador no responde (404) | La clase no deriva de `ControladorBase` (el atributo `[Controller]` se hereda) o la acción no tiene atributo de ruta (`[HttpGet("…")]`) |
| `POST` con HTTP 400 | Falta el token anti-falsificación: el formulario debe ser un `<ui-formulario>` |
| Tildes o «…» salen como `&#xF3;` | El texto no pasa por el codificador de `Aplicacion` (no usar `Html.Raw` ni armar HTML a mano) |
| El resultado de una operación reaparece al recargar | El `POST` devolvió la página en vez de redirigir: usar `GuardarFlash` + `Redirect` (§4, paso 6) |
| `CommunicationException` / «Servidor no disponible» al llamar una operación | Servidor apagado (con IIS Express, Visual Studio detenido) o `UrlPorDefecto` distinta de la real. Abrir `<URL>?wsdl` en el navegador |
| `dotnet-svcutil` falla (`MSB3030`, rutas demasiado largas) | Generar en una carpeta de ruta corta (§4, paso 1) |
| `dotnet-svcutil` añade paquetes `System.ServiceModel.*` 4.10 a un proyecto | Se ejecutó dentro del proyecto: generar en una carpeta descartable y copiar solo los `Reference.cs` |
| 26 avisos `NU1902`/`NU1903` al restaurar | Dependencias transitivas de `System.ServiceModel.*`: fijar `System.Security.Cryptography.Pkcs` y `.Xml` en 10.0.14 en el `.csproj` (con esta plataforma web no aparecen) |
| Cambió el servidor y el cliente no ve la operación nueva | Guardar de nuevo el WSDL y los esquemas, regenerar el proxy y agregar la operación a la interfaz y al envoltorio (§4) |
| Respuesta con campos vacíos | Se guardó un WSDL antiguo, o el envoltorio no copia un campo de la respuesta generada al DTO propio |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | El WSDL guardado no trae el `soapAction` de la operación: guardar el WSDL del servidor actualizado y regenerar |
| El servidor no recibe los parámetros (llegan vacíos) | El esquema guardado no califica los elementos (`elementFormDefault="qualified"`) |
| `ListaX` no tiene `Length` | Es una lista (`List<T>`): usar `Count` |
| Errores por tipos duplicados (`RespuestaBase`…) | Varios servicios generados en el mismo espacio de nombres: darle a cada uno el suyo |
| Una prueba se cuelga | Un canal simulado cuyo `BeginClose` no invoca el callback: `Task.Factory.FromAsync` espera para siempre (ver `TraductorRedPruebas`) |
| `MSB3021`/`MSB3027`: no se puede copiar `apphost.exe` al compilar | La aplicación sigue en ejecución (p. ej. depurando en Visual Studio): detenerla antes de compilar |
