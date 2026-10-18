# Plantilla: cliente móvil .NET (MAUI) para servicios SOAP

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita (acceso, navegación, estilos,
transporte, sesión y pruebas) y deja claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** C# 14, `net10.0-android` y `net10.0-windows10.0.19041.0`, `Nullable` e `ImplicitUsings` habilitados. **Interfaz:** .NET MAUI (XAML) sin librerías de terceros. **Arquitectura:** MVC por capas con MVVM (el controlador hace de ViewModel), navegación con `Shell`.
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` `10.0.652802`) con un **proxy generado desde el WSDL** por `dotnet-svcutil` (§4, paso 1). No se escribe el contrato a mano.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (tema **oscuro**, tipografía, medidas, componentes). Implementado en `Resources/Styles/Colors.xaml` y `Styles.xaml`.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla. Con un proxy generado, el generador cumple por sí mismo R3 y R4.
- **Verificada:**
  - `dotnet test -f net10.0-windows10.0.19041.0` → **34 pruebas xUnit en verde**, sin servidor ni interacción: URL, binding http/https, traducción del fallo de red a `RED`, `ServicioBase`, acceso (con un autenticador falso) y habilitación de comandos.
  - `dotnet build -f net10.0-android` → **0 errores y 0 avisos**; el destino de Windows también compila (lo hace `dotnet test`).
  - **En el emulador de Android** (Debug): la pantalla de acceso se ve con el estilo, con el nombre y la imagen de la plantilla; con credenciales cualquiera muestra «Plantilla sin dominio…»; con un autenticador que acepta (solo en una copia de prueba) pasa a `InicioVista`, **Atrás no sale de la sesión** y **«Cerrar sesión» vuelve a un acceso limpio**.
  - **No se ha hecho:** una llamada a un servidor real desde esta plantilla (no tiene proxy ni dominio), ejecutarla en Windows, probarla en un teléfono físico, ni compilarla en **Release** (con recorte de código). El proxy WCF y las pantallas de la que se derivó (el cliente móvil del conversor de unidades) sí se probaron en emulador, **en Debug y en Release**, contra un servidor .NET real (IIS Express y Kestrel).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta**: `aapt2` (Android) y `dotnet-svcutil` fallan con rutas largas (§9).
2. **Renombrar** `soap_dotnet_movil.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 con las cargas de trabajo `android` y `maui-windows`, el SDK de Android (con un emulador) y el JDK; la primera vez restaura los paquetes (Internet).
4. **Nombre de la aplicación:** `ApplicationTitle` en el `.csproj` (nombre visible en el dispositivo) y `vista/Textos.cs` → `NombreAplicacion` (título de la pantalla de acceso y del `Shell`); mantenerlos iguales.
   **Identificador del paquete:** `ApplicationId` en el `.csproj` (`ec.edu.monster.movil` de la plantilla). Android trata un `ApplicationId` distinto como otra aplicación: desinstalar la anterior del dispositivo.
5. **Configurar el servidor:** en `ec/edu/monster/ws/ConfiguracionServicio.cs`, `Puerto` y `Ruta` (la URL completa se arma con el host de cada plataforma: `10.0.2.2` en el emulador, `IpPc` en un teléfono físico, `localhost` en Windows) y, para un teléfono físico, `IpPc`.
6. **Sustituir la imagen** `Resources/Images/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6), o quitar el `Image` de la plantilla `PlantillaPagina` de `Resources/Styles/Styles.xaml` para no usarla.
7. **Generar el proxy** desde el WSDL del servidor (§4, paso 1) y conectar el servicio en `MauiProgram.cs` y `Navegacion.cs` (§4, pasos 6 y 7).
8. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
9. **Probar el servidor** (el WSDL abre en el navegador del PC y del emulador, §6) y ejecutar en el emulador. Con la plantilla sin tocar abre el acceso y responde que falta conectar el dominio.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Acceso (compartido)** | `vista/LoginVista`, `controlador/LoginControlador`, `servicio/IAutenticador` | No; el servicio del dominio implementa `IAutenticador` |
| **Transporte SOAP** | `ws/ClienteWS` (binding por esquema http/https, timeouts, `LlamarAsync` con traducción de fallos de red) | No |
| **URL del servicio** | `ws/ConfiguracionServicio` (`Puerto`, `Ruta`, `IpPc`, host por plataforma, variable `WS_URL`, normalización) | `Puerto`, `Ruta` e `IpPc` siempre; el resto no |
| **Proxy generado** | — (`ws/Reference.cs`, generado por `dotnet-svcutil` desde `wsdl/`) | **Se genera** por proyecto; **no se edita** |
| **Fábrica del cliente del dominio** | — (`ws/ClienteWSXxx` + `ws/IClienteXxx` sobre el proxy) | **Se crea** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`ExigirToken`, `Verificar`, `ExigirRespuesta`, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Base MVVM** | `controlador/ControladorBase` (`Ocupado`, `Mensaje`, `HayMensaje`), `ComandoAsincrono`, `Comando` | No |
| **Navegación** | `vista/Navegacion` (crea los controladores, entrega los delegados de navegación), `AppShell.xaml(.cs)` (rutas) | Se agregan las pantallas del dominio |
| **Estilos** | `Resources/Styles/Colors.xaml` (tokens), `Styles.xaml` (estilos y `PlantillaPagina`), `vista/PanelMensaje`, `vista/NegacionConvertidor`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` |
| **Composición** | `MauiProgram.cs` (DI), `App.xaml(.cs)` (tema oscuro fijo) | Se conecta el servicio y se registran las pantallas del dominio |
| **Plataforma Android** | `Platforms/Android/AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`, `usesCleartextTraffic`), `colors.xml` (barra de estado) | `usesCleartextTraffic` solo si el servidor usa HTTPS |
| **Pantalla de ejemplo (dominio)** | `vista/InicioVista`, `controlador/InicioControlador` | **Se reemplaza** por las pantallas del dominio |
| **Pruebas** | `pruebas/` (URL, binding, transporte, `ServicioBase`, acceso, comandos) | Se conservan; se agregan las del dominio |
| **Dominio (vacío)** | — | Nuevos `ws/`, `servicio/`, `controlador/`, `vista/`, `modelo/` |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec MauiProgram.cs AppShell.xaml.cs soap_dotnet_movil.csproj`).

## 3. Estructura

```
MauiProgram.cs, App.xaml(.cs)      punto de composición (DI: Sesion, servicio, Navegacion, pantallas) y tema
AppShell.xaml(.cs)                 Shell: pantalla inicial (login) y rutas de las demás
Platforms/                         Android (manifiesto, actividad) y Windows
Resources/                         Styles/ (Colors.xaml, Styles.xaml), Images/fondo_app.png, AppIcon/, Splash/, Raw/
wsdl/                              copia del WSDL del servidor (fuente de verdad del contrato); versionada
docs/CLIENT_STYLES_DOTNET.md       guía de estilos
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion                                  entidades: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; ExigeLogin, ServidorNoDisponible
├── ws/            ClienteWS, ConfiguracionServicio (+ Reference.cs generado, IClienteXxx y ClienteWSXxx del dominio)
├── servicio/      ServicioBase, IAutenticador (+ los servicios del dominio)    única capa que conoce los tipos generados
├── controlador/   ControladorBase, ComandoAsincrono, Comando, LoginControlador, InicioControlador
├── vista/         Navegacion, LoginVista, InicioVista, PanelMensaje, Textos, NegacionConvertidor
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo (solo se compilan en el destino de Windows)
```

Dependencias permitidas: `vista → controlador → servicio → ws`; `modelo` y `excepciones` no dependen de nadie.
**Los tipos generados (el «Dto» de la familia) viven en `ws/Reference.cs` y no salen de `servicio/`**; no hay carpeta `dto` ni clases `Dto` a mano.
Los controladores nunca importan `ws` ni `Microsoft.Maui`: hablan con los servicios y exponen propiedades y comandos. La vista (XAML) no tiene lógica de negocio ni llama al servidor;
su código subyacente solo hace tareas de plataforma (entregar la lectura de la contraseña, crear el controlador al aparecer, el botón Atrás).

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas`. Solo se compilan (y sus paquetes solo se agregan) en el destino de Windows: un proyecto Android no se ejecuta con `dotnet test` y no deben empaquetarse en el APK. `GenerateProgramFile=false` porque el punto de entrada lo genera MAUI.
> **La vista y `Navegacion` no se prueban con xUnit** (necesitan la aplicación en ejecución); se verifican a mano (§7).

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems(token)` que devuelve una lista y su pantalla. Se sigue **siempre el mismo orden**, de abajo hacia arriba.

1. **Proxy generado** — guardar el WSDL y generar:
   ```
   curl -o wsdl/Servicio.wsdl "<URL del servicio>?singleWsdl"      # servidor .NET: un solo archivo con los esquemas
   dotnet tool install dotnet-svcutil --tool-path <carpeta corta>\herr
   dotnet-svcutil wsdl/Servicio.wsdl -n "*,ec.edu.monster.ws" -d ec/edu/monster/ws -o Reference.cs -nl
   ```
   - Un servidor **Java** no entrega `?singleWsdl`: guardar `?wsdl` **y** el XSD que importa (`schemaLocation`) y cambiar el `schemaLocation` a un nombre relativo.
   - La herramienta modifica el `.csproj` (añade `System.ServiceModel.*` `4.10.*`): **dejar solo** `System.ServiceModel.Http` y `Primitives` `10.0.652802` (y las dos versiones corregidas de `Cryptography`, que ya están).
   - Generar desde una copia en una ruta corta si la del repositorio es larga (§9); puede hacer falta `DOTNET_ROLL_FORWARD=Major`. Conservar `dotnet-svcutil.params.json` (documenta cómo regenerar; ajustar la ruta del WSDL) y **no editar** `Reference.cs`.
   - El proxy trae `RespuestaBase` (`estado`, `codigo`, `mensaje`), los tipos de cada operación, la interfaz del contrato y `ServicioXxxClient` (derivada de `ClientBase<T>`).
   - **Prueba de humo en Android antes de seguir** (Debug **y Release**: el recorte de código puede romper la serialización de WCF, que usa reflexión): una sola operación, en el emulador. En el conversor de unidades funcionó en ambos.
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
3. **Servicio** — `servicio/ServicioXxx : ServicioBase, IAutenticador` (recibe `IClienteXxx` y `Sesion`). Es la **única** capa que ve los tipos generados:
   ```csharp
   public async Task IniciarSesionAsync(string usuario, string contrasenia)        // IAutenticador
   {
       var r = ExigirRespuesta(await _cliente.LoginAsync(usuario, contrasenia));
       Verificar(r.estado, r.codigo, r.mensaje);
       if (string.IsNullOrEmpty(r.token)) throw new ExcepcionServicio("El servidor no entregó un token de sesión", "ERR-500");
       Sesion.Token = r.token;
   }

   public async Task<IReadOnlyList<string>> ObtenerItemsAsync()
   {
       var token = ExigirToken();
       var r = ExigirRespuesta(await _cliente.ObtenerItemsAsync(token));
       Verificar(r.estado, r.codigo, r.mensaje);
       return r.elementos?.ToList() ?? [];     // las listas pueden venir ausentes
   }
   ```
   - `ExigirToken()` lanza `AUTH-002` sin llamar al servidor si la `Sesion` no tiene token. `Verificar(...)` deja pasar si `estado` es `OK`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor y descarta el token si `ExigeLogin`.
   - Aquí van las reglas **del cliente** (campo vacío, formato, `NormalizarValor`: coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
4. **Controlador** — `controlador/ItemsControlador : ControladorBase` (molde: `LoginControlador`):
   - Recibe el servicio y la navegación como **delegados** (`Action`, `Action<string>`); los controladores no se conocen entre sí ni conocen `Shell`.
   - Comandos con `ComandoAsincrono(ejecutar, () => !Ocupado, this)`: el **`this` es obligatorio** en todo comando cuya habilitación dependa de `Ocupado`. Sin él, un botón enlazado mientras la pantalla cargaba queda deshabilitado para siempre (el comando solo vuelve a consultarse cuando avisa).
   - Una pantalla que **carga datos al abrirse** expone `Task CargarAsync()` que **no lanza**: pone `Ocupado`, captura `ExcepcionServicio` (solo `ex.Message` en `Mensaje`, nunca el código) y cualquier otra excepción (mensaje genérico), y si `ex.ExigeLogin` avisa con un delegado `Action<string> alExpirarSesion` para volver al login con ese mensaje.
   - Un fallo de red conserva lo que el usuario capturó.
5. **Vista** — `vista/ItemsVista.xaml` (`ContentPage`) **sin lógica**, construida solo con los estilos de `Styles.xaml` (§5) y con `x:DataType="ctl:ItemsControlador"` (enlaces compilados). Moldes: `LoginVista` e `InicioVista`:
   - Raíz: `ControlTemplate="{StaticResource PlantillaPagina}"` y `Shell.NavBarIsVisible="False"`; contenido dentro de un `ScrollView` con `Padding="24"` y `Spacing="16"`.
   - Enlazar `IsEnabled` de la raíz a `Ocupado` con `Converter={StaticResource Negacion}`, mostrar el mensaje con `<vista:PanelMensaje Texto="{Binding Mensaje}" IsVisible="{Binding HayMensaje}" />` y el progreso con un `ActivityIndicator`.
   - **Campo de texto:** el texto va como `Placeholder` dentro de un `Border Style="{StaticResource Campo}"`, sin etiqueta encima. **Selector** (`Picker`): conserva su etiqueta (no admite placeholder) y lleva la flecha `▼` (§5.5 del documento de estilos):
     ```xml
     <Border Style="{StaticResource Campo}">
         <Grid>
             <Picker ItemsSource="{Binding Opciones}" SelectedItem="{Binding Elegida}" Margin="0,0,28,0" />
             <Label Text="▼" TextColor="{StaticResource CampoFlecha}" FontSize="12" InputTransparent="True"
                    HorizontalOptions="End" VerticalOptions="Center" />
         </Grid>
     </Border>
     ```
   - **Lista de acciones** (un botón primario por dato del servidor): `BindableLayout.ItemsSource` + `DataTemplate x:DataType="x:String"` con un `Button`; el comando del controlador se enlaza con `{Binding Comando, Source={RelativeSource AncestorType={x:Type ctl:ItemsControlador}}, x:DataType=ctl:ItemsControlador}` y `CommandParameter="{Binding .}"`.
   - Pantalla de nivel superior tras el acceso (no se vuelve con Atrás): `OnBackButtonPressed() => true` y `Shell.SetBackButtonBehavior(this, new BackButtonBehavior { IsVisible = false })`, como `InicioVista`. Sale por un botón secundario («Cerrar sesión» o «Regresar»).
   - Un `Entry` numérico usa `Keyboard="Numeric"`; el texto se envía tal cual y `ServicioBase.NormalizarValor` cambia la coma por punto.
6. **Navegación** — en `AppShell.xaml.cs` registrar la ruta (`Routing.RegisterRoute("items", typeof(ItemsVista))`) y en `vista/Navegacion.cs` agregar `CrearItems()`, que entrega los delegados: `alExpirarSesion: mensaje => IrAlLogin(mensaje)`, y para ir a otra pantalla `Ir("detalle", new Dictionary<string, object> { ["id"] = id })` (la vista destino implementa `IQueryAttributable` y crea su controlador al recibir el parámetro). `Navegacion` recibe por constructor los servicios del dominio.
   Una pantalla que carga datos al abrirse lo hace en `OnAppearing` (una sola vez, con una bandera) llamando a `CargarAsync()` del controlador que le entregó `Navegacion`.
7. **Composición** — en `MauiProgram.cs`: registrar el cliente (`AddSingleton<IClienteXxx>(_ => new ClienteWSXxx())`), el servicio y `IAutenticador` (`AddSingleton<IAutenticador>(sp => sp.GetRequiredService<ServicioXxx>())`), las vistas (`AddTransient<ItemsVista>()`), y **borrar `AutenticadorPendiente`**, `InicioVista` e `InicioControlador` (y su ruta y su `CrearInicio`). La `Sesion` es **una sola** (`Singleton`). Cambiar la pantalla a la que va el acceso: `Ir(RutaInicio)` en `Navegacion.CrearLogin`.
8. **Pruebas** (en `ec/edu/monster/pruebas/`):
   - Un doble escrito a mano de `IClienteXxx` que devuelve respuestas simuladas, para el servicio (OK, error de negocio, red, sesión vencida, coma enviada como punto, sin sesión no se consulta nada).
   - Los controladores con ese servicio doble (error de negocio solo con mensaje, red, token vencido, `Ocupado`).
   - **`CanExecute` de cada comando que cargue datos al abrir** (molde: `ComandosHabilitadosPruebas`).
   - Una **prueba de humo** contra un servidor falso local (CoreWCF) con el mismo contrato detecta un WSDL desactualizado (opcional).
   - La vista se verifica a mano (§7): un recurso mal escrito en un XAML puede **compilar** y fallar al abrir la pantalla.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se muestra. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual): el teclado numérico puede ofrecer la coma.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de la interfaz:** `async/await` con las operaciones `…Async`. Un fallo de red no cierra la aplicación.
- La **contraseña** no se enlaza: la vista entrega a `LoginControlador.LeerContrasenia` cómo leerla, y no se guarda en el controlador.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo referencian recursos** (`{StaticResource …}`); no colorean ni dimensionan componentes sueltos. `App.xaml` carga `Colors.xaml` y `Styles.xaml`.

| Recurso | Para qué |
|---|---|
| `PlantillaPagina` (`ControlTemplate` de la página) | Fondo de la pantalla: imagen `fondo_app.png` recortada (`AspectFill`) sobre el color `Fondo` |
| `BotonPrimario` | Acción principal de la pantalla (relleno `Primario`, texto blanco). En una lista de acciones, uno por elemento |
| `BotonSecundario` | Salida o regreso: «Cerrar sesión», «Regresar» (sólido `Secundario`) |
| `Panel` + `TituloAplicacion` / `TituloPantalla` | Encabezado: título de la aplicación (acceso, centrado) y de pantalla |
| `Etiqueta`, `Campo` (+ `Entry`/`Picker` transparentes dentro de un `Border`) | Etiqueta de campo y campos; el texto de un `Entry` va como `Placeholder` |
| `PanelMensaje` (componente) | Mensaje de error o validación con barra de 4 en `Error`; oculto hasta que hay texto (`HayMensaje`) |
| `Panel` + `Resultado` | Resultado de una operación (centrado, altura mínima 72) |
| `ActivityIndicator` | Indicador de carga; deshabilitar la pantalla mientras `Ocupado` |

- **Tema oscuro** fijo (`UserAppTheme = Dark` en `App.xaml.cs`); no hay selector. Para la presentación clara, cambiar los valores de `Colors.xaml` por los de `docs/CLIENT_STYLES_DOTNET.md` §2.3 (y no usar imagen de fondo).
- **Sin fuentes propias:** se usa la del sistema (Roboto en Android). No agregar `OpenSans` ni la paleta de ejemplo de MAUI.
- **Disposición:** una sola columna; márgenes de 24 en el layout, controles de 48 de alto mínimo, radio 8, sin sombras. Los paneles usan `PanelSobreImagen` porque hay imagen debajo.
- **La imagen** va en `Resources/Images/` (MAUI la convierte a recurso de cada plataforma; nombre en minúsculas, sin espacios). Si falta, la pantalla queda con el color `Fondo` plano.
- Los enlaces con `x:DataType` se compilan; si el compilador avisa `MAUIG2045` (propiedad no encontrada) el enlace caerá a reflexión, más lenta y frágil con el recorte de Release: corregir el `x:DataType`.

## 6. Servidor, emulador y teléfono real

`ConfiguracionServicio.UrlPorDefecto()` arma `http://<host>:<Puerto>/<Ruta>` con el host de la plataforma. Sin recompilar: variable de entorno `WS_URL` (solo útil en el destino de Windows).

| Entorno | Host | Notas |
|---|---|---|
| Emulador de Android | `10.0.2.2` (el PC anfitrión; **no** `localhost`) | automático (`DeviceType.Virtual`) |
| Teléfono físico | `IpPc` (IPv4 del PC en la red local, `ipconfig`) | misma Wi-Fi; la IP cambia al cambiar de red |
| Windows | `localhost` | |

- **HTTP sin cifrar:** `android:usesCleartextTraffic="true"` en el manifiesto (ya está) y los permisos `INTERNET`/`ACCESS_NETWORK_STATE`. Si el servidor pasa a HTTPS, quitar el primero y asegurar que el dispositivo confía en el certificado del servidor (el certificado de desarrollo de `dotnet dev-certs` es de confianza solo en el PC; en Android habría que instalarlo en el dispositivo). *HTTPS no se probó en esta plantilla.*
- **El servidor debe escuchar fuera de `localhost`:** con Kestrel, perfil `http` con `applicationUrl` `http://0.0.0.0:<puerto>`; con IIS Express, un binding comodín (`*:<puerto>:*` en `applicationhost.config`), Visual Studio como administrador (o `netsh http add urlacl url=http://*:<puerto>/ user=Todos`) y la regla de firewall.
- **Firewall de Windows** (PowerShell como administrador): `netsh advfirewall firewall add rule name="Servidor SOAP" dir=in action=allow protocol=TCP localport=<puerto>`; la red debe estar en categoría **Privada**.
- **Probar antes de tocar la app:** abrir `http://<host>:<puerto>/<Ruta>?wsdl` en el navegador del emulador (`10.0.2.2`) o del teléfono (IP del PC). Si carga el XML, la app también conectará. `400 Invalid Hostname` indica un IIS Express sin el binding comodín.
- **El WSDL no se usa en ejecución:** un cliente WCF no lo descarga; usa el binding y la URL indicados. Con el servidor apagado el proxy se crea igual y la llamada falla con `RED` tras el tiempo límite (10 s).
- **HTTP y HTTPS:** el esquema de la URL decide el modo de seguridad del binding (`ClienteWS.CrearBinding`). La validación del certificado **no** se desactiva; si falla, el cliente solo muestra «No se pudo comunicar con el servidor».

## 7. Comandos y verificación

```
dotnet build -f net10.0-android                          # compilar para Android
dotnet test -f net10.0-windows10.0.19041.0               # pruebas locales (sin servidor ni interacción)
dotnet build -f net10.0-android -t:Run                   # compilar, instalar y abrir en el emulador o teléfono conectado
dotnet build -c Release -f net10.0-android -t:Run -p:AndroidPackageFormat=apk   # lo mismo en Release (con recorte)
```
En Visual Studio: elegir el emulador y *Run* (F5). Si el emulador no se ve, `adb devices` debe listarlo (`XA0010: No hay ningún dispositivo disponible` es que no hay ninguno).

Verificación con la aplicación en marcha (la vista se revisa a mano; los controladores, con pruebas):
1. Acceso correcto lleva a la primera pantalla; credenciales malas muestran el mensaje del servidor y conservan el usuario.
2. Un botón de una pantalla que carga datos al abrirse **se habilita** al terminar la carga.
3. Error de negocio: se muestra el mensaje en el panel y la pantalla sigue usable.
4. Con el servidor apagado: aparece «No se pudo comunicar con el servidor» (≈ 10 s) y la aplicación sigue respondiendo.
5. Token vencido: vuelve al acceso con el mensaje.
6. **Atrás** de Android no devuelve al acceso con la sesión abierta; «Cerrar sesión» deja el acceso limpio.
7. Tildes y «ñ» en pantalla; el teclado numérico acepta el signo menos y la coma.
8. **Repetir los puntos 1 y 3 en Release** (el recorte de código puede romper enlaces por reflexión o la serialización de WCF, y no falla en Debug).
9. En teléfono físico: `IpPc`, firewall y red Privada.

Un agente que no pueda abrir el emulador debe decir explícitamente qué verificó (pruebas, compilación, arranque, contra qué servidor) y que **no vio la aplicación real**.
Con `adb` se puede automatizar el recorrido (`adb shell input tap/text`, `adb exec-out screencap -p`, `adb shell uiautomator dump`); en Git Bash definir `MSYS_NO_PATHCONV=1` para que no convierta rutas como `/sdcard`.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws` y `servicio`. Es la convención común de la familia (`CONVENCION_SOAP.md`):

- SOAP **1.1**, *document/literal wrapped*, `BasicHttpBinding`. Elementos **calificados** con el namespace del servicio y `SOAPAction` = `TNS + Servicio + "/" + operación`: con el proxy generado los cumple el generador (R3, R4); un servidor .NET rechaza la acción vacía con HTTP 500.
- **El cliente no depende del nombre del puerto ni del binding** (R10): usa el proxy con una URL explícita. **La URL completa** del servicio se arma por configuración; no se asume «base + servicio» (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Éxito y error usan el mismo tipo.
- **Lo que el código no puede imponer** (acuerdo con el servidor): todos los datos viajan como texto (R6); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R7); los nombres de operaciones, parámetros y campos son fijos (R5/R8).
  Si el servidor cambia su contrato hay que **regenerar el proxy** y revisar `servicio`.
- Los campos que pueden faltar (`token`, listas) llegan `null` en un error: no darlos por existentes.
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (comportamientos del cliente WCF).
- Si el proxy generado no funcionara en Android (sobre todo en Release), la alternativa documentada es `HttpClient` con el sobre SOAP armado a mano, aplicando la receta de `CONVENCION_SOAP.md` §4: operación y parámetros con prefijo de namespace, `SOAPAction` con la URI y lectura por nombre local.
- Para un servicio **REST** usar una plantilla REST, no esta (la capa `servicio` solo depende de `IClienteXxx`).

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `APT2098` / `APT2261` al compilar para Android (`…microsoft_maui_essentials_fileprovider_file_paths.xml.flat: failed to open file`) | La ruta del proyecto es demasiado larga para `aapt2` (no soporta rutas largas; `LongPathsEnabled` no ayuda). Mover el proyecto a una ruta corta, borrar `bin/` y `obj/` |
| `XA0010: No hay ningún dispositivo disponible` | No hay emulador ni teléfono conectado: arrancar el emulador; `adb devices` debe listarlo |
| «No se pudo comunicar con el servidor» | Servidor apagado, URL distinta de la real, host equivocado (`localhost` no sirve en Android), firewall o red Pública, o `IpPc` desactualizada. Abrir `<URL>?wsdl` en el navegador del dispositivo |
| `Cleartext HTTP traffic to … not permitted` | Falta `android:usesCleartextTraffic="true"` en `Platforms/Android/AndroidManifest.xml` (la plantilla lo trae) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín (§6) |
| Otro dispositivo recibe timeout y desde el PC con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| Un botón aparece deshabilitado y no se habilita nunca | El comando depende de `Ocupado` y se creó sin `this` (`new ComandoAsincrono(ejecutar, () => !Ocupado, this)`) |
| La pantalla compila pero falla al abrirse | Un recurso (`StaticResource`) inexistente o una clave repetida: se detecta al abrir la vista, no al compilar. Probar cada pantalla nueva |
| Aviso `MAUIG2045: Property … not found on "object"` | El enlace no tiene `x:DataType` y se hará por reflexión (más lento y frágil en Release); declarar `x:DataType` en la página o en el enlace |
| Funciona en Debug pero falla en Release | Recorte de código (*trimming*): enlaces por reflexión o serialización de WCF. Corregir los `x:DataType`; si es el proxy, usar la alternativa de §8 |
| Respuesta con campos vacíos / `null` | El servidor no califica los campos (R3) o el proxy está desactualizado: regenerar desde el WSDL actual |
| HTTP 500 *ActionNotSupported* | Se envía `SOAPAction` vacía: el proxy no es el generado o se alteró el atributo `Action` |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `dotnet-svcutil` falla (`MSB3030`, ruta demasiado larga, o pide otro runtime) | Generar en una copia con ruta corta (p. ej. `C:\Temp\gen`) y copiar `Reference.cs`; definir `DOTNET_ROLL_FORWARD=Major`. Si la carpeta de salida ya tiene un `Reference.cs`, usar una carpeta nueva |
| Avisos `NU1903` de `System.Security.Cryptography.*` | Dependencia transitiva de los paquetes WCF: el `.csproj` ya fija `Pkcs` y `Xml` en `10.0.13`. Revisar si hay versión más reciente |
| `CS1729` / `CS0118` en el destino de Windows | Falta `<CsWinRTAotOptimizerEnabled>false</CsWinRTAotOptimizerEnabled>` en el `.csproj` (ya está) |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el `.csproj` |
| El teclado del emulador abre un tutorial de lápiz y ensucia los campos | Gboard muestra su tutorial de escritura a mano la primera vez: cancelarlo; si la prueba automatizada deja texto basura, reiniciar la aplicación |
| La aplicación instalada no cambia de nombre o de ícono | `ApplicationTitle`/`ApplicationId` se aplican al instalar: desinstalar la anterior y volver a ejecutar |
| `dotnet test` en Android no encuentra pruebas | Las pruebas solo existen en el destino de Windows: `-f net10.0-windows10.0.19041.0` |
