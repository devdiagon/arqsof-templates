# Plantilla: cliente web .NET (ASP.NET Core MVC) para servicios REST

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga una API **REST** con el esquema de respuesta de esta familia de proyectos (envoltura `estado` / `codigo` / `mensaje`). Trae el transporte HTTP, los tipos comunes, la verificación de la envoltura, el layout con sus estilos y los componentes de vista, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de dominio ni autenticación: se construyen con la receta de §4. Es la contraparte de `soap_dotnet_web`: todo es igual salvo la capa de protocolo (`ws` ↔ `api`).

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0` (`Nullable` e `ImplicitUsings` habilitados). **Plataforma:** ASP.NET Core 10 (Kestrel o IIS Express). **Interfaz:** controladores MVC + **vistas Razor** (`.cshtml`) escritas a código, sin librerías de UI (ni Bootstrap, ni jQuery, ni framework de JavaScript). **Arquitectura:** capas, con MVC.
- **Transporte:** `HttpClient` del SDK y `System.Text.Json`; ningún paquete extra. Un solo `HttpClient` de larga vida para toda la aplicación, con tiempo límite de 10 s. El SDK Web ya incluye `System.Net.Http` entre los `using` implícitos (el de WPF no).
- **El navegador no habla con la API:** quien llama al servidor es el controlador (el servidor del cliente web), nunca JavaScript. Por eso la API no necesita CORS (`ENDPOINTS_REST.md`).
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (presentación oscura, tipografía, medidas, componentes, disposición). Se aplica con **componentes reutilizables** (*tag helpers* `ui-*`) y un único `wwwroot/css/estilos.css`.
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`). El contrato concreto de cada proyecto (rutas, verbos, cuerpos, autenticación) va en su `docs/ENDPOINTS_REST.md`, que se **escribe o se copia del servidor antes de implementar**. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Cada proyecto define cómo viajan las credenciales o el token (R8, §8) y su sesión; la plantilla no trae ninguna pantalla ni estado de usuario.
- **Mapeo JSON ↔ DTO:** los DTO no llevan anotaciones; `api/TraductorApi` configura nombres en camelCase, lectura sin distinguir mayúsculas y `Populate` (`ARQUITECTURA_CLIENTE.md` §4.5).
- **Núcleo:** `dto`, `excepciones`, `servicios/ServicioBase` y `api` (`ConfiguracionApi`, `ITransporte`, `TransporteHttp`, `RespuestaHttp`, `TraductorApi`) son los mismos archivos que la plantilla `rest_dotnet_consola`: un proyecto con consola y web los copia **sin editarlos** (`COMPARTIR_CLIENTE.md`).
- **Verificada:** `dotnet test` → **110 pruebas** xUnit en verde, sin servidor REST ni navegador: configuración de la URL, armado y lectura de peticiones (`TraductorApi`, con todos los HTTP de error, `RED` y `SRV-000`), el transporte real contra un `HttpMessageHandler` simulado, verificación de la envoltura (`ServicioBase`), excepción del cliente, base de los controladores (`ControladorBase`), **cada componente `ui-*`** (HTML, clases y escape de todo texto dinámico) y **la aplicación arrancada dentro de la prueba** (`PantallasWebPruebas`: rutas, layout en sus dos modos, vistas Razor, tag helpers resueltos, recursos estáticos, página de error y token anti-falsificación). `dotnet build` sin errores ni advertencias. La capa `api` es la del cliente web .NET REST construido sobre esta base, probado a mano en un navegador contra su servidor.
  **No se ha hecho:** ver la plantilla en un navegador (el aspecto no se comprobó), una llamada a un servidor real (la plantilla no trae dominio ni clientes `…Api`), la presentación clara, `dotnet publish`, ni la ejecución desde Visual Studio con el perfil `IIS Express`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real, decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**.
2. **Renombrar** `rest_dotnet_web.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. **Corregir también `@addTagHelper *, rest_dotnet_web` en `ec/edu/monster/vista/_ViewImports.cshtml`** con el nuevo nombre del proyecto (es el nombre del ensamblado; si no coincide, los `<ui-…>` quedan sin resolver **sin error de compilación**). El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.api`…). Las carpetas de código van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); `wwwroot/` y `docs/` también; los archivos, en PascalCase.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10.
4. **Configurar el servidor:** en `api/ConfiguracionApi.cs`, `UrlBasePorDefecto` = **URL base completa de la API**, con host, puerto, contexto (si lo hay) y prefijo `api`, **con «/» final** (R1). Se sobrescribe sin recompilar con la variable de entorno `API_BASE_URL`. Las rutas de las operaciones se le añaden relativas y sin barra inicial.
5. **Copiar el contrato del servidor** a `docs/ENDPOINTS_REST.md` (una fila por operación).
6. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la pestaña) y, si se quiere, **sustituir la imagen** `wwwroot/img/fondo_app.png` (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6). La imagen es un **PNG** (admite transparencia); si se cambia por otra, **se conserva el nombre `fondo_app.png`** para no tocar el CSS. Una imagen horizontal se muestra compacta al 60 % de su contenedor (`--margen-imagen: 20%` en `estilos.css`).
7. **Completar `docs/PROJECT.md`** (viene como esqueleto) y mantenerlo al día.
8. Con el servidor en marcha, ejecutar (F5 en Visual Studio o `dotnet run`). Con la plantilla sin tocar abre la pantalla `Inicio`.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración de la URL** | `api/ConfiguracionApi` (`UrlBase()`, `NormalizarUrl`, `TiempoLimite`) | Solo `UrlBasePorDefecto` |
| **Transporte** | `api/ITransporte`, `api/TransporteHttp`, `api/RespuestaHttp` (un solo `HttpClient`; la cabecera `Authorization` se pone por petición; un HTTP de error no lanza excepción) | No |
| **Armado y lectura** | `api/TraductorApi` (`Codificar`, `Cuerpo`, `LlamarAsync<T>`, `Leer<T>`; opciones JSON; `RED` y `SRV-000`) | No |
| **Interfaz y cliente** | — (`api/IXCliente.cs` y `api/XClienteApi.cs`) | **Se crean** por proyecto, una firma por fila de `ENDPOINTS_REST.md` (§4, paso 1) |
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
docs/                           CLIENT_STYLES_DOTNET.md, CONVENCION_REST.md y CONVENCION_PRUEBAS.md (copias vigentes), ENDPOINTS_REST.md (del proyecto) y PROJECT.md (esqueleto)
README.md                       esta guía: el único documento que queda en la raíz
ec/edu/monster/
├── Aplicacion.cs               punto de composición (clientes REST, servicios, MVC)
├── modelo/                     datos propios del cliente que no son de una pantalla; vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── api/           ConfiguracionApi, ITransporte, TransporteHttp, RespuestaHttp, TraductorApi (+ IXCliente y XClienteApi del dominio)
├── controlador/   ControladorBase, InicioControlador, ErrorControlador (+ uno por pantalla)   rutas con atributos
├── vista/         _Layout, _ViewImports, _ViewStart, Inicio, Error, Textos, Modelos (+ una vista y un modelo por pantalla)
│   └── componentes/  Ui*TagHelper                         los componentes ui-*
└── pruebas/                                               xUnit + Moq, en el mismo proyecto (ver §4, paso 9)
```

Dependencias permitidas: `vista → controlador → servicios → api`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `api` ni `dto`**: recibe de su controlador un modelo con el texto ya listo para mostrar y usa `Textos`.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo `api` conoce HTTP y JSON. Si el contrato cambia solo se tocan `api`, `dto` y `servicios`. **Quien llama al servidor es el controlador**, nunca el navegador.

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET items` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Interfaz y cliente** — `api/IXCliente.cs` (interfaz propia con **un método por fila** de `ENDPOINTS_REST.md`, con el nombre de la operación, retorno `Task<DTO propio?>`) y `api/XClienteApi.cs`, que la implementa con `TraductorApi`. Lo único que ven el servicio y los controladores es la interfaz:
   ```csharp
   public sealed class XClienteApi : IXCliente
   {
       private readonly ITransporte _transporte;

       public XClienteApi(ITransporte transporte) { _transporte = transporte; }

       public static IXCliente Crear() => new XClienteApi(TransporteHttp.Crear());   // un solo HttpClient para toda la aplicación

       public async Task<RespuestaItemsDto?> ListarItemsAsync() =>
           await TraductorApi.LlamarAsync<RespuestaItemsDto>(_transporte, "GET", "items", null, null);

       public async Task<RespuestaItemDto?> ConsultarItemAsync(string codigo) =>
           await TraductorApi.LlamarAsync<RespuestaItemDto>(_transporte, "GET",
               "items/" + TraductorApi.Codificar(codigo), null, null);      // cada variable de ruta, codificada (R10)
   }
   ```
   Reglas de la capa `api`:
   - **Se lee la envoltura también con HTTP de error** (400, 401, 404, 422…): el `mensaje` del servidor nunca se descarta. **Nunca `EnsureSuccessStatusCode()`.**
   - Sin respuesta (servidor caído, red, tiempo agotado) → `RED`; cuerpo vacío o que no es JSON (página de un proxy, `502`) → `SRV-000` (R11, R12). `TraductorApi` ya lo hace.
   - Todo dato viaja como **texto** (R9); los decimales con punto. Un fallo de programación **no** se disfraza de `RED`.
2. **DTO propio** — clase `…Dto` en `dto` que extienda `RespuestaBaseDto`, con propiedades `{ get; set; }`, todas texto y `string?`; una lista se inicializa vacía y **sin setter** (`public List<string> Items { get; } = [];`): **nunca nula**. **Sin atributos de JSON**: el nombre JSON es el camelCase del nombre de la propiedad (`CodigoItem` ↔ `codigoItem`); `Populate` (en `TraductorApi`) rellena la lista de solo lectura.
3. **Servicio** — `servicios/XServicio : ServicioBase` (con sufijo `Async` en lo que llama a la red; los métodos públicos, `virtual`: Moq los necesita):
   ```csharp
   public virtual async Task<IReadOnlyList<string>> ItemsAsync()
   {
       RespuestaItemsDto respuesta = Verificar(await _cliente.ListarItemsAsync());
       return respuesta.Items;
   }
   ```
   `Verificar(...)` devuelve la respuesta si `EsOk`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`. **No captura excepciones de red** (ya llegan como `RED`). Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante). Las reglas del dominio las decide el servidor y el cliente muestra su mensaje.
4. **Modelo de la pantalla** — un `record` en `vista/Modelos.cs` con el texto ya listo para mostrar: lo escrito, el mensaje, el campo con error y, si hay resultado, sus datos. Es lo único que la vista recibe.
5. **Controlador** — `controlador/XControlador : ControladorBase`, con las rutas por atributos (`[HttpGet("consulta")]`, `[HttpPost("consulta")]`) y el servicio por constructor (`ILogger<T>` para registrar lo inesperado). Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar los campos vacíos **antes** de llamar, en el orden de la pantalla (se informa el primero); mostrar solo el mensaje del error, nunca el código; conservar lo escrito ante un error salvo las contraseñas; no conocer la pantalla siguiente:
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
6. **Vista** — `vista/Consulta.cshtml` (`@model` del paso 4), solo `ui-*`: sin `class`, `style` ni lógica. `ViewData["Titulo"]` (obligatorio), `["Modo"]` y `["Ancho"]` dan la disposición. Patrón de toda pantalla: `<ui-formulario accion="/consulta" ocupado="@Textos.Consultando">` con `<ui-encabezado>`, los `<ui-campo>` (con `invalido` y `autofocus`), `<ui-fila-botones>` (el principal y `Cancelar` con `cancelar="true"`) y `<ui-mensaje mensaje="@Model.Error">`; el resultado (`<ui-tarjetas>`, `<ui-tabla>`) va **después** del formulario. Ver `Inicio.cshtml` y `docs/CLIENT_STYLES_DOTNET.md` §5 y §7.2.
7. **Registro** — en `Aplicacion.Construir`: `AddSingleton(_ => XClienteApi.Crear())` y `AddSingleton<XServicio>()`. La ruta ya la declara el controlador.
8. **Autenticación (si el proyecto la tiene)** — se define en `docs/ENDPOINTS_REST.md` (R8) y se implementa **solo en `api`**: las credenciales siguen siendo parámetros de la interfaz del cliente y `XClienteApi` las coloca donde el contrato diga (p. ej. `Authorization: Basic` por petición: `"Basic " + Convert.ToBase64String(Encoding.UTF8.GetBytes(usuario + ":" + clave))`, pasado como último argumento de `TraductorApi.LlamarAsync`). Nunca en la URL, nunca en las cabeceras por defecto del `HttpClient`, nunca en un registro. **El `HttpClient` y los clientes `…Api` son únicos para toda la aplicación y no guardan credenciales**; las de cada empleado viven en el servidor web, en un contexto por sesión de servidor (`AddSession` con cookie de solo un identificador), nunca en un objeto compartido: de lo contrario el segundo empleado en entrar reemplazaría las credenciales del primero. Nada de esto está en la plantilla.
9. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto).
   - **Clientes `…Api`:** con `TransporteFalso` (en `pruebas/`) se comprueba el verbo, la ruta, el cuerpo y la cabecera de cada método, y que un HTTP de error llega como DTO y no como excepción.
   - **Pantallas:** `ServidorWeb` arranca la aplicación en la prueba, en un puerto libre; `Aplicacion.Construir(args, servicios => …)` permite sustituir el cliente REST por uno simulado (`RemoveAll<IXCliente>()` y `AddSingleton(simulado)`). Se recorre cada ruta con `HttpClient` (`AllowAutoRedirect = false`): éxito, campos vacíos, error de negocio, redirección y resultado de una sola lectura. Un `POST` necesita el token anti-falsificación: se toma del formulario de la página (`__RequestVerificationToken`) y las cookies del mismo `HttpClient`. Revisar también que no quede ningún `<ui-` sin resolver.
   - El contrato **no** se prueba con código: se verifica a mano contra `docs/ENDPOINTS_REST.md` con `docs/CONVENCION_PRUEBAS.md` §6.3.

Reglas que se repiten en todo dominio:
- **Un error de negocio llega con HTTP 4xx o 5xx y con la envoltura en el cuerpo**; el cliente decide por el `codigo` y muestra el `mensaje`. Solo red, tiempo agotado y cuerpo ilegible son `RED` y `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- Un fallo de red no cierra la aplicación: se muestra el mensaje en la zona de estado de la pantalla.
- `ERR-404`, `ERR-405` y `ERR-415` (errores de plataforma) se muestran como cualquier otro error, pero indican un defecto del cliente (ruta, verbo o `Content-Type`).
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

- **Servidor por defecto:** `ConfiguracionApi.UrlBasePorDefecto` (URL base completa, con «/» final). Sin recompilar: la variable de entorno `API_BASE_URL`. En PowerShell: `$env:API_BASE_URL = 'http://host:5267/api/'` antes de `dotnet run`.
- **Servidor .NET local:** con el perfil `http` (Kestrel) el puerto por defecto es 5267; con `IIS Express`, 57592 (solo HTTP; ver `context/entornos/IIS_CONFIG.md`). Si el servidor corre en IIS Express, es Visual Studio quien lo mantiene vivo.
- **Esta aplicación** escucha en `http://localhost:5193` (`Properties/launchSettings.json`, perfil `http`).
- **Crear el cliente no abre ninguna conexión**: con el servidor apagado la aplicación arranca y el fallo llega al **llamar** una operación, donde `TraductorApi` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **Visual Studio:** *Run* ejecuta el proyecto; **detener la depuración antes de volver a compilar** (si no, `MSB3021`/`MSB3027`: el `.exe` queda bloqueado).

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor REST ni navegador)
dotnet build                 # compilar
dotnet run                   # ejecutar contra el servidor configurado
```

Verificación con la aplicación en marcha, en un navegador:
1. Operación correcta: el resultado se muestra con el formato esperado, y recargar la página (F5) no repite una operación que cambia datos.
2. Error de negocio: se muestra el mensaje del servidor y se conserva lo escrito.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y la aplicación sigue respondiendo.
4. Doble clic en una acción: se envía **una** petición (controles deshabilitados mientras espera).
5. Ventana ancha, intermedia (< 900 px) y estrecha (< 600 px); sin barra de desplazamiento horizontal; tildes y «ñ» en campos y mensajes; `Esc` como `Cancelar`.
6. Si el proyecto tiene login: dos empleados a la vez desde navegadores distintos no se mezclan, y credenciales inválidas muestran el mensaje del servidor y no inician sesión.

Un agente que no pueda abrir un navegador debe decir explícitamente qué verificó (pruebas, recorrido en memoria) y que **no vio la página real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- **URL base** con prefijo `api` y «/» final (R1); rutas relativas y sin barra inicial.
- **JSON UTF-8** en peticiones y respuestas (R3); el cuerpo de las peticiones lleva `Content-Type: application/json`, o el servidor responde `ERR-415`.
- **Toda respuesta, de éxito o de error, lleva la envoltura** `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación (R5). Los campos propios nunca se llaman `estado`, `codigo` ni `mensaje`. Las listas llegan vacías, nunca ausentes.
- **HTTP por categoría del error** (R7): el cliente decide por el `codigo` de la envoltura, no solo por el HTTP.
- **Errores de plataforma** (ruta inexistente, verbo no permitido, `Content-Type` incorrecto, JSON mal formado, excepción no prevista) también con la envoltura (R6).
- **Todo dato como texto** (R9): las respuestas nunca llevan números JSON.
- **Autenticación:** no la asume la plantilla (R8). Cómo se identifica quien opera lo define cada servidor en su `ENDPOINTS_REST.md` y su cliente lo implementa en `api`, con la sesión en el servidor web.
- Para un servicio **SOAP** usar la plantilla `soap_dotnet_web`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| Un componente aparece como etiqueta sin resolver (`<ui-campo …>` en el HTML) | `@addTagHelper *, <Proyecto>` de `vista/_ViewImports.cshtml` no coincide con el nombre del ensamblado (§1, paso 2) |
| `The view '…' was not found` | La vista no está en `ec/edu/monster/vista/` o su nombre no coincide con el de `View("…")`: la aplicación solo busca allí (`ViewLocationFormats`) |
| El controlador no responde (404) | La clase no deriva de `ControladorBase` (el atributo `[Controller]` se hereda) o la acción no tiene atributo de ruta (`[HttpGet("…")]`) |
| `POST` con HTTP 400 | Falta el token anti-falsificación: el formulario debe ser un `<ui-formulario>` |
| Tildes o «…» salen como `&#xF3;` | El texto no pasa por el codificador de `Aplicacion` (no usar `Html.Raw` ni armar HTML a mano) |
| El resultado de una operación reaparece al recargar | El `POST` devolvió la página en vez de redirigir: usar `GuardarFlash` + `Redirect` (§4, paso 5) |
| «Servidor no disponible» al llamar una operación | Servidor apagado (con IIS Express, Visual Studio detenido) o `UrlBasePorDefecto` distinta de la real. Probar la URL en el navegador o con `curl` |
| `SRV-000 … (HTTP 404)` con una página HTML | La URL base no incluye el prefijo `api` o el contexto del servidor; revisar `ConfiguracionApi` |
| `ERR-404` / `ERR-405` / `ERR-415` en pantalla | Defecto del cliente: ruta mal escrita, verbo equivocado o falta el `Content-Type` (el transporte lo envía en todo cuerpo) |
| Un campo llega `null` aunque el servidor lo envía | El nombre de la propiedad del DTO no es el camelCase del JSON (`CodigoItem` ↔ `codigoItem`) |
| Una lista del DTO llega siempre vacía | Debe ser `{ get; } = []` y se rellena con `Populate` (no quitarlo de `TraductorApi.Opciones`) |
| El servidor rechaza un importe con coma | La coma se normaliza a punto en el **servicio**, no en la vista |
| Dos empleados ven las credenciales del otro | Las credenciales están en un objeto compartido (singleton) en vez de en un contexto por sesión de servidor (§4, paso 8) |
| `MSB3021`/`MSB3027`: no se puede copiar `apphost.exe` al compilar | La aplicación sigue en ejecución (p. ej. depurando en Visual Studio): detenerla antes de compilar |
