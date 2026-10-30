# Plantilla: cliente de escritorio .NET (WPF) para servicios SOAP

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
con el esquema de respuesta de esta familia de proyectos. Trae el transporte, los tipos comunes, la verificación de la envoltura,
la ventana principal con sus estilos y el molde de los controladores, y deja marcado lo que cambia con el **dominio**.
No incluye inicio de sesión, sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0-windows`, `Nullable` e `ImplicitUsings` habilitados. **Interfaz:** WPF (XAML) escrita a mano, sin librerías de terceros. **Arquitectura:** capas, con MVVM (el controlador hace de ViewModel).
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` 10.0.652802) con **proxy generado**: `dotnet-svcutil` genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto (`wsdl/`), y `ws/ConfiguracionServicio` solo fija la URL completa, el binding y los tiempos límite. El WSDL **no se descarga en ejecución**.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (presentación oscura, tipografía, medidas, componentes, disposición). Implementado en `estilos/tema.xaml` y `estilos/controles.xaml`.
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (copia en `docs/`, con el TNS fijo `http://ws.monster.edu.ec/` de R2): así entiende a un servidor Java o .NET que la cumpla. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su propio login y su propia sesión (en `modelo`); la plantilla no trae ninguno (§8).
- **Núcleo:** `dto`, `excepciones`, `servicios/ServicioBase` y `ws` (`ConfiguracionServicio`, `TraductorRed`) son los mismos archivos que la plantilla `soap_dotnet_consola`: un proyecto con consola y escritorio los copia **sin editarlos** (`COMPARTIR_CLIENTE.md`).
- **Verificada:** `dotnet test` → 36 pruebas xUnit en verde, sin servidor ni interacción: configuración de la URL y del binding, verificación de la envoltura (`ServicioBase`), traducción de fallos de red (`TraductorRed`, con un canal simulado), excepción del cliente, molde de controlador (`ControladorBase`) y **carga real de la ventana, los estilos y los componentes** (`VistasPruebas`: recursos de `estilos/`, ventana en sus dos modos, alto de campos y botones, zona de estado y tarjeta). `dotnet build` sin errores ni advertencias.
  Las recetas de §4 y los estilos son los que se siguieron y verificaron al construir un cliente de escritorio .NET sobre esta base (probado a mano contra su servidor .NET).
  **No se ha ejecutado:** la ventana de la plantilla abierta en pantalla, ni la plantilla contra un servidor real (no trae proxy ni dominio).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta** para generar los proxies: `dotnet-svcutil` falla con rutas largas (§4, paso 1).
2. **Renombrar** `soap_dotnet_escritorio.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.ws`…), sin intermedio de módulo. Las carpetas de código van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); `estilos/`, `estaticos/`, `wsdl/` y `docs/` también; los archivos, en PascalCase salvo los de `estilos/` (`tema.xaml`, `controles.xaml`).
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 y el escritorio de Windows; la primera vez restaura los paquetes (Internet).
4. **Configurar el servidor:** en `ws/ConfiguracionServicio.cs`, `UrlPorDefecto` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor). Con el servidor **ya actualizado y en marcha**, guardar su WSDL y sus esquemas en `wsdl/` y generar el proxy (§4, paso 1).
   **Si el servidor publica varios servicios** (cada uno con su WSDL y su **URL completa**; R9 prohíbe asumir «base + nombre del servicio»):
   - Guardar el WSDL y los esquemas de cada uno en `wsdl/` y generar **un proxy por servicio, cada uno en su propio espacio de nombres** (p. ej. `ec.edu.monster.ws.generado.consulta`): los esquemas de un mismo servidor suelen definir los mismos tipos (como `RespuestaBase`) y en un espacio común las clases generadas chocan.
   - En `ConfiguracionServicio`, una constante con la URL completa de cada servicio y un método que la lea con `UrlConfigurada(servicio, urlPorDefecto)`:
     ```csharp
     public const string UrlConsultaPorDefecto = "http://localhost:5267/Consulta";
     public static string UrlConsulta() => UrlConfigurada("consulta", UrlConsultaPorDefecto);
     ```
   - Cada servicio se sobrescribe sin recompilar con la variable de entorno `WS_URL_CONSULTA` (el nombre del servicio, en mayúsculas). Los nombres de los servicios y de sus URL se registran en el `docs/PROJECT.md` del proyecto.
5. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la ventana) y, si se quiere, **sustituir la imagen** `estaticos/fondo_app.png` (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6). La imagen es un **PNG** (admite transparencia); si se cambia por otra, **se conserva el nombre `fondo_app.png`** para no tocar el código ni los estilos. Una imagen horizontal se muestra compacta al 60 % de su contenedor (`MargenImagen` y `CentroImagen` en `estilos/tema.xaml`).
6. **Completar `docs/PROJECT.md`** (viene como esqueleto) y mantenerlo al día.
7. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar (F5 en Visual Studio o `dotnet run`). Con la plantilla sin tocar abre la ventana con la pantalla `Inicio`.

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
wsdl/                           copias del WSDL y de los esquemas de cada servicio (de aquí se genera el proxy)
docs/                           CLIENT_STYLES_DOTNET.md, CONVENCION_SOAP.md y CONVENCION_PRUEBAS.md (copias vigentes) y PROJECT.md (esqueleto del proyecto)
README.md                       esta guía: el único documento que queda en la raíz
ec/edu/monster/
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── ws/            ConfiguracionServicio, TraductorRed (+ IXCliente y XClienteWS del dominio)
│   └── generado/  <servicio>/Reference.cs                 proxy de dotnet-svcutil: se regenera, no se edita
├── controlador/   ControladorBase, Comando (+ uno por pantalla)   ViewModel de cada vista
├── vista/         Pantalla, Textos, VentanaPrincipal, ZonaEstado, TarjetaResultado, DatosTarjeta, AyudaCampo, InicioVista (+ las del dominio)
└── pruebas/                                               xUnit + Moq, en el mismo proyecto (ver §4, paso 8)
```

Dependencias permitidas: `vista → controlador → servicios → ws`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `ws` ni `dto`**: su única fuente de datos es su controlador (`DataContext`) y `Textos`.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo la implementación de `ws` conoce el protocolo, los tipos generados y las excepciones de WCF.
Si el WSDL cambia solo se tocan `ws`, `dto` y `servicios`. **Nunca red en el hilo de la interfaz**: las operaciones son `async` y el controlador las espera con `await`.

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
   `Verificar(...)` devuelve la respuesta si `EsOk`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`. **No captura excepciones de red** (ya llegan como `RED`). Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante) y exigir la sesión si el proyecto tiene login. Las reglas del dominio las decide el servidor.
5. **Controlador** — `controlador/XControlador : ControladorBase` (el ViewModel de la pantalla). Recibe el servicio y la navegación como acciones; expone propiedades para cada campo, una propiedad `…Invalida` por campo (`AyudaCampo.Invalido` en la vista), los resultados y `ICommand` con `Comando`:
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
6. **Vista** — `vista/XVista.xaml` (`UserControl`), solo XAML con los estilos de `estilos/controles.xaml` (`{StaticResource …}`; ningún color ni medida suelta). El constructor recibe su controlador y lo asigna a `DataContext`; el código detrás solo lleva el foco (`FocoSolicitado`) y lo que XAML no enlaza (la clave de un `PasswordBox`). Patrón de toda pantalla: `Grid` raíz con `IsEnabled="{Binding NoOcupado}"` (deshabilita todo mientras espera, también `Cancelar`), encabezado en un `Border` `PanelSobreImagenEstilo`, campos con `CajaTexto` y `vista:AyudaCampo.Invalido`, botón principal con `IsDefault="True"` y `Cancelar` con `IsCancel="True"`, y `vista:ZonaEstado` bajo los botones. Ver `InicioVista` y `docs/CLIENT_STYLES_DOTNET.md` §5 y §7.3.
7. **Pantalla y composición** — constante en `vista/Pantalla` (y su ancho en `AnchoContenido`; una pantalla con tabla en `VaArriba`) y, en `App.Componer`: crear el cliente (`XClienteWS.Crear()`), el servicio, el controlador y la vista, registrar `_pantallas[Pantalla.X] = (vista, controlador)` y conectar la navegación con `Ir(Pantalla.X)`.
8. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto, sin sesión).
   - **Controladores:** el servicio concreto se sustituye con Moq (métodos públicos `virtual`; `new Mock<XServicio>(null!, null!)`, los constructores solo asignan campos). Las tareas pendientes se simulan con `TaskCompletionSource` para probar el estado «ocupado».
   - **Envoltorio:** `Convertir` se prueba campo a campo con los tipos generados.
   - **Vistas:** ampliar `VistasPruebas` con las del dominio (carga sin errores de XAML). Los cambios de estado llegan a los enlaces por la cola del despachador: vaciarla (`Dispatcher.Invoke` a prioridad `ContextIdle`) antes de medir.
   - El contrato y el envoltorio de protocolo **no** se prueban con código: se verifican a mano con `docs/CONVENCION_PRUEBAS.md` §6.3.

**Si el proyecto tiene login:** un `AutenticacionControlador` (usuario, clave, `Manejar` sobrescrito para tratar la credencial inválida como mensaje y la clave siempre limpia), una `LoginVista` en `Pantalla.Login` (modo acceso, paneles en `Superficie`; la clave en un `PasswordBox` que pasa su valor al controlador en `PasswordChanged`) y la sesión en `modelo`. Cada servicio exige la sesión; `AlSesionNoValida` borra la sesión y vuelve al acceso. Nada de esto está en la plantilla.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, tiempo agotado y `Fault` son `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- Un fallo de red no cierra la aplicación: se muestra el mensaje en la zona de estado.
- **Un fallo al cerrar el canal después de una operación exitosa no es un error de la operación** (`TraductorRed` lo descarta con `Abort`).
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

- **Servidor por defecto:** `ConfiguracionServicio.UrlPorDefecto` (URL completa). Sin recompilar: la variable de entorno `WS_URL`. Con varios servicios, una por servicio: `WS_URL_<SERVICIO>` (§1, paso 4). En PowerShell: `$env:WS_URL_CONSULTA = 'http://host:5267/Consulta'` antes de `dotnet run`.
- **Servidor .NET local:** con el perfil `http` (Kestrel) el puerto por defecto es 5267; con `IIS Express`, 57592 (solo HTTP; ver `context/entornos/IIS_CONFIG.md`). Si el servidor corre en IIS Express, es Visual Studio quien lo mantiene vivo: si se detiene, el servidor está apagado.
- **El WSDL está empaquetado** (`wsdl/`): crear el cliente no abre ninguna conexión, así que con el servidor apagado la ventana abre y el fallo llega al **llamar** una operación, donde `TraductorRed` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **Visual Studio:** *Run* ejecuta el proyecto; **detener la depuración antes de volver a compilar** (si no, `MSB3021`/`MSB3027`: el `.exe` queda bloqueado).
- Si Windows marca los archivos descargados (`MSB3821`, zona restringida): `Get-ChildItem <carpeta> -Recurse -File -Force | Unblock-File`.

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor ni pantalla)
dotnet build                 # compilar
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con la aplicación en marcha:
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor en la zona de estado y se conserva lo escrito.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y la ventana sigue respondiendo.
4. Doble clic en una acción: se envía **una** petición (controles deshabilitados mientras espera).
5. Ventana maximizada y a tamaño mínimo; sin barras de desplazamiento si el contenido cabe; tildes y «ñ» en campos y mensajes.
6. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, carga de vistas) y que **no vio la ventana real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_SOAP.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `servicios`:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (`elementFormDefault="qualified"`): si la copia del esquema no lo declara, los campos llegan `null` sin error (R3).
- **Namespace (TNS) fijo `http://ws.monster.edu.ec/`** en todos los servicios del equipo (R2). **SOAPAction = `TNS + Servicio + "/" + operación`**, que viene en el WSDL (R4).
- **El cliente no depende del nombre del puerto ni del binding** (R10) y recibe la **URL completa** por configuración (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los campos propios nunca se llaman `estado`, `codigo` ni `mensaje`.
- Todos los datos viajan como texto (R7); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R8); los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5).
- Un WSDL de CoreWCF trae el esquema de serialización de Microsoft (`xsd1`) y una lista como clase que hereda de `List<T>`; un WSDL Java, un solo `.xsd`.
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (token, credenciales en cada operación, nada) lo define cada servidor y su cliente lo implementa en `servicios`, con la sesión en `modelo`.
- Para un servicio **REST** usar la plantilla `rest_dotnet_escritorio`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `XamlParseException` al abrir la ventana: «"4" no es un valor válido para la propiedad "Width"» | Un recurso `Double` usado como `ColumnDefinition.Width` / `RowDefinition.Height`: debe ser `GridLength` |
| `"System.Windows.Style" no es un valor válido para la propiedad "Foreground"` | Un pincel y un estilo comparten clave: renombrar el estilo (sufijo `Estilo`) |
| Los campos de texto salen de 66 de alto | `Padding` repetido en el `Border` de la plantilla del campo: `TextBox` y `PasswordBox` ya lo aplican (el `Border` no lleva `Padding`) |
| Barra de desplazamiento siempre visible | Margen en el `Grid` interior en vez de `Padding` en el `ScrollViewer` (el contenido excede el área visible) |
| El botón secundario se pone morado al pasar el ratón | El pincel de «presionado» debe viajar en `Tag` (`BotonSecundario` cambia `Background` y `Tag`) |
| Una prueba de la zona de estado no ve el cambio | Los enlaces se actualizan por la cola del despachador: vaciarla antes de medir (`VistasPruebas`) |
| `CommunicationException` / «Servidor no disponible» al llamar una operación | Servidor apagado (con IIS Express, Visual Studio detenido) o `UrlPorDefecto` distinta de la real. Abrir `<URL>?wsdl` en el navegador |
| `dotnet-svcutil` falla (`MSB3030`, rutas demasiado largas) | Generar en una carpeta de ruta corta (§4, paso 1) |
| `dotnet-svcutil` añade paquetes `System.ServiceModel.*` 4.10 a un proyecto | Se ejecutó dentro del proyecto: generar en una carpeta descartable y copiar solo los `Reference.cs` |
| Avisos `NU1510` por `System.Security.Cryptography.Pkcs`/`.Xml` | No fijarlos en este proyecto: en `net10.0-windows` ya vienen con el framework (la consola sí los fija) |
| Cambió el servidor y el cliente no ve la operación nueva | Guardar de nuevo el WSDL y los esquemas, regenerar el proxy y agregar la operación a la interfaz y al envoltorio (§4) |
| Respuesta con campos vacíos | Se guardó un WSDL antiguo, o el envoltorio no copia un campo de la respuesta generada al DTO propio |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | El WSDL guardado no trae el `soapAction` de la operación: guardar el WSDL del servidor actualizado y regenerar |
| El servidor no recibe los parámetros (llegan vacíos) | El esquema guardado no califica los elementos (`elementFormDefault="qualified"`) |
| `ListaX` no tiene `Length` | Es una lista (`List<T>`): usar `Count` |
| Errores por tipos duplicados (`RespuestaBase`…) | Varios servicios generados en el mismo espacio de nombres: darle a cada uno el suyo |
| Una prueba se cuelga | Un canal simulado cuyo `BeginClose` no invoca el callback: `Task.Factory.FromAsync` espera para siempre (ver `TraductorRedPruebas`) |
| `MSB3021`/`MSB3027`: no se puede copiar `apphost.exe` al compilar | La aplicación sigue en ejecución (p. ej. depurando en Visual Studio): detenerla antes de compilar |
