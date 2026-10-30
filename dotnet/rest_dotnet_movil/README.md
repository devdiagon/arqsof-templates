# Plantilla: cliente móvil .NET (MAUI) para servicios REST

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga una API **REST** con el esquema de respuesta de esta familia de proyectos (envoltura `estado` / `codigo` / `mensaje`). Es la plantilla **mínima**: trae el transporte HTTP (con la URL por plataforma), los tipos comunes, la verificación de la envoltura, el molde de los controladores, los estilos y los componentes visuales base, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4. Es la contraparte de `soap_dotnet_movil`: todo es igual salvo la capa de protocolo (`ws` ↔ `api`).

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0-android` y `net10.0-windows10.0.19041.0` (`Nullable` e `ImplicitUsings` habilitados). **Interfaz:** .NET MAUI (XAML), una sola página que aloja la pantalla actual (sin `Shell`), sin librerías de terceros. **Arquitectura:** capas con MVVM (el controlador hace de ViewModel), sin frameworks.
- **Transporte:** `HttpClient` del SDK y `System.Text.Json`; ningún paquete extra. Un solo `HttpClient` de larga vida, con tiempo límite de 10 s. `api/ConfiguracionApi` fija la URL base completa **con el host de cada plataforma**. El SDK de MAUI ya incluye `System.Net.Http` entre los `using` implícitos.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (presentación **oscura**, tipografía, medidas, componentes). Implementado en `Resources/Styles/Colors.xaml` y `Styles.xaml` y en los componentes `CampoTexto` y `ZonaEstado`.
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`). El contrato concreto de cada proyecto (rutas, verbos, cuerpos, autenticación) va en su `docs/ENDPOINTS_REST.md`, que se **escribe o se copia del servidor antes de implementar**. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación y sesión:** **fuera de la plantilla base.** Cada proyecto define su mecanismo (R8, §8) y su sesión (en `modelo`); la plantilla no trae ninguno ni decide cómo se maneja una sesión no válida.
- **Mapeo JSON ↔ DTO:** los DTO no llevan anotaciones; `api/TraductorApi` configura nombres en camelCase, lectura sin distinguir mayúsculas y `Populate` (`ARQUITECTURA_CLIENTE.md` §4.5).
- **Núcleo:** `dto`, `excepciones`, `servicios/ServicioBase` y `api` (`ITransporte`, `TransporteHttp`, `RespuestaHttp`, `TraductorApi`) son los mismos archivos que la plantilla `rest_dotnet_consola`. **La única pieza de `api` que difiere de la consola es `ConfiguracionApi`**, reescrita para el móvil (§6; `ARQUITECTURA_CLIENTE.md` §4.9).
- **Verificada:** `dotnet test -f net10.0-windows10.0.19041.0` → **75 pruebas** xUnit en verde, sin servidor: configuración de la URL por plataforma y de la variable de entorno, armado y lectura de peticiones (`TraductorApi`, con todos los HTTP de error, `RED` y `SRV-000`), el transporte real contra un `HttpMessageHandler` simulado, verificación de la envoltura (`ServicioBase`), excepción del cliente y molde de los controladores (`ControladorBase`). `dotnet build` de **los dos destinos** (`net10.0-android` y `net10.0-windows10.0.19041.0`) sin errores ni advertencias, compilando desde una ruta corta. La capa `api` y la `ConfiguracionApi` son las del cliente móvil .NET REST construido sobre esta base, probado a mano en un teléfono físico contra su servidor.
  **No se ha ejecutado:** la plantilla en un emulador o teléfono (no trae dominio ni pantallas: la página queda vacía sobre el color de fondo). **(No verificado)** en Release: el recorte de código de MAUI puede afectar a la serialización por reflexión de `System.Text.Json`; probar los pasos 1 y 2 de §7 en Release antes de dar por bueno un proyecto.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real, decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta**: `aapt2` (Android) y el compilador XAML de WinUI fallan con rutas largas (§9).
2. **Renombrar** `rest_dotnet_movil.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.api`…). Las carpetas van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); los archivos, en PascalCase.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 con las cargas de trabajo `android` y `maui-windows`, el SDK de Android (con un emulador) y el JDK; la primera vez restaura los paquetes (Internet).
4. **Nombre de la aplicación:** `ApplicationTitle` en el `.csproj` (nombre visible en el dispositivo) y `vista/Textos.cs` → `NombreAplicacion`; mantenerlos iguales.
   **Identificador del paquete:** `ApplicationId` en el `.csproj` (`ec.edu.monster.movil` de la plantilla). Android trata un `ApplicationId` distinto como otra aplicación: desinstalar la anterior del dispositivo.
5. **Configurar el servidor:** en `api/ConfiguracionApi.cs`, `Puerto` (5267 con Kestrel, 57592 con IIS Express, 8080 con Payara), `Ruta` (el prefijo de la API; `api` por convención, o `Contexto/api` en un servidor Java) e `IpPc` (solo para un teléfono físico: la IP del PC en la red; la plantilla trae el marcador `127.0.0.1`). La URL base completa se arma con el host de cada plataforma: `10.0.2.2` en el emulador, `IpPc` en un teléfono físico y `localhost` en Windows (§6). Las pruebas no fijan `Puerto` ni `IpPc`: se cambian sin tocarlas.
6. **Copiar el contrato del servidor** a `docs/ENDPOINTS_REST.md` (una fila por operación).
7. **Sustituir la imagen** `Resources/Images/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6), o quitar el `Image` de `PaginaPrincipal.xaml` y de la pantalla de acceso para no usarla.
8. **Construir las pantallas del dominio** (§4).
9. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
10. **Probar el servidor** (la URL de una operación `GET` abre en el navegador del PC y del dispositivo, §6) y ejecutar en el emulador. Con la plantilla sin tocar abre una página vacía sobre el color de fondo.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración de la URL** | `api/ConfiguracionApi` (`Puerto`, `IpPc`, `Ruta`, host por plataforma con `DeviceInfo`, `UrlBase()`, normalización; nada del dominio) | `Puerto`, `Ruta` e `IpPc` siempre; el resto no |
| **Transporte** | `api/ITransporte`, `api/TransporteHttp`, `api/RespuestaHttp` (un solo `HttpClient`; la cabecera `Authorization` se pone por petición; un HTTP de error no lanza excepción) | No |
| **Armado y lectura** | `api/TraductorApi` (`Codificar`, `Cuerpo`, `LlamarAsync<T>`, `Leer<T>`; opciones JSON; `RED` y `SRV-000`) | No |
| **Interfaz y cliente** | — (`api/IXCliente.cs` y `api/XClienteApi.cs`) | **Se crean** por proyecto, una firma por fila de `ENDPOINTS_REST.md` (§4, paso 1) |
| **DTO base** | `dto/RespuestaBaseDto` (`Estado`, `Codigo`, `Mensaje`, `EsOk`) | No; se agregan DTO del dominio al lado |
| **Verificación de la envoltura** | `servicios/ServicioBase` (`Verificar`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (código + mensaje; códigos propios `RED` y `SRV-000`) | No |
| **Molde MVVM** | `controlador/ControladorBase` (`Estado` de la zona, `Ocupado`, `Mensaje`, `EjecutarAsync`, `Manejar`, `FocoSolicitado`), `Comando` | No; los controladores del dominio la extienden |
| **Página y componentes** | `vista/PaginaPrincipal` (aloja la pantalla actual y la imagen de fondo), `vista/Pantalla`, `vista/CampoTexto`, `vista/ZonaEstado` | `Pantalla` crece con las pantallas del dominio; el resto no |
| **Estilos** | `Resources/Styles/Colors.xaml` (tokens), `Styles.xaml` (estilos), `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` y los textos del dominio |
| **Composición** | `App.xaml(.cs)` (tema oscuro fijo; `Componer` con el `TODO` del montaje; `Ir`), `MauiProgram.cs` (subrayado del `Entry` en Android) | `Componer` se completa con los clientes, servicios, controladores y vistas del dominio |
| **Plataforma Android** | `Platforms/Android/AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`, `usesCleartextTraffic`), `colors.xml` (colores de la barra) | `usesCleartextTraffic` solo se quita si el servidor usa HTTPS |
| **Pruebas** | `pruebas/` (URL por plataforma, transporte, `TraductorApi`, `ServicioBase`, excepción, `ControladorBase`) | Se conservan; se agregan las del dominio |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" ec App.xaml.cs rest_dotnet_movil.csproj`).

## 3. Estructura

```
App.xaml(.cs), MauiProgram.cs   punto de composición (clientes, servicios, controladores, vistas y navegación) y tema; sin Shell
Platforms/                      Android (manifiesto, actividad) y Windows
Resources/                      Styles/ (Colors.xaml, Styles.xaml), Images/fondo_app.png, AppIcon/, Splash/, Raw/
ec/edu/monster/
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── api/           ConfiguracionApi (propia del móvil), ITransporte, TransporteHttp, RespuestaHttp, TraductorApi (+ IXCliente y XClienteApi del dominio)
├── controlador/   ControladorBase, Comando (+ los del dominio)   ViewModel de cada pantalla: estado, comandos y llamadas al servicio
├── vista/         PaginaPrincipal, Pantalla, CampoTexto, ZonaEstado, Textos (+ las vistas del dominio)   única capa que conoce XAML y MAUI
└── pruebas/       xUnit + Moq, en el mismo proyecto (solo se compilan en el destino de Windows)
docs/                           CLIENT_STYLES_DOTNET.md, CONVENCION_REST.md y CONVENCION_PRUEBAS.md (copias vigentes), ENDPOINTS_REST.md (del proyecto) y PROJECT.md (esqueleto)
README.md                       esta guía: el único documento que queda en la raíz
```

Dependencias permitidas: `vista → controlador → servicios → api`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `api` ni `dto`**: solo conoce a su controlador (su `BindingContext`), a `Textos` y a los estilos.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo `api` conoce HTTP y JSON. Si el contrato cambia solo se tocan `api`, `dto` y `servicios`. Los controladores nunca importan `api` ni `Microsoft.Maui`: hablan con los servicios y exponen propiedades y comandos; el código subyacente de una vista solo hace tareas de plataforma (foco y teclado).

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas`. Solo se compilan (y sus paquetes solo se agregan) en el destino de Windows: un proyecto Android no se ejecuta con `dotnet test`, no deben empaquetarse en el APK y el SDK de pruebas rompe la compilación de Android (`XAPRAS7009`). `GenerateProgramFile=false` (solo en Windows) porque el punto de entrada lo genera MAUI.
> **Las vistas y la página no se prueban con xUnit** (necesitan la aplicación en ejecución); se verifican a mano (§7).

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET items` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Interfaz y cliente** — `api/IXCliente.cs` (interfaz propia con **un método por fila** de `ENDPOINTS_REST.md`, con el nombre de la operación, retorno `Task<DTO propio?>`) y `api/XClienteApi.cs`, que la implementa con `TraductorApi`. Lo único que ven el servicio y las vistas es la interfaz:
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
2. **DTO propio** — clase `…Dto` en `dto` que extienda `RespuestaBaseDto`, con propiedades `{ get; set; }`, todas texto y `string?` (los campos solo existen si hubo éxito); una lista se inicializa vacía y **sin setter** (`public List<string> Items { get; } = [];`): **nunca nula**. **Sin atributos de JSON**: el nombre JSON es el camelCase del nombre de la propiedad (`CodigoItem` ↔ `codigoItem`); `Populate` (en `TraductorApi`) rellena la lista de solo lectura.
3. **Servicio** — `servicios/XServicio : ServicioBase` (con sufijo `Async` en lo que llama a la red; los métodos públicos, `virtual`: Moq los necesita):
   ```csharp
   public virtual async Task<IReadOnlyList<string>> ItemsAsync()
   {
       RespuestaItemsDto respuesta = Verificar(await _cliente.ListarItemsAsync());
       return respuesta.Items;
   }
   ```
   - `Verificar(...)` devuelve la respuesta si `EsOk`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`.
   - **No captura excepciones de red**: ya llegan traducidas a `RED` desde `api`.
   - Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante: el teclado numérico puede ofrecer la coma) y exigir la sesión si el proyecto tiene login. Las reglas del dominio las decide el servidor y el cliente muestra su mensaje.
4. **Controlador** — `controlador/XControlador : ControladorBase` (ViewModel de la pantalla):
   - Expone propiedades de entrada (`Asignar(ref campo, valor)` notifica el cambio), una propiedad `…Invalido` por campo y los comandos (`new Comando(() => _ = AceptarAsync())`). Recibe el servicio y la navegación como **delegados** (`Action`): los controladores no se conocen entre sí ni conocen la pantalla siguiente.
   - Valida campos vacíos **antes** de llamar (`Vacio(...)`; si faltan, no se llama al servicio): marca el primero vacío, `MostrarError(Textos.CampoObligatorio(...))` y `PedirFoco(...)`.
   - Llama con `EjecutarAsync(textoOcupado, () => servicio.OperacionAsync(...), alExito)`: la pantalla queda `Ocupado` (todos sus controles deshabilitados), una segunda acción se ignora, un `ExcepcionServicio` se muestra con `Manejar` (**solo el mensaje**, nunca el código) y cualquier otra excepción con el texto genérico de `Textos`. Un fallo de red conserva lo que el usuario capturó (salvo los datos sensibles).
   - `AlMostrar()` deja la pantalla limpia cada vez que se muestra; `OcultarResultado()` y `LimpiarMarcas()` se sobrescriben para limpiar lo propio de la pantalla.
   - Si el proyecto define una sesión, `Manejar` se sobrescribe para tratar ahí el código de sesión no válida (la plantilla no define ninguna).
5. **Vista** — `vista/XVista.xaml` (`ContentView`) **sin lógica**, construida solo con los estilos de `Styles.xaml` (§5) y con el controlador como `BindingContext`:
   - Raíz: un `Grid` con `IsEnabled="{Binding NoOcupado}"`; dentro, un `VerticalStackLayout`. Panel de encabezado (`PanelSobreImagenEstilo`, o `PanelSuperficie` en el acceso), etiquetas (`EtiquetaCampo`), campos `<vista:CampoTexto Text="{Binding X, Mode=TwoWay}" Invalido="{Binding XInvalido}" />` (`EsClave="True"` para una clave, `Teclado="Numeric"` para un importe), botones apilados (el principal y `Cancelar` debajo, separados 16, `HorizontalOptions="Center"`) y, debajo, `<vista:ZonaEstado Fondo="{StaticResource PanelSobreImagen}" />` (`Superficie` en el acceso).
   - Un resultado que cambia un valor se dibuja con una tarjeta (estilos `Tarjeta*`, ancho completo, `BindableLayout`); una lista de registros, con **una tarjeta compacta por registro** dentro del mismo `ScrollView` (no un `CollectionView`); un menú, con botones `BotonMenu` de 96 de alto (`docs/CLIENT_STYLES_DOTNET.md` §5.11 a §5.13 y §7.1).
   - El código subyacente solo lleva el foco (`controlador.FocoSolicitado += campo => …Enfocar()`) y oculta el teclado cuando la pantalla pasa a ocupada (`Desenfocar()` de cada `CampoTexto`).
6. **Composición y navegación** — en `App.xaml.cs`, `Componer`: crear los clientes (`XClienteApi.Crear()`), los servicios, los controladores y las vistas; registrarlos en `_pantallas` (un miembro de `Pantalla` por pantalla) y mostrar la primera con `Ir(...)`. La navegación se decide aquí: cada controlador avisa con un delegado y `Ir` limpia el controlador de destino (`AlMostrar()`) y lo muestra en `PaginaPrincipal`. Ajustar `EsAcceso` y `VaArriba` en `vista/Pantalla.cs`.
7. **Autenticación (si el proyecto la tiene)** — se define en `docs/ENDPOINTS_REST.md` (R8) y se implementa **solo en `api`**: las credenciales siguen siendo parámetros de la interfaz del cliente y `XClienteApi` las coloca donde el contrato diga (p. ej. `Authorization: Basic` por petición: `"Basic " + Convert.ToBase64String(Encoding.UTF8.GetBytes(usuario + ":" + clave))`, pasado como último argumento de `TraductorApi.LlamarAsync`). Nunca en la URL, nunca en las cabeceras por defecto del `HttpClient`, nunca en un registro. La sesión (usuario y clave en memoria) vive en `modelo`, es **una por aplicación** (hay un solo empleado por teléfono) y se limpia al cerrar sesión o ante una sesión no válida.
8. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto, sin sesión).
   - **Clientes `…Api`:** con `TransporteFalso` (en `pruebas/`) se comprueba el verbo, la ruta, el cuerpo y la cabecera de cada método, y que un HTTP de error llega como DTO y no como excepción.
   - **Controladores:** el servicio concreto se sustituye con Moq (sus métodos públicos son `virtual`; se crea con `new Mock<XServicio>(null!)`, los constructores solo asignan campos). Se prueban la validación de vacíos, el mensaje sin código, el estado ocupado y la navegación por delegados. Molde: `ControladorBasePruebas`.
   - El contrato **no** se prueba con código: se verifica a mano contra `docs/ENDPOINTS_REST.md` con `docs/CONVENCION_PRUEBAS.md` §6.3.
   - La vista se verifica a mano (§7): un recurso mal escrito en un XAML puede **compilar** y fallar al abrir la pantalla.

Reglas que se repiten en todo dominio:
- **Un error de negocio llega con HTTP 4xx o 5xx y con la envoltura en el cuerpo**; el cliente decide por el `codigo` y muestra el `mensaje`. Solo red, tiempo agotado y cuerpo ilegible son `RED` y `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio; nunca `double.Parse` con la cultura actual).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de la interfaz:** `async/await` con las operaciones `…Async`. Un fallo de red no cierra la aplicación.
- `ERR-404`, `ERR-405` y `ERR-415` (errores de plataforma) se muestran como cualquier otro error, pero indican un defecto del cliente (ruta, verbo o `Content-Type`).
- Una clave no se enlaza de vuelta ni se registra: se limpia ante un error.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo referencian recursos** (`{StaticResource …}`); no colorean ni dimensionan componentes sueltos. `App.xaml` carga `Colors.xaml` y `Styles.xaml`.

| Recurso | Para qué |
|---|---|
| `PaginaPrincipal` (componente) | Aloja la pantalla actual en un `ScrollView`; la imagen `fondo_app.png` es el fondo fijo (compacta, `AspectFit`, al 40 %) fuera del acceso |
| `BotonPrimario` / `BotonMenu` | Acción principal de la pantalla (relleno `Primario`, texto blanco); `BotonMenu` es el de 96 de alto de un menú |
| `BotonSecundario` | Salida o regreso: `Cancelar`, cerrar, volver (sólido `Secundario`) |
| `PanelSuperficie` / `PanelSobreImagenEstilo` + `TituloAplicacion` / `TituloPantalla` | Encabezado: título de la aplicación (acceso, centrado) y de pantalla |
| `EtiquetaCampo` + `CampoTexto` (componente) | Etiqueta de campo y campo (`Border` con `Entry` transparente; foco y error cambian el borde) |
| `ZonaEstado` (componente) | Un solo lugar bajo los botones: ocupado, error o éxito; con barra izquierda de 4; oculto si no hay nada |
| `Tarjeta`, `TarjetaTitulo`, `TarjetaTexto`, `TarjetaPie`, `TarjetaDivisor` | Tarjeta de resultado (ancho completo) y base de la tarjeta compacta de un registro |
| `TextoMensaje`, `TextoAyuda` | Mensajes y ayudas |
| `ActivityIndicator` | Indicador de carga; la pantalla se deshabilita mientras `Ocupado` |

- **Presentación oscura** fija (`UserAppTheme = Dark` en `App.xaml.cs`); no hay selector. Para la presentación clara, cambiar los valores de `Colors.xaml` por los de `docs/CLIENT_STYLES_DOTNET.md` §2.3 (y no usar imagen de fondo).
- **Sin fuentes propias:** se usa la del sistema (Roboto en Android). No agregar `OpenSans` ni la paleta de ejemplo de MAUI.
- **Disposición:** una sola columna; márgenes de 24, controles de 48 de alto mínimo, radio 8, sin sombras. Fuera del acceso los paneles usan `PanelSobreImagen` porque hay imagen debajo.
- **La imagen** va en `Resources/Images/` (MAUI la convierte a recurso de cada plataforma; nombre en minúsculas, sin espacios). Es horizontal y con transparencia: se muestra compacta (60 %). Si falta, la pantalla queda con el color `Fondo` plano.

## 6. Servidor, emulador y teléfono real

`ConfiguracionApi.UrlBase()` arma `http://<host>:<Puerto>/<Ruta>/` con el host de la plataforma. Sin recompilar: la variable de entorno `API_BASE_URL` (solo útil en el destino de Windows: Android no tiene variables de entorno).

| Entorno | Host | Notas |
|---|---|---|
| Emulador de Android | `10.0.2.2` (el PC anfitrión; **no** `localhost`) | automático (`DeviceType.Virtual`) |
| Teléfono físico | `IpPc` (IPv4 del PC en la red local, `ipconfig`) | misma Wi-Fi; la IP cambia al cambiar de red; recompilar al cambiarla |
| Windows | `localhost` | |

- **HTTP sin cifrar:** `android:usesCleartextTraffic="true"` en el manifiesto (ya está) y los permisos `INTERNET`/`ACCESS_NETWORK_STATE`. Si el servidor pasa a HTTPS, quitar el primero y asegurar que el dispositivo confía en el certificado del servidor (el certificado de desarrollo de `dotnet dev-certs` es de confianza solo en el PC). *HTTPS no se probó en esta plantilla.*
- **El servidor debe escuchar fuera de `localhost`:** con Kestrel, perfil `http` con `applicationUrl` `http://0.0.0.0:<puerto>`; con IIS Express, un binding comodín (`*:<puerto>:*` en el `applicationhost.config` **del proyecto del servidor**), Visual Studio como administrador (o `netsh http add urlacl url=http://*:<puerto>/ user=Todos`) y la regla de firewall (`context/entornos/IIS_CONFIG.md` §3).
- **Firewall de Windows** (PowerShell como administrador): `netsh advfirewall firewall add rule name="Servidor REST" dir=in action=allow protocol=TCP localport=<puerto>`; la red debe estar en categoría **Privada**.
- **Probar antes de tocar la app:** abrir en el navegador del emulador (`10.0.2.2`) o del teléfono (IP del PC) una URL `GET` de la API, por ejemplo `http://<host>:<puerto>/<Ruta>/<recurso>`. Si llega la envoltura JSON (aunque sea de error), la app también conectará. `400 Invalid Hostname` indica un IIS Express sin el binding comodín.
- **Con el servidor apagado** la aplicación arranca igual y la llamada falla con `RED` tras el tiempo límite (10 s), que `TraductorApi` traduce a «Servidor no disponible, intente más tarde».
- **HTTP y HTTPS:** el esquema de la URL base decide si se usa TLS; la validación del certificado **no** se desactiva.

## 7. Comandos y verificación

```
dotnet build -f net10.0-android                          # compilar para Android
dotnet test -f net10.0-windows10.0.19041.0               # pruebas locales (sin servidor ni interacción)
dotnet build -f net10.0-android -t:Run                   # compilar, instalar y abrir en el emulador o teléfono conectado
dotnet build -c Release -f net10.0-android -t:Run -p:AndroidPackageFormat=apk   # lo mismo en Release (con recorte)
```
Usar `dotnet restore` **sin** `-f` (con `-f` falla `NETSDK1005`). En Visual Studio: elegir el emulador y *Run* (F5). Si el emulador no se ve, `adb devices` debe listarlo (`XA0010: No hay ningún dispositivo disponible` es que no hay ninguno).

Verificación con la aplicación en marcha (las vistas se revisan a mano; los controladores, con pruebas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje en la zona de estado y la pantalla sigue usable.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» (≈ 10 s) y la aplicación sigue respondiendo.
4. Un campo vacío muestra el mensaje y lleva el foco al primero vacío; mientras hay una llamada en curso, toda la pantalla (incluido `Cancelar`) queda deshabilitada y el teclado se oculta.
5. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.
6. Tildes y «ñ» en pantalla; el teclado numérico acepta la coma y el signo menos.
7. **Repetir los puntos 1 y 2 en Release** (el recorte de código puede romper enlaces por reflexión o la serialización de `System.Text.Json`, y no falla en Debug). **No verificado** en esta plantilla.
8. En teléfono físico: `IpPc`, firewall y red Privada.

Un agente que no pueda abrir el emulador debe decir explícitamente qué verificó (pruebas, compilación, arranque, contra qué servidor) y que **no vio la aplicación real**.
Con `adb` se puede automatizar el recorrido (`adb shell input tap/text`, `adb exec-out screencap -p`, `adb shell uiautomator dump`); en Git Bash definir `MSYS_NO_PATHCONV=1` para que no convierta rutas como `/sdcard`.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- **URL base** con prefijo `api` y «/» final (R1); rutas relativas y sin barra inicial.
- **JSON UTF-8** en peticiones y respuestas (R3); el cuerpo de las peticiones lleva `Content-Type: application/json`, o el servidor responde `ERR-415`.
- **Toda respuesta, de éxito o de error, lleva la envoltura** `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación (R5). Los campos propios nunca se llaman `estado`, `codigo` ni `mensaje`. Las listas llegan vacías, nunca ausentes.
- **HTTP por categoría del error** (R7): el cliente decide por el `codigo` de la envoltura, no solo por el HTTP.
- **Errores de plataforma** (ruta inexistente, verbo no permitido, `Content-Type` incorrecto, JSON mal formado, excepción no prevista) también con la envoltura (R6).
- **Todo dato como texto** (R9): las respuestas nunca llevan números JSON.
- **Autenticación y sesión:** no las asume la plantilla (R8). Cómo se identifica quien opera lo define cada servidor en su `ENDPOINTS_REST.md`, y su cliente lo implementa en `api`, `servicios`, `modelo` y `controlador` (`Manejar`).
- Para un servicio **SOAP** usar la plantilla `soap_dotnet_movil`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `APT2098` / `APT2261` al compilar para Android (`…microsoft_maui_essentials_fileprovider_file_paths.xml.flat: failed to open file`) o `WMC1006` / `WMC9999` en Windows | La ruta del proyecto es demasiado larga para `aapt2` y para el compilador XAML de WinUI (`LongPathsEnabled` no ayuda). Mover el proyecto a una ruta corta, borrar `bin/` y `obj/` |
| `XA0010: No hay ningún dispositivo disponible` | No hay emulador ni teléfono conectado: arrancar el emulador; `adb devices` debe listarlo |
| «Servidor no disponible» al llamar una operación | Servidor apagado, URL distinta de la real, host equivocado (`localhost` no sirve en Android), firewall o red Pública, o `IpPc` desactualizada. Abrir la URL en el navegador del dispositivo |
| `Cleartext HTTP traffic to … not permitted` | Falta `android:usesCleartextTraffic="true"` en `Platforms/Android/AndroidManifest.xml` (la plantilla lo trae) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín (§6) |
| Otro dispositivo recibe timeout y desde el PC con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| `SRV-000 … (HTTP 404)` con una página HTML | La URL base no incluye el prefijo `api` o el contexto del servidor; revisar `Ruta` en `ConfiguracionApi` |
| `ERR-404` / `ERR-405` / `ERR-415` en pantalla | Defecto del cliente: ruta mal escrita, verbo equivocado o falta el `Content-Type` (el transporte lo envía en todo cuerpo) |
| Un campo llega `null` aunque el servidor lo envía | El nombre de la propiedad del DTO no es el camelCase del JSON (`CodigoItem` ↔ `codigoItem`) |
| Una lista del DTO llega siempre vacía | Debe ser `{ get; } = []` y se rellena con `Populate` (no quitarlo de `TraductorApi.Opciones`) |
| `NETSDK1005 … no tiene un destino para net10.0-android` | Se restauró con `dotnet restore -f` o se compiló con `-f` sin restaurar antes: `dotnet restore` sin `-f` |
| La pantalla compila pero falla al abrirse | Un recurso (`StaticResource`) inexistente o una clave repetida: se detecta al abrir la vista, no al compilar. Probar cada pantalla nueva |
| Funciona en Debug pero falla en Release | Recorte de código (*trimming*): serialización por reflexión de `System.Text.Json` o enlaces. **No verificado** en esta plantilla; si ocurre, probar con el recorte desactivado para confirmar la causa |
| El campo de texto muestra un subrayado en Android | Falta el `AppendToMapping("SinSubrayado", …)` de `MauiProgram.cs` |
| El servidor responde un error de formato con un número | Se envió coma decimal: normalizarla a punto en el servicio |
| `CS1729` / `CS0118` en el destino de Windows | Falta `<CsWinRTAotOptimizerEnabled>false</CsWinRTAotOptimizerEnabled>` en el `.csproj` (ya está) |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el grupo del destino de Windows del `.csproj` |
| `XAPRAS7009` al compilar Android con pruebas | Los paquetes de pruebas están referenciados en el destino de Android: solo van en el de Windows (`.csproj`) |
| `dotnet test` en Android no encuentra pruebas | Las pruebas solo existen en el destino de Windows: `-f net10.0-windows10.0.19041.0` |
| La carpeta `pruebas` no aparece en el Explorador de soluciones de Visual Studio | Falta en el `.csproj` el `<None Include="ec\edu\monster\pruebas\**\*.cs" />` junto al `Compile Remove` |
| El teclado del emulador abre un tutorial de lápiz y ensucia los campos | Gboard muestra su tutorial de escritura a mano la primera vez: cancelarlo; si la prueba automatizada deja texto basura, reiniciar la aplicación |
| La aplicación instalada no cambia de nombre o de ícono | `ApplicationTitle`/`ApplicationId` se aplican al instalar: desinstalar la anterior y volver a ejecutar |
