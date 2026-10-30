# Plantilla: cliente móvil .NET (MAUI) para servicios SOAP

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte (con la URL por plataforma), los tipos comunes,
la verificación de la envoltura, el molde de los controladores, los estilos y los componentes visuales base, y deja marcado lo que cambia con el **dominio**.
No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0-android` y `net10.0-windows10.0.19041.0` (`Nullable` e `ImplicitUsings` habilitados). **Interfaz:** .NET MAUI (XAML), una sola página que aloja la pantalla actual (sin `Shell`), sin librerías de terceros. **Arquitectura:** capas con MVVM (el controlador hace de ViewModel), sin frameworks.
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` 10.0.652802) con **proxy generado**: `dotnet-svcutil` genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto (`wsdl/`), y `ws/ConfiguracionServicio` fija la URL completa (con el host de cada plataforma), el binding y los tiempos límite. El WSDL **no se descarga en ejecución**.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (presentación **oscura**, tipografía, medidas, componentes). Implementado en `Resources/Styles/Colors.xaml` y `Styles.xaml` y en los componentes `CampoTexto` y `ZonaEstado`.
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (copia en `docs/`, con el TNS fijo `http://ws.monster.edu.ec/` de R2): así entiende a un servidor Java o .NET que la cumpla.
- **Autenticación y sesión:** **fuera de la plantilla base.** Todo proyecto define su propio login y su propia sesión (en `modelo`); la plantilla no trae ninguno ni decide cómo se maneja una sesión no válida (§8).
- **Verificada:** `dotnet test -f net10.0-windows10.0.19041.0` → 41 pruebas xUnit en verde, sin servidor: configuración de la URL por plataforma, de la variable de entorno y del binding, verificación de la envoltura (`ServicioBase`), traducción de fallos de red (`TraductorRed`, con un canal simulado), excepción del cliente y molde de los controladores (`ControladorBase`). `dotnet build -f net10.0-android` sin errores ni advertencias.
  Las recetas de §4 son las que se siguieron y verificaron al construir un cliente móvil .NET sobre esta base (probado en un teléfono real contra su servidor .NET).
  **No se ha ejecutado:** la plantilla en un emulador o teléfono (no trae proxy ni dominio ni pantallas: la página queda vacía sobre el color de fondo).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta**: `aapt2` (Android) y `dotnet-svcutil` fallan con rutas largas (§9).
2. **Renombrar** `soap_dotnet_movil.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.ws`…), sin intermedio de módulo. Las carpetas van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); los archivos, en PascalCase.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 con las cargas de trabajo `android` y `maui-windows`, el SDK de Android (con un emulador) y el JDK; la primera vez restaura los paquetes (Internet).
4. **Nombre de la aplicación:** `ApplicationTitle` en el `.csproj` (nombre visible en el dispositivo) y `vista/Textos.cs` → `NombreAplicacion`; mantenerlos iguales.
   **Identificador del paquete:** `ApplicationId` en el `.csproj` (`ec.edu.monster.movil` de la plantilla). Android trata un `ApplicationId` distinto como otra aplicación: desinstalar la anterior del dispositivo.
5. **Configurar el servidor:** en `ws/ConfiguracionServicio.cs`, `Puerto` (5267 con Kestrel, 57592 con IIS Express), `RutaServicio` (ruta de la URL, sin «/» inicial ni final) e `IpPc` (solo para un teléfono físico: la IP del PC en la red; la plantilla trae el marcador `127.0.0.1`). La URL completa se arma con el host de cada plataforma: `10.0.2.2` en el emulador, `IpPc` en un teléfono físico y `localhost` en Windows (§6).
   **Si el servidor publica varios servicios** (cada uno con su WSDL y su **URL completa**; R9 prohíbe asumir «base + nombre del servicio»):
   - Guardar el WSDL y los esquemas de cada uno en `wsdl/` y generar **un proxy por servicio, cada uno en su propio espacio de nombres** (p. ej. `ec.edu.monster.ws.generado.consulta`). Los esquemas de un mismo servidor suelen definir los mismos tipos (como `RespuestaBase`) en el mismo namespace: en un espacio común las clases generadas chocan.
   - En `ConfiguracionServicio`, una constante con la ruta de cada servicio y un método que arme su URL con `UrlConfigurada(servicio, UrlPorDefecto(ruta))`:
     ```csharp
     public const string RutaConsulta = "Consulta";
     public static string UrlConsulta() => UrlConfigurada("consulta", UrlPorDefecto(RutaConsulta));
     ```
   - Cada servicio se sobrescribe sin recompilar con la variable de entorno `WS_URL_CONSULTA` (el nombre del servicio, en mayúsculas), que solo rige en el destino de Windows (Android no tiene variables de entorno). Un servicio Java se configura igual que uno .NET: es solo otra ruta.
   - Los nombres de los servicios y de sus rutas se registran en el `docs/PROJECT.md` del proyecto.
6. **Sustituir la imagen** `Resources/Images/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6), o quitar el `Image` de `PaginaPrincipal.xaml` y de la pantalla de acceso para no usarla.
7. **Generar el proxy** desde el WSDL del servidor (§4, paso 1) y construir las pantallas del dominio (§4, pasos 2 a 7).
8. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
9. **Probar el servidor** (el WSDL abre en el navegador del PC y del emulador, §6) y ejecutar en el emulador. Con la plantilla sin tocar abre una página vacía sobre el color de fondo.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración del transporte** | `ws/ConfiguracionServicio` (`Puerto`, `IpPc`, `RutaServicio`, host por plataforma con `DeviceInfo`, `CrearBinding`, `UrlConfigurada()` y `UrlConfigurada(servicio, urlPorDefecto)`, normalización; nada del dominio) | `Puerto`, `RutaServicio` (o una ruta por servicio, §1 paso 5) e `IpPc` siempre; el resto no |
| **Traducción de fallos de red** | `ws/TraductorRed` (`CommunicationException` y `TimeoutException` → `ExcepcionServicio` con código `RED`; un canal por llamada) | No |
| **Proxy generado** | `wsdl/` (copias del WSDL y los esquemas) → `dotnet-svcutil` → `ws/generado/<servicio>/Reference.cs` | **Se genera** por proyecto y servicio (§4, paso 1) |
| **Interfaz y envoltorio** | — (`ws/IXCliente.cs` y `ws/XClienteWS.cs`) | **Se crean** por proyecto, una firma por operación del servidor (§4, paso 2) |
| **DTO base** | `dto/RespuestaBaseDto` (`Estado`, `Codigo`, `Mensaje`, `EsOk`) | No; se agregan DTO del dominio al lado |
| **Verificación de la envoltura** | `servicios/ServicioBase` (`Verificar`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (código + mensaje; códigos propios `RED` y `SRV-000`) | No |
| **Molde MVVM** | `controlador/ControladorBase` (`Estado` de la zona, `Ocupado`, `Mensaje`, `EjecutarAsync`, `Manejar`, `FocoSolicitado`), `Comando` | No; los controladores del dominio la extienden |
| **Página y componentes** | `vista/PaginaPrincipal` (aloja la pantalla actual y la imagen de fondo), `vista/Pantalla`, `vista/CampoTexto`, `vista/ZonaEstado` | `Pantalla` crece con las pantallas del dominio; el resto no |
| **Estilos** | `Resources/Styles/Colors.xaml` (tokens), `Styles.xaml` (estilos), `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` y los textos del dominio |
| **Composición** | `App.xaml(.cs)` (tema oscuro fijo; `Componer` con el `TODO` del montaje; `Ir`), `MauiProgram.cs` (subrayado del `Entry` en Android) | `Componer` se completa con los clientes, servicios, controladores y vistas del dominio |
| **Plataforma Android** | `Platforms/Android/AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`, `usesCleartextTraffic`), `colors.xml` (colores de la barra) | `usesCleartextTraffic` solo se quita si el servidor usa HTTPS |
| **Pruebas** | `pruebas/` (URL y binding, transporte, `ServicioBase`, excepción, `ControladorBase`) | Se conservan; se agregan las del dominio |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" ec App.xaml.cs soap_dotnet_movil.csproj`).

## 3. Estructura

```
App.xaml(.cs), MauiProgram.cs   punto de composición (clientes, servicios, controladores, vistas y navegación) y tema; sin Shell
Platforms/                      Android (manifiesto, actividad) y Windows
Resources/                      Styles/ (Colors.xaml, Styles.xaml), Images/fondo_app.png, AppIcon/, Splash/, Raw/
ec/edu/monster/
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto: lo que ve el resto del cliente
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── ws/            ConfiguracionServicio, TraductorRed (+ IXCliente y XClienteWS del dominio)
│   └── generado/  <servicio>/Reference.cs                 proxy de dotnet-svcutil: se regenera, no se edita
├── controlador/   ControladorBase, Comando (+ los del dominio)   ViewModel de cada pantalla: estado, comandos y llamadas al servicio
├── vista/         PaginaPrincipal, Pantalla, CampoTexto, ZonaEstado, Textos (+ las vistas del dominio)   única capa que conoce XAML y MAUI
└── pruebas/       xUnit + Moq, en el mismo proyecto (solo se compilan en el destino de Windows)
wsdl/                           copias del WSDL y de los esquemas de cada servicio (de aquí se genera el proxy)
docs/                           CONVENCION_SOAP.md, CONVENCION_PRUEBAS.md y CLIENT_STYLES_DOTNET.md (copias vigentes) y PROJECT.md (esqueleto del proyecto)
README.md                       esta guía: el único documento que queda en la raíz
```

Dependencias permitidas: `vista → controlador → servicios → ws`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `ws` ni `dto`**: solo conoce a su controlador (su `BindingContext`), a `Textos` y a los estilos.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo la implementación de `ws` conoce el protocolo, los tipos generados y las excepciones de WCF.
Si el WSDL cambia solo se tocan `ws`, `dto` y `servicios`. Los controladores nunca importan `ws` ni `Microsoft.Maui`: hablan con los servicios y exponen propiedades y comandos; el código subyacente de una vista solo hace tareas de plataforma (foco y teclado).

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas`. Solo se compilan (y sus paquetes solo se agregan) en el destino de Windows: un proyecto Android no se ejecuta con `dotnet test`, no deben empaquetarse en el APK y el SDK de pruebas rompe la compilación de Android (`XAPRAS7009`). `GenerateProgramFile=false` (solo en Windows) porque el punto de entrada lo genera MAUI.
> **Las vistas y la página no se prueban con xUnit** (necesitan la aplicación en ejecución); se verifican a mano (§7).

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems()` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **WSDL y proxy** — con el servidor en marcha, guardar por servicio el WSDL y sus esquemas: `<URL>?wsdl`, `<URL>?xsd=xsd0` y `<URL>?xsd=xsd1` (un servidor CoreWCF; uno Java publica un solo `.xsd`), como `wsdl/<Servicio>.wsdl`, `<Servicio>.xsd0.xsd`, `<Servicio>.xsd1.xsd`.
   En cada `.wsdl`, **reescribir los `schemaLocation` absolutos a los nombres de archivo locales**. Instalar la herramienta una vez (`dotnet tool install -g dotnet-svcutil`, 8.0.0; con el SDK 10 conviene `DOTNET_ROLL_FORWARD=LatestMajor`) y, **en una carpeta de ruta corta** con un proyecto `net10.0` descartable, por cada servicio:
   ```
   dotnet-svcutil wsdl/Consulta.wsdl -d . -o gen/consulta/Reference.cs -n "*,ec.edu.monster.ws.generado.consulta" --noLogo
   ```
   Copiar cada `Reference.cs` a `ec/edu/monster/ws/generado/<servicio>/`. **No dejar que `dotnet-svcutil` modifique este proyecto** (añade paquetes `System.ServiceModel.*` 4.10 que no se usan). Lo generado: la clase `…Client` con constructor `(Binding, EndpointAddress)`, métodos `<operación>Async` con el nombre de la operación en minúscula inicial (`obtenerItemsAsync`), tipos con propiedades en minúscula (`estado`, `codigo`, `mensaje`) y las listas como clases que heredan de `List<T>` (se usa `Count`, no `Length`).
   **El código generado no se edita.** Cuando el servidor cambia su contrato: volver a guardar el WSDL y repetir.
   **Prueba de humo en Android antes de seguir** (Debug **y Release**: el recorte de código puede romper la serialización de WCF, que usa reflexión): una sola operación, en el emulador.
2. **Interfaz y envoltorio** — `ws/IXCliente.cs` (interfaz propia con las operaciones que usa el cliente, retorno `Task<DTO propio?>`) y `ws/XClienteWS.cs`, que implementa la interfaz sobre el cliente generado, copia cada respuesta generada a un DTO propio y **traduce los fallos de red con `TraductorRed`**. Lo único que ven el servicio y las vistas es la interfaz. **El cliente no usa la URL de `Reference.cs`**: se crea con su propio binding y la URL configurada (R9, R10):
   ```csharp
   public sealed class XClienteWS : IXCliente
   {
       private readonly Func<ConsultaClient> _crearProxy;          // clase generada por dotnet-svcutil

       public XClienteWS(Func<ConsultaClient> crearProxy) { _crearProxy = crearProxy; }

       public static IXCliente Crear()
       {
           string url = ConfiguracionServicio.UrlConfigurada();    // o UrlConsulta() si hay varios servicios
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
   Los nombres exactos (`ConsultaClient`, `obtenerItemsAsync`, `RespuestaLista`) los decide `dotnet-svcutil` a partir del WSDL y solo se usan en este archivo. Cuando hay varios servicios, cada `Reference.cs` está en su espacio de nombres: importar solo el del servicio en cada envoltorio.
3. **DTO propio** — clase `…Dto` en `dto` que extienda `RespuestaBaseDto`, con propiedades `{ get; set; }`, todas texto y `string?` (los campos solo existen si hubo éxito); una lista se inicializa vacía (`public List<string> Elementos { get; } = [];`): **nunca nula**.
4. **Servicio** — `servicios/XServicio : ServicioBase` (con sufijo `Async` en lo que llama a la red; los métodos públicos, `virtual`: Moq los necesita):
   ```csharp
   public virtual async Task<IReadOnlyList<string>> ItemsAsync()
   {
       RespuestaListaDto respuesta = Verificar(await _cliente.ObtenerItemsAsync());
       return respuesta.Elementos;
   }
   ```
   - `Verificar(...)` devuelve la respuesta si `EsOk`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`.
   - **No captura excepciones de red**: ya llegan traducidas a `RED` desde `ws`.
   - Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante: el teclado numérico puede ofrecer la coma) y exigir la sesión si el proyecto tiene login. Las reglas del dominio las decide el servidor y el cliente muestra su mensaje.
5. **Controlador** — `controlador/XControlador : ControladorBase` (ViewModel de la pantalla):
   - Expone propiedades de entrada (`Asignar(ref campo, valor)` notifica el cambio), una propiedad `…Invalido` por campo y los comandos (`new Comando(() => _ = AceptarAsync())`). Recibe el servicio y la navegación como **delegados** (`Action`): los controladores no se conocen entre sí ni conocen la pantalla siguiente.
   - Valida campos vacíos **antes** de llamar (`Vacio(...)`; si faltan, no se llama al servicio): marca el primero vacío, `MostrarError(Textos.CampoObligatorio(...))` y `PedirFoco(...)`.
   - Llama con `EjecutarAsync(textoOcupado, () => servicio.OperacionAsync(...), alExito)`: la pantalla queda `Ocupado` (todos sus controles deshabilitados), una segunda acción se ignora, un `ExcepcionServicio` se muestra con `Manejar` (**solo el mensaje**, nunca el código) y cualquier otra excepción con el texto genérico de `Textos`. Un fallo de red conserva lo que el usuario capturó (salvo los datos sensibles).
   - `AlMostrar()` deja la pantalla limpia cada vez que se muestra; `OcultarResultado()` y `LimpiarMarcas()` se sobrescriben para limpiar lo propio de la pantalla.
   - Si el proyecto define una sesión, `Manejar` se sobrescribe para tratar ahí el código de sesión no válida (la plantilla no define ninguna).
6. **Vista** — `vista/XVista.xaml` (`ContentView`) **sin lógica**, construida solo con los estilos de `Styles.xaml` (§5) y con el controlador como `BindingContext`:
   - Raíz: un `Grid` con `IsEnabled="{Binding NoOcupado}"`; dentro, un `VerticalStackLayout`. Panel de encabezado (`PanelSobreImagenEstilo`, o `PanelSuperficie` en el acceso), etiquetas (`EtiquetaCampo`), campos `<vista:CampoTexto Text="{Binding X, Mode=TwoWay}" Invalido="{Binding XInvalido}" />` (`EsClave="True"` para una clave, `Teclado="Numeric"` para un importe), botones apilados (el principal y `Cancelar` debajo, separados 16, `HorizontalOptions="Center"`) y, debajo, `<vista:ZonaEstado Fondo="{StaticResource PanelSobreImagen}" />` (`Superficie` en el acceso).
   - Un resultado que cambia un valor se dibuja con una tarjeta (estilos `Tarjeta*`, ancho completo, `BindableLayout`); una lista de registros, con **una tarjeta compacta por registro** dentro del mismo `ScrollView` (no un `CollectionView`); un menú, con botones `BotonMenu` de 96 de alto (`docs/CLIENT_STYLES_DOTNET.md` §5.11 a §5.13 y §7.1).
   - El código subyacente solo lleva el foco (`controlador.FocoSolicitado += campo => …Enfocar()`) y oculta el teclado cuando la pantalla pasa a ocupada (`Desenfocar()` de cada `CampoTexto`).
7. **Composición y navegación** — en `App.xaml.cs`, `Componer`: crear los clientes (`XClienteWS.Crear()`), los servicios, los controladores y las vistas; registrarlos en `_pantallas` (un miembro de `Pantalla` por pantalla) y mostrar la primera con `Ir(...)`. La navegación se decide aquí: cada controlador avisa con un delegado y `Ir` limpia el controlador de destino (`AlMostrar()`) y lo muestra en `PaginaPrincipal`. Ajustar `EsAcceso` y `VaArriba` en `vista/Pantalla.cs`.
8. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto, sin sesión).
   - **Controladores:** el servicio concreto se sustituye con Moq (sus métodos públicos son `virtual`; se crea con `new Mock<XServicio>(null!)`, los constructores solo asignan campos). Se prueban la validación de vacíos, el mensaje sin código, el estado ocupado y la navegación por delegados. Molde: `ControladorBasePruebas`.
   - **Envoltorio:** `Convertir` se prueba campo a campo con los tipos generados.
   - El contrato y el envoltorio de protocolo **no** se prueban con código: se verifican a mano con la lista de `docs/CONVENCION_PRUEBAS.md` §6.3. El criterio de qué probar y con cuánto detalle está en ese documento.
   - La vista se verifica a mano (§7): un recurso mal escrito en un XAML puede **compilar** y fallar al abrir la pantalla.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, tiempo agotado y `Fault` son `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio; nunca `double.Parse` con la cultura actual).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de la interfaz:** `async/await` con las operaciones `…Async`. Un fallo de red no cierra la aplicación.
- **Un fallo al cerrar el canal después de una operación exitosa no es un error de la operación** (`TraductorRed` lo descarta con `Abort`): una operación que ya cambió datos nunca debe mostrarse como «servidor no disponible».
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

`ConfiguracionServicio.UrlPorDefecto()` arma `http://<host>:<Puerto>/<RutaServicio>` con el host de la plataforma. Sin recompilar: variable de entorno `WS_URL` (solo útil en el destino de Windows; con varios servicios, `WS_URL_<SERVICIO>`).

| Entorno | Host | Notas |
|---|---|---|
| Emulador de Android | `10.0.2.2` (el PC anfitrión; **no** `localhost`) | automático (`DeviceType.Virtual`) |
| Teléfono físico | `IpPc` (IPv4 del PC en la red local, `ipconfig`) | misma Wi-Fi; la IP cambia al cambiar de red; recompilar al cambiarla |
| Windows | `localhost` | |

- **HTTP sin cifrar:** `android:usesCleartextTraffic="true"` en el manifiesto (ya está) y los permisos `INTERNET`/`ACCESS_NETWORK_STATE`. Si el servidor pasa a HTTPS, quitar el primero y asegurar que el dispositivo confía en el certificado del servidor (el certificado de desarrollo de `dotnet dev-certs` es de confianza solo en el PC). *HTTPS no se probó en esta plantilla.*
- **El servidor debe escuchar fuera de `localhost`:** con Kestrel, perfil `http` con `applicationUrl` `http://0.0.0.0:<puerto>`; con IIS Express, un binding comodín (`*:<puerto>:*` en `applicationhost.config`), Visual Studio como administrador (o `netsh http add urlacl url=http://*:<puerto>/ user=Todos`) y la regla de firewall (`context/entornos/IIS_CONFIG.md` §3).
- **Firewall de Windows** (PowerShell como administrador): `netsh advfirewall firewall add rule name="Servidor SOAP" dir=in action=allow protocol=TCP localport=<puerto>`; la red debe estar en categoría **Privada**.
- **Probar antes de tocar la app:** abrir `http://<host>:<puerto>/<RutaServicio>?wsdl` en el navegador del emulador (`10.0.2.2`) o del teléfono (IP del PC). Si carga el XML, la app también conectará. `400 Invalid Hostname` indica un IIS Express sin el binding comodín.
- **El WSDL no se usa en ejecución:** un cliente WCF no lo descarga; usa el binding y la URL indicados. Con el servidor apagado el proxy se crea igual y la llamada falla con `RED` tras el tiempo límite (10 s), que `TraductorRed` traduce a «Servidor no disponible, intente más tarde».
- **HTTP y HTTPS:** el esquema de la URL decide el modo de seguridad del binding (`ConfiguracionServicio.CrearBinding`). La validación del certificado **no** se desactiva.

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
7. **Repetir los puntos 1 y 2 en Release** (el recorte de código puede romper enlaces por reflexión o la serialización de WCF, y no falla en Debug).
8. En teléfono físico: `IpPc`, firewall y red Privada.

Un agente que no pueda abrir el emulador debe decir explícitamente qué verificó (pruebas, compilación, arranque, contra qué servidor) y que **no vio la aplicación real**.
Con `adb` se puede automatizar el recorrido (`adb shell input tap/text`, `adb exec-out screencap -p`, `adb shell uiautomator dump`); en Git Bash definir `MSYS_NO_PATHCONV=1` para que no convierta rutas como `/sdcard`.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_SOAP.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `servicios`.
Lo que **el código ya cumple** y lo que **debe cumplir el servidor**:

- SOAP **1.1**, *document/literal wrapped*, `BasicHttpBinding`. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): lo declara el esquema del servidor (`elementFormDefault="qualified"`) y el proxy generado lo respeta. Si la copia del esquema no lo declara, los campos llegan `null` sin error (R3).
- **Namespace (TNS) fijo `http://ws.monster.edu.ec/`** en todos los servicios del equipo (R2).
- **SOAPAction = `TNS + Servicio + "/" + operación`**: viene en cada operación del WSDL y el proxy la envía (R4); un servidor .NET rechaza la acción vacía con HTTP 500.
- **El cliente no depende del nombre del puerto ni del binding** (R10): se crea con su propio binding y la URL configurada.
- **La URL completa del servicio** se recibe por configuración y sustituye a la del `Reference.cs`; el cliente no asume puerto ni «base + servicio» (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los campos propios de la respuesta nunca se llaman `estado`, `codigo` ni `mensaje`.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R7); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R8); los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5). Si el servidor cambia su contrato hay que regenerar el proxy y actualizar el contrato y los DTO del cliente.
- Un WSDL de CoreWCF trae el esquema de serialización de Microsoft (`xsd1`) y una lista como clase que hereda de `List<T>`; un WSDL Java, un solo `.xsd`. Con campos `nillable` el proxy sigue usando `string`.
- Los campos que pueden faltar (listas, datos opcionales) llegan `null` en un error: no darlos por existentes.
- **Autenticación y sesión:** no las asume la plantilla. Cómo se identifica quien opera (token, credenciales en cada operación, nada), dónde vive la sesión y qué hace el cliente ante una sesión no válida lo define cada servidor y su cliente lo implementa en `servicios`, `modelo` y `controlador` (`Manejar`).
- Si el proxy generado no funcionara en Android (sobre todo en Release), la alternativa documentada es `HttpClient` con el sobre SOAP armado a mano, aplicando la receta de `docs/CONVENCION_SOAP.md` §4: operación y parámetros con prefijo de namespace, `SOAPAction` con la URI y lectura por nombre local.
- Para un servicio **REST** usar la plantilla `rest_dotnet_movil`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `APT2098` / `APT2261` al compilar para Android (`…microsoft_maui_essentials_fileprovider_file_paths.xml.flat: failed to open file`) | La ruta del proyecto es demasiado larga para `aapt2` (no soporta rutas largas; `LongPathsEnabled` no ayuda). Mover el proyecto a una ruta corta, borrar `bin/` y `obj/` |
| `XA0010: No hay ningún dispositivo disponible` | No hay emulador ni teléfono conectado: arrancar el emulador; `adb devices` debe listarlo |
| «Servidor no disponible» al llamar una operación | Servidor apagado, URL distinta de la real, host equivocado (`localhost` no sirve en Android), firewall o red Pública, o `IpPc` desactualizada. Abrir `<URL>?wsdl` en el navegador del dispositivo |
| `Cleartext HTTP traffic to … not permitted` | Falta `android:usesCleartextTraffic="true"` en `Platforms/Android/AndroidManifest.xml` (la plantilla lo trae) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín (§6) |
| Otro dispositivo recibe timeout y desde el PC con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| `NETSDK1005 … no tiene un destino para net10.0-android` | Se restauró con `dotnet restore -f` o se compiló con `-f` sin restaurar antes: `dotnet restore` sin `-f` |
| La pantalla compila pero falla al abrirse | Un recurso (`StaticResource`) inexistente o una clave repetida: se detecta al abrir la vista, no al compilar. Probar cada pantalla nueva |
| Funciona en Debug pero falla en Release | Recorte de código (*trimming*): enlaces por reflexión o serialización de WCF. Si es el proxy, usar la alternativa de §8 |
| El campo de texto muestra un subrayado en Android | Falta el `AppendToMapping("SinSubrayado", …)` de `MauiProgram.cs` |
| Respuesta con campos vacíos / `null` | El servidor no califica los campos (R3) o el proxy está desactualizado: regenerar desde el WSDL actual |
| HTTP 500 *ActionNotSupported* | Se envía `SOAPAction` vacía: el proxy no es el generado o se alteró el atributo `Action` |
| El servidor responde un error de formato con un número | Se envió coma decimal: normalizarla a punto en el servicio |
| `dotnet-svcutil` falla (`MSB3030`, ruta demasiado larga, o pide otro runtime) | Generar en una carpeta de ruta corta y copiar solo los `Reference.cs`; definir `DOTNET_ROLL_FORWARD=LatestMajor` |
| `dotnet-svcutil` añade paquetes `System.ServiceModel.*` 4.10 a un proyecto | Se ejecutó dentro del proyecto: generar en una carpeta descartable y copiar solo los `Reference.cs` |
| 26 avisos `NU1902`/`NU1903` al restaurar | Dependencias transitivas de `System.ServiceModel.*`: `Pkcs` y `Xml` ya van fijadas en 10.0.14 en el `.csproj` |
| `ListaX` no tiene `Length` | Es una lista (`List<T>`): usar `Count` |
| Errores por tipos duplicados (`RespuestaBase`…) o proxies que se pisan | Varios servicios generados en el mismo espacio de nombres: darle a cada uno el suyo (`-n "*,ec.edu.monster.ws.generado.<servicio>"`) |
| `CS1729` / `CS0118` en el destino de Windows | Falta `<CsWinRTAotOptimizerEnabled>false</CsWinRTAotOptimizerEnabled>` en el `.csproj` (ya está) |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el grupo del destino de Windows del `.csproj` |
| `XAPRAS7009` al compilar Android con pruebas | Los paquetes de pruebas están referenciados en el destino de Android: solo van en el de Windows (`.csproj`) |
| `dotnet test` en Android no encuentra pruebas | Las pruebas solo existen en el destino de Windows: `-f net10.0-windows10.0.19041.0` |
| La carpeta `pruebas` no aparece en el Explorador de soluciones de Visual Studio | Falta en el `.csproj` el `<None Include="ec\edu\monster\pruebas\**\*.cs" />` junto al `Compile Remove` (en Android la carpeta se excluye de la compilación y, sin él, deja de ser un elemento del proyecto) |
| El teclado del emulador abre un tutorial de lápiz y ensucia los campos | Gboard muestra su tutorial de escritura a mano la primera vez: cancelarlo; si la prueba automatizada deja texto basura, reiniciar la aplicación |
| La aplicación instalada no cambia de nombre o de ícono | `ApplicationTitle`/`ApplicationId` se aplican al instalar: desinstalar la anterior y volver a ejecutar |
