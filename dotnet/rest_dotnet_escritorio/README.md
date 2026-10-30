# Plantilla: cliente de escritorio .NET (WPF) para servicios REST

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga una API **REST** con el esquema de respuesta de esta familia de proyectos (envoltura `estado` / `codigo` / `mensaje`). Trae el transporte HTTP, los tipos comunes, la verificación de la envoltura, la ventana principal con sus estilos y el molde de los controladores, y deja marcado lo que cambia con el **dominio**. No incluye inicio de sesión, sesión ni pantallas de dominio: se construyen con la receta de §4. Es la contraparte de `soap_dotnet_escritorio`: todo es igual salvo la capa de protocolo (`ws` ↔ `api`).

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0-windows`, `Nullable` e `ImplicitUsings` habilitados. **Interfaz:** WPF (XAML) escrita a mano, sin librerías de terceros. **Arquitectura:** capas, con MVVM (el controlador hace de ViewModel).
- **Transporte:** `HttpClient` del SDK y `System.Text.Json`; ningún paquete extra. Un solo `HttpClient` de larga vida, con tiempo límite de 10 s. **Ojo:** el SDK de WPF no incluye `System.Net.Http` entre los `using` implícitos (el de la consola sí), por eso el `.csproj` trae `<Using Include="System.Net.Http" />`; sin esa línea `api` no compila.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (presentación oscura, tipografía, medidas, componentes, disposición). Implementado en `estilos/tema.xaml` y `estilos/controles.xaml`.
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`). El contrato concreto de cada proyecto (rutas, verbos, cuerpos, autenticación) va en su `docs/ENDPOINTS_REST.md`, que se **escribe o se copia del servidor antes de implementar**. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Cada proyecto define cómo viajan las credenciales o el token (R8, §8) y su sesión (en `modelo`); la plantilla no trae ninguno.
- **Mapeo JSON ↔ DTO:** los DTO no llevan anotaciones; `api/TraductorApi` configura nombres en camelCase, lectura sin distinguir mayúsculas y `Populate` (`ARQUITECTURA_CLIENTE.md` §4.5).
- **Núcleo:** `dto`, `excepciones`, `servicios/ServicioBase` y `api` (`ConfiguracionApi`, `ITransporte`, `TransporteHttp`, `RespuestaHttp`, `TraductorApi`) son los mismos archivos que la plantilla `rest_dotnet_consola`: un proyecto con consola y escritorio los copia **sin editarlos** (`COMPARTIR_CLIENTE.md`).
- **Verificada:** `dotnet test` → **69 pruebas** xUnit en verde, sin servidor ni interacción: configuración de la URL, armado y lectura de peticiones (`TraductorApi`, con todos los HTTP de error, `RED` y `SRV-000`), el transporte real contra un `HttpMessageHandler` simulado, verificación de la envoltura (`ServicioBase`), excepción del cliente, molde de controlador (`ControladorBase`) y **carga real de la ventana, los estilos y los componentes** (`VistasPruebas`). `dotnet build` sin errores ni advertencias. La capa `api` es la del cliente de escritorio .NET REST construido sobre esta base, probado a mano contra su servidor.
  **No se ha ejecutado:** la ventana de la plantilla abierta en pantalla, ni la plantilla contra un servidor real (no trae dominio ni clientes `…Api`).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real, decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**.
2. **Renombrar** `rest_dotnet_escritorio.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.api`…). Las carpetas de código van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); `estilos/`, `estaticos/` y `docs/` también; los archivos, en PascalCase salvo los de `estilos/` (`tema.xaml`, `controles.xaml`).
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 y el escritorio de Windows.
4. **Configurar el servidor:** en `api/ConfiguracionApi.cs`, `UrlBasePorDefecto` = **URL base completa de la API**, con host, puerto, contexto (si lo hay) y prefijo `api`, **con «/» final** (R1). Se sobrescribe sin recompilar con la variable de entorno `API_BASE_URL`. Las rutas de las operaciones se le añaden relativas y sin barra inicial.
5. **Copiar el contrato del servidor** a `docs/ENDPOINTS_REST.md` (una fila por operación).
6. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la ventana) y, si se quiere, **sustituir la imagen** `estaticos/fondo_app.png` (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6). La imagen es un **PNG** (admite transparencia); si se cambia por otra, **se conserva el nombre `fondo_app.png`** para no tocar el código ni los estilos. Una imagen horizontal se muestra compacta al 60 % de su contenedor (`MargenImagen` y `CentroImagen` en `estilos/tema.xaml`).
7. **Completar `docs/PROJECT.md`** (viene como esqueleto) y mantenerlo al día.
8. Con el servidor en marcha, ejecutar (F5 en Visual Studio o `dotnet run`). Con la plantilla sin tocar abre la ventana con la pantalla `Inicio`.

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
| **Molde de controlador** | `controlador/ControladorBase` (estado de la zona: `Ocupado`/`Error`/`Exito`, `EjecutarAsync`, segunda acción ignorada, error como mensaje, `FocoSolicitado`) y `controlador/Comando` | No; los controladores del dominio la extienden. Un proyecto con login sobrescribe `Manejar` |
| **Ventana y navegación** | `vista/VentanaPrincipal` (dos modos: acceso dividido y aplicación sobre la imagen), `vista/Pantalla` | Se agregan pantallas y, si no son un formulario de 480, su ancho y alineación |
| **Componentes** | `vista/ZonaEstado` (ocupado / error / éxito), `vista/TarjetaResultado` + `vista/DatosTarjeta`, `vista/AyudaCampo` (campo inválido) | No |
| **Estilos** | `estilos/tema.xaml`, `estilos/controles.xaml`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos** | `vista/Textos` | `NombreAplicacion` y los textos del dominio |
| **Composición** | `App.xaml` (carga los estilos) y `App.xaml.cs` (`Componer`, `Ir`) | Se completa con los clientes, servicios, controladores y vistas del dominio |
| **Pantalla de ejemplo** | `vista/InicioVista` | **Se reemplaza** por las pantallas del dominio |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec App.xaml.cs`).

## 3. Estructura

```
App.xaml, App.xaml.cs           carga los estilos; punto de composición (clientes, servicios, controladores, vistas) y navegación (Ir)
estilos/       tema.xaml, controles.xaml                       tokens y estilos de los controles (nada de colores sueltos en las vistas)
estaticos/     fondo_app.png                                    imagen decorativa de ejemplo (Resource)
docs/                           CLIENT_STYLES_DOTNET.md, CONVENCION_REST.md y CONVENCION_PRUEBAS.md (copias vigentes), ENDPOINTS_REST.md (del proyecto) y PROJECT.md (esqueleto)
README.md                       esta guía: el único documento que queda en la raíz
ec/edu/monster/
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── api/           ConfiguracionApi, ITransporte, TransporteHttp, RespuestaHttp, TraductorApi (+ IXCliente y XClienteApi del dominio)
├── controlador/   ControladorBase, Comando (+ uno por pantalla)   ViewModel de cada vista
├── vista/         Pantalla, Textos, VentanaPrincipal, ZonaEstado, TarjetaResultado, DatosTarjeta, AyudaCampo, InicioVista (+ las del dominio)
└── pruebas/                                               xUnit + Moq, en el mismo proyecto (ver §4, paso 8)
```

Dependencias permitidas: `vista → controlador → servicios → api`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `api` ni `dto`**: su única fuente de datos es su controlador (`DataContext`) y `Textos`.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo `api` conoce HTTP y JSON. Si el contrato cambia solo se tocan `api`, `dto` y `servicios`. **Nunca red en el hilo de la interfaz**: las operaciones son `async` y el controlador las espera con `await`.

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET items` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Interfaz y cliente** — `api/IXCliente.cs` (interfaz propia con **un método por fila** de `ENDPOINTS_REST.md`, con el nombre de la operación, retorno `Task<DTO propio?>`) y `api/XClienteApi.cs`, que la implementa con `TraductorApi`. Lo único que ven el servicio y las vistas es la interfaz:
   ```csharp
   public sealed class XClienteApi : IXCliente
   {
       private readonly ITransporte _transporte;

       public XClienteApi(ITransporte transporte) { _transporte = transporte; }

       public static IXCliente Crear() => new XClienteApi(TransporteHttp.Crear());   // un solo HttpClient para todo el programa

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
   `Verificar(...)` devuelve la respuesta si `EsOk`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`. **No captura excepciones de red** (ya llegan como `RED`). Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante) y exigir la sesión si el proyecto tiene login. Las reglas del dominio las decide el servidor.
4. **Controlador** — `controlador/XControlador : ControladorBase` (el ViewModel de la pantalla). Recibe el servicio y la navegación como acciones; expone propiedades para cada campo, una propiedad `…Invalida` por campo (`AyudaCampo.Invalido` en la vista), los resultados y `ICommand` con `Comando`:
   ```csharp
   public async Task ConsultarAsync()
   {
       if (Ocupado) { return; }
       LimpiarMarcas();
       if (Vacio(Codigo)) { CodigoInvalido = true; MostrarError(Textos.CampoObligatorio(Textos.Codigo)); PedirFoco(CampoCodigo); return; }
       await EjecutarAsync(Textos.Consultando, () => _servicio.ItemsAsync(Codigo.Trim()), items => Items = items);
   }
   ```
   `EjecutarAsync` marca la pantalla «ocupada», ignora una segunda acción y muestra `e.Message` si el servicio falla (`Manejar`; un proyecto con login lo sobrescribe para el código de sesión no válida). Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar los campos vacíos **antes** de llamar, en el orden de la pantalla (se informa el primero); mostrar solo el mensaje, nunca el código; conservar lo escrito ante un error salvo las claves; no conocer la pantalla siguiente. Sobrescribir `LimpiarMarcas`, `OcultarResultado` y `AlMostrar` para dejar la pantalla limpia.
5. **Vista** — `vista/XVista.xaml` (`UserControl`), solo XAML con los estilos de `estilos/controles.xaml` (`{StaticResource …}`; ningún color ni medida suelta). El constructor recibe su controlador y lo asigna a `DataContext`; el código detrás solo lleva el foco (`FocoSolicitado`) y lo que XAML no enlaza (la clave de un `PasswordBox`). Patrón de toda pantalla: `Grid` raíz con `IsEnabled="{Binding NoOcupado}"` (deshabilita todo mientras espera, también `Cancelar`), encabezado en un `Border` `PanelSobreImagenEstilo`, campos con `CajaTexto` y `vista:AyudaCampo.Invalido`, botón principal con `IsDefault="True"` y `Cancelar` con `IsCancel="True"`, y `vista:ZonaEstado` bajo los botones. Ver `InicioVista` y `docs/CLIENT_STYLES_DOTNET.md` §5 y §7.3.
6. **Pantalla y composición** — constante en `vista/Pantalla` (y su ancho en `AnchoContenido`; una pantalla con tabla en `VaArriba`) y, en `App.Componer`: crear el cliente (`XClienteApi.Crear()`), el servicio, el controlador y la vista, registrar `_pantallas[Pantalla.X] = (vista, controlador)` y conectar la navegación con `Ir(Pantalla.X)`.
7. **Autenticación (si el proyecto la tiene)** — se define en `docs/ENDPOINTS_REST.md` (R8) y se implementa **solo en `api`**: las credenciales siguen siendo parámetros de la interfaz del cliente y `XClienteApi` las coloca donde el contrato diga. Con `Authorization: Basic` por petición, por ejemplo:
   ```csharp
   string Basic(string usuario, string clave) =>
       "Basic " + Convert.ToBase64String(Encoding.UTF8.GetBytes(usuario + ":" + clave));
   // ... TraductorApi.LlamarAsync<...>(_transporte, "GET", ruta, null, Basic(usuario, clave))
   ```
   Nunca en la URL, nunca en las cabeceras por defecto del `HttpClient`, nunca en un registro. La sesión (si la hay) vive en `modelo`.
8. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto, sin sesión).
   - **Clientes `…Api`:** con `TransporteFalso` (en `pruebas/`) se comprueba el verbo, la ruta, el cuerpo y la cabecera de cada método, y que un HTTP de error llega como DTO y no como excepción.
   - **Controladores:** el servicio concreto se sustituye con Moq (métodos públicos `virtual`; `new Mock<XServicio>(null!, null!)`, los constructores solo asignan campos). Las tareas pendientes se simulan con `TaskCompletionSource` para probar el estado «ocupado».
   - **Vistas:** ampliar `VistasPruebas` con las del dominio (carga sin errores de XAML). Los cambios de estado llegan a los enlaces por la cola del despachador: vaciarla (`Dispatcher.Invoke` a prioridad `ContextIdle`) antes de medir.
   - El contrato **no** se prueba con código: se verifica a mano contra `docs/ENDPOINTS_REST.md` con `docs/CONVENCION_PRUEBAS.md` §6.3.

**Si el proyecto tiene login:** un `AutenticacionControlador` (usuario, clave, `Manejar` sobrescrito para tratar la credencial inválida como mensaje y la clave siempre limpia), una `LoginVista` en `Pantalla.Login` (modo acceso, paneles en `Superficie`; la clave en un `PasswordBox` que pasa su valor al controlador en `PasswordChanged`) y la sesión en `modelo`. Cada servicio exige la sesión; `AlSesionNoValida` borra la sesión y vuelve al acceso. Nada de esto está en la plantilla.

Reglas que se repiten en todo dominio:
- **Un error de negocio llega con HTTP 4xx o 5xx y con la envoltura en el cuerpo**; el cliente decide por el `codigo` y muestra el `mensaje`. Solo red, tiempo agotado y cuerpo ilegible son `RED` y `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- Un fallo de red no cierra la aplicación: se muestra el mensaje en la zona de estado.
- `ERR-404`, `ERR-405` y `ERR-415` (errores de plataforma) se muestran como cualquier otro error, pero indican un defecto del cliente (ruta, verbo o `Content-Type`).
- Si hay login: la sesión vive solo en memoria (`modelo`), nunca en disco ni en logs.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo referencian recursos** (`{StaticResource …}`); no colorean ni dimensionan controles sueltos.

| Recurso | Para qué |
|---|---|
| `BotonPrimario` | Acción principal de la pantalla |
| `BotonSecundario` | Salida o regreso: «Cerrar sesión», «Cancelar» |
| `BotonCuadricula` | Botón de una cuadrícula de acciones (96 de alto; `UniformGrid` con `Margin="-8,16,-8,-8"`) |
| `TituloAplicacion`, `TituloPantalla`, `TextoResultado`, `TextoCuerpo`, `EtiquetaCampo`, `TextoMensaje` | Estilos de texto por rol |
| `CajaTexto`, `CajaContrasenia` | Campos; `vista:AyudaCampo.Invalido="{Binding …}"` marca el borde de error |
| `PanelSuperficie`, `PanelSobreImagenEstilo`, `PanelMensajeSuperficie` | Paneles (encabezado, mensaje); `Superficie` en el acceso, sobre la imagen en el resto |
| `IndicadorProgreso` | Barra de progreso indeterminada (la usa `ZonaEstado`) |
| `vista:ZonaEstado` | Un solo lugar, bajo los botones, que muestra de uno en uno el indicador de ocupado, un error o un éxito; propiedad `Fondo` |
| `vista:TarjetaResultado` | Resumen de un cambio de valor (título, monto con signo, variación, pie); `DataContext` = `DatosTarjeta` con los textos ya formateados |

- **Disposición:** dos modos en `VentanaPrincipal.Mostrar(pantalla, vista)`. El **acceso** (`Pantalla.Login`) va dividido: pantalla a la izquierda (columna de 420) e imagen a la derecha. El **resto** usa el ancho completo y la imagen es el fondo de la ventana: completa, centrada, con opacidad (`OpacidadImagen`) y reescalable, con los paneles sobre ella; el contenido se centra en vertical y se desplaza solo si no cabe. Una pantalla con tabla (`VaArriba`) va arriba, ocupa el alto de la ventana y solo su tabla se desplaza. Tamaño inicial 1100 × 720, mínimo 900 × 640.
- **Tabla:** no hay componente: se arma en cada pantalla (`ItemsControl` con columnas `SharedSizeGroup`, encabezado fijo; receta en `docs/CLIENT_STYLES_DOTNET.md` §7.3).
- **Presentación clara:** cambiar los valores de `estilos/tema.xaml` por los de `docs/CLIENT_STYLES_DOTNET.md` §2.3 (la imagen no se usa).
- Los recursos que alimentan `ColumnDefinition.Width` o `RowDefinition.Height` son `GridLength`; un pincel y un estilo no pueden compartir clave (sufijo `Estilo`). Un error de recurso solo aparece al cargar la vista, no al compilar: `VistasPruebas` lo detecta.
- Antes de construir las pantallas de un proyecto conviene especificarlas en su `docs/PANTALLAS.md` (orden de los elementos, anchos, estados y decisiones).

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.UrlBasePorDefecto` (URL base completa, con «/» final). Sin recompilar: la variable de entorno `API_BASE_URL`. En PowerShell: `$env:API_BASE_URL = 'http://host:5267/api/'` antes de `dotnet run`.
- **Servidor .NET local:** con el perfil `http` (Kestrel) el puerto por defecto es 5267; con `IIS Express`, 57592 (solo HTTP; ver `context/entornos/IIS_CONFIG.md`). Si el servidor corre en IIS Express, es Visual Studio quien lo mantiene vivo.
- **Crear el cliente no abre ninguna conexión**: con el servidor apagado la ventana abre, y el fallo llega al **llamar** una operación, donde `TraductorApi` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **Visual Studio:** *Run* ejecuta el proyecto; **detener la depuración antes de volver a compilar** (si no, `MSB3021`/`MSB3027`: el `.exe` queda bloqueado).
- Si Windows marca los archivos descargados (`MSB3821`, zona restringida): `Get-ChildItem <carpeta> -Recurse -File -Force | Unblock-File`.

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor ni pantalla)
dotnet build                 # compilar
dotnet run                   # ejecutar contra el servidor configurado
```

Verificación con la aplicación en marcha:
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor en la zona de estado y se conserva lo escrito.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y la ventana sigue respondiendo.
4. Doble clic en una acción: se envía **una** petición (controles deshabilitados mientras espera).
5. Ventana maximizada y a tamaño mínimo; sin barras de desplazamiento si el contenido cabe; tildes y «ñ» en campos y mensajes.
6. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, carga de vistas) y que **no vio la ventana real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- **URL base** con prefijo `api` y «/» final (R1); rutas relativas y sin barra inicial.
- **JSON UTF-8** en peticiones y respuestas (R3); el cuerpo de las peticiones lleva `Content-Type: application/json`, o el servidor responde `ERR-415`.
- **Toda respuesta, de éxito o de error, lleva la envoltura** `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación (R5). Los campos propios nunca se llaman `estado`, `codigo` ni `mensaje`. Las listas llegan vacías, nunca ausentes.
- **HTTP por categoría del error** (R7): el cliente decide por el `codigo` de la envoltura, no solo por el HTTP.
- **Errores de plataforma** (ruta inexistente, verbo no permitido, `Content-Type` incorrecto, JSON mal formado, excepción no prevista) también con la envoltura (R6).
- **Todo dato como texto** (R9): las respuestas nunca llevan números JSON.
- **Autenticación:** no la asume la plantilla (R8). Cómo se identifica quien opera lo define cada servidor en su `ENDPOINTS_REST.md` y su cliente lo implementa en `api`, con la sesión en `modelo`.
- Para un servicio **SOAP** usar la plantilla `soap_dotnet_escritorio`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `HttpClient` no se encuentra (`CS0246`) al compilar `api` | Falta `<Using Include="System.Net.Http" />` en el `.csproj`: el SDK de WPF no lo incluye entre los `using` implícitos |
| `XamlParseException` al abrir la ventana: «"4" no es un valor válido para la propiedad "Width"» | Un recurso `Double` usado como `ColumnDefinition.Width` / `RowDefinition.Height`: debe ser `GridLength` |
| `"System.Windows.Style" no es un valor válido para la propiedad "Foreground"` | Un pincel y un estilo comparten clave: renombrar el estilo (sufijo `Estilo`) |
| Los campos de texto salen de 66 de alto | `Padding` repetido en el `Border` de la plantilla del campo: `TextBox` y `PasswordBox` ya lo aplican (el `Border` no lleva `Padding`) |
| Barra de desplazamiento siempre visible | Margen en el `Grid` interior en vez de `Padding` en el `ScrollViewer` (el contenido excede el área visible) |
| El botón secundario se pone morado al pasar el ratón | El pincel de «presionado» debe viajar en `Tag` (`BotonSecundario` cambia `Background` y `Tag`) |
| Una prueba de la zona de estado no ve el cambio | Los enlaces se actualizan por la cola del despachador: vaciarla antes de medir (`VistasPruebas`) |
| «Servidor no disponible» al llamar una operación | Servidor apagado (con IIS Express, Visual Studio detenido) o `UrlBasePorDefecto` distinta de la real. Probar la URL en el navegador o con `curl` |
| `SRV-000 … (HTTP 404)` con una página HTML | La URL base no incluye el prefijo `api` o el contexto del servidor; revisar `ConfiguracionApi` |
| `ERR-404` / `ERR-405` / `ERR-415` en pantalla | Defecto del cliente: ruta mal escrita, verbo equivocado o falta el `Content-Type` (el transporte lo envía en todo cuerpo) |
| Un campo llega `null` aunque el servidor lo envía | El nombre de la propiedad del DTO no es el camelCase del JSON (`CodigoItem` ↔ `codigoItem`) |
| Una lista del DTO llega siempre vacía | Debe ser `{ get; } = []` y se rellena con `Populate` (no quitarlo de `TraductorApi.Opciones`) |
| El servidor rechaza un importe con coma | La coma se normaliza a punto en el **servicio**, no en la vista |
| Avisos `NU1510` por `System.Security.Cryptography.Pkcs`/`.Xml` | No fijarlos en este proyecto: en `net10.0-windows` ya vienen con el framework |
| `MSB3021`/`MSB3027`: no se puede copiar `apphost.exe` al compilar | La aplicación sigue en ejecución (p. ej. depurando en Visual Studio): detenerla antes de compilar |
