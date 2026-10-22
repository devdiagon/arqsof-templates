# Plantilla: cliente móvil .NET (MAUI) para APIs REST

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos (`CONVENCION_REST.md`). Contiene lo que **todo** cliente necesita (acceso, navegación, estilos,
transporte HTTP, sesión y pruebas) y deja claramente marcado lo que cambia con el **dominio**. Es la contraparte REST de `soap_dotnet_movil` y la versión móvil de `rest_dotnet_escritorio`.

- **Lenguaje:** C# 14, `net10.0-android` y `net10.0-windows10.0.19041.0`, `Nullable` e `ImplicitUsings` habilitados. **Interfaz:** .NET MAUI (XAML) sin librerías de terceros. **Arquitectura:** MVC por capas con MVVM (el controlador hace de ViewModel), navegación con `Shell`.
- **Transporte:** `HttpClient` + `System.Net.Http.Json` + `System.Text.Json`, todo incluido en el SDK: **ningún paquete** en producción. No hay proxy ni WSDL: los DTO se escriben a mano. `api/` (salvo `ConfiguracionApi`), `dto/RespuestaBaseDto`, `excepciones/`, `modelo/Sesion` y `servicio/ServicioBase` son **los mismos** de `rest_dotnet_escritorio` (copiarlos entre plantillas, no reescribirlos). `ConfiguracionApi` es propia del móvil: elige el host según la plataforma.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (tema **oscuro** por defecto, tipografía, medidas, componentes). Implementado en `Resources/Styles/Colors.xaml` y `Styles.xaml`. Para el tema claro, §5.
- **Convención REST:** sigue `CONVENCION_REST.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla; cambiar de servidor es cambiar solo la URL base.
- **Verificada:**
  - `dotnet test -f net10.0-windows10.0.19041.0` → **57 pruebas xUnit en verde**, sin servidor ni interacción: URL base, transporte con un `HttpMessageHandler` falso, traducción de fallos de red, `ServicioBase` (códigos HTTP, token, cuerpos que no son JSON), acceso (con un autenticador falso) y habilitación de comandos.
  - `dotnet build -f net10.0-android` → **0 errores y 0 avisos**; el destino de Windows también compila (lo hace `dotnet test`).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta**: `aapt2` (Android) falla con rutas largas (§9).
2. **Renombrar** `rest_dotnet_movil.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 con las cargas de trabajo `android` y `maui-windows`, el SDK de Android (con un emulador) y el JDK; la primera vez restaura los paquetes (Internet).
4. **Nombre de la aplicación:** `ApplicationTitle` en el `.csproj` (nombre visible en el dispositivo) y `vista/Textos.cs` → `NombreAplicacion` (título de la pantalla de acceso y del `Shell`); mantenerlos iguales.
   **Identificador del paquete:** `ApplicationId` en el `.csproj` (`ec.edu.monster.movil` de la plantilla). Android trata un `ApplicationId` distinto como otra aplicación: desinstalar la anterior del dispositivo.
5. **Configurar el servidor:** en `ec/edu/monster/api/ConfiguracionApi.cs`, `Puerto` y `Ruta` (prefijo de la API, sin barras: `api`). La URL base completa se arma con el host de cada plataforma: `10.0.2.2` en el emulador, `IpPc` en un teléfono físico, `localhost` en Windows. Para un teléfono físico, actualizar `IpPc`.
6. **Sustituir la imagen** `Resources/Images/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6), o quitar el `Image` de la plantilla `PlantillaPagina` de `Resources/Styles/Styles.xaml` para no usarla.
7. **Crear los DTO y el servicio del dominio** (§4) y conectarlos en `MauiProgram.cs` y `Navegacion.cs`.
8. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
9. **Probar el servidor** (una petición con curl desde el PC y desde el emulador, §6) y ejecutar en el emulador. Con la plantilla sin tocar abre el acceso y responde que falta conectar el dominio.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Acceso (compartido)** | `vista/LoginVista`, `controlador/LoginControlador`, `servicio/IAutenticador` | No; el servicio del dominio implementa `IAutenticador` |
| **Transporte HTTP/JSON** | `api/ClienteApi`, `api/IClienteApi`, `api/RespuestaHttp` (Bearer por petición, JSON UTF-8, fallos de red → `ServidorNoDisponible`) | No |
| **URL base** | `api/ConfiguracionApi` (`Puerto`, `Ruta`, `IpPc`, host por plataforma, variable `API_BASE_URL`, normalización con «/» final, timeout 10 s) | `Puerto`, `Ruta` e `IpPc` siempre; el resto no |
| **Respuesta común** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`) | No; los DTO del dominio la extienden |
| **DTO del dominio** | — (`dto/SolicitudXxxDto`, `dto/RespuestaXxxDto`) | **Se crean** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`Codigo`, `EstadoHttp`, `ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`EnviarAsync<T>`, `ExigirToken`, decisión por código HTTP, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Base MVVM** | `controlador/ControladorBase` (`Ocupado`, `Mensaje`, `HayMensaje`), `ComandoAsincrono`, `Comando` | No |
| **Navegación** | `vista/Navegacion` (crea los controladores, entrega los delegados de navegación), `AppShell.xaml(.cs)` (rutas) | Se agregan las pantallas del dominio |
| **Estilos** | `Resources/Styles/Colors.xaml` (tokens), `Styles.xaml` (estilos y `PlantillaPagina`), `vista/PanelMensaje`, `vista/NegacionConvertidor`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` |
| **Composición** | `MauiProgram.cs` (DI), `App.xaml(.cs)` (tema fijo) | Se conecta el servicio y se registran las pantallas del dominio |
| **Plataforma Android** | `Platforms/Android/AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`, `usesCleartextTraffic`), `colors.xml` (barra de estado) | `usesCleartextTraffic` solo si el servidor usa HTTPS |
| **Pantalla de ejemplo (dominio)** | `vista/InicioVista`, `controlador/InicioControlador` | **Se reemplaza** por las pantallas del dominio |
| **Pruebas** | `pruebas/` (URL, transporte, `ServicioBase`, doble `ClienteApiFalso`, acceso, comandos) | Se conservan; se agregan las del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/`, `modelo/` |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec MauiProgram.cs AppShell.xaml.cs rest_dotnet_movil.csproj`).

## 3. Estructura

```
MauiProgram.cs, App.xaml(.cs)      punto de composición (DI: Sesion, HttpClient, servicio, Navegacion, pantallas) y tema
AppShell.xaml(.cs)                 Shell: pantalla inicial (login) y rutas de las demás
Platforms/                         Android (manifiesto, actividad) y Windows
Resources/                         Styles/ (Colors.xaml, Styles.xaml), Images/fondo_app.png, AppIcon/, Splash/, Raw/
docs/CLIENT_STYLES_DOTNET.md       guía de estilos
CONVENCION_REST.md                 convención común de los servidores REST de la familia
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion                                  entidades: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje + estado HTTP; ExigeLogin, ServidorNoDisponible
├── api/           ClienteApi, IClienteApi, RespuestaHttp, ConfiguracionApi     transporte HTTP: URL base, timeout, cabeceras, código + cuerpo
├── dto/           RespuestaBaseDto (+ los DTO del dominio)     cuerpos JSON del contrato, escritos a mano; no salen de servicio/
├── servicio/      ServicioBase, IAutenticador (+ los servicios del dominio)    única capa que conoce api y dto
├── controlador/   ControladorBase, ComandoAsincrono, Comando, LoginControlador, InicioControlador
├── vista/         Navegacion, LoginVista, InicioVista, PanelMensaje, Textos, NegacionConvertidor
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo (solo se compilan en el destino de Windows)
```

Dependencias permitidas: `vista → controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**Los DTO no salen de `servicio/`**: el servicio los traduce a `modelo` (`record`) o a tipos simples.
Los controladores nunca importan `api`, `dto` ni `Microsoft.Maui`: hablan con los servicios y exponen propiedades y comandos. La vista (XAML) no tiene lógica de negocio ni llama al servidor;
su código subyacente solo hace tareas de plataforma (entregar la lectura de la contraseña, crear el controlador al aparecer, el botón Atrás).
Si el contrato cambia (rutas, campos, códigos) solo se tocan `servicio`, `api` y `dto`.

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas`. Solo se compilan (y sus paquetes solo se agregan) en el destino de Windows: un proyecto Android no se ejecuta con `dotnet test` y no deben empaquetarse en el APK. `GenerateProgramFile=false` porque el punto de entrada lo genera MAUI.
> **La vista y `Navegacion` no se prueban con xUnit** (necesitan la aplicación en ejecución); se verifican a mano (§7).

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
   ```
   - Aquí van las reglas **del cliente** (campo vacío, formato, `NormalizarValor`: coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
   - Un valor de ruta variable se codifica con `Uri.EscapeDataString` (`$"items/{Uri.EscapeDataString(nombre)}"`).
3. **Controlador** — `controlador/ItemsControlador : ControladorBase` (molde: `LoginControlador`):
   - Recibe el servicio y la navegación como **delegados** (`Action`, `Action<string>`); los controladores no se conocen entre sí ni conocen `Shell`.
   - Comandos con `ComandoAsincrono(ejecutar, () => !Ocupado, this)`: el **`this` es obligatorio** en todo comando cuya habilitación dependa de `Ocupado`. Sin él, un botón enlazado mientras la pantalla cargaba queda deshabilitado para siempre (el comando solo vuelve a consultarse cuando avisa).
   - Una pantalla que **carga datos al abrirse** expone `Task CargarAsync()` que **no lanza**: pone `Ocupado`, captura `ExcepcionServicio` (solo `ex.Message` en `Mensaje`, nunca el código) y cualquier otra excepción (mensaje genérico), y si `ex.ExigeLogin` avisa con un delegado `Action<string> alExpirarSesion` para volver al login con ese mensaje.
   - Un fallo de red (`ServidorNoDisponible`) o un error de negocio (`400`, `404`) muestra el mensaje y **conserva lo que el usuario capturó** y la sesión.
4. **Vista** — `vista/ItemsVista.xaml` (`ContentPage`) **sin lógica**, construida solo con los estilos de `Styles.xaml` (§5) y con `x:DataType="ctl:ItemsControlador"` (enlaces compilados). Moldes: `LoginVista` e `InicioVista`:
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
5. **Navegación** — en `AppShell.xaml.cs` registrar la ruta (`Routing.RegisterRoute("items", typeof(ItemsVista))`) y en `vista/Navegacion.cs` agregar `CrearItems()`, que entrega los delegados: `alExpirarSesion: mensaje => IrAlLogin(mensaje)`, y para ir a otra pantalla `Ir("detalle", new Dictionary<string, object> { ["id"] = id })` (la vista destino implementa `IQueryAttributable` y crea su controlador al recibir el parámetro). `Navegacion` recibe por constructor los servicios del dominio.
   Una pantalla que carga datos al abrirse lo hace en `OnAppearing` (una sola vez, con una bandera) llamando a `CargarAsync()` del controlador que le entregó `Navegacion`.
6. **Composición** — en `MauiProgram.cs` (el `HttpClient` y `IClienteApi` ya están registrados): registrar el servicio y `IAutenticador` (`AddSingleton<IAutenticador>(sp => sp.GetRequiredService<ServicioXxx>())`), las vistas (`AddTransient<ItemsVista>()`), y **borrar `AutenticadorPendiente`**, `InicioVista` e `InicioControlador` (y su ruta y su `CrearInicio`). La `Sesion` es **una sola** (`Singleton`). Cambiar la pantalla a la que va el acceso: `Ir(RutaInicio)` en `Navegacion.CrearLogin`.
7. **Pruebas** (en `ec/edu/monster/pruebas/`):
   - El servicio con `ClienteApiFalso` (`Responder` por ruta): `200`, `400`, `401` con `AUTH-002/003`, `404`, error de plataforma, cuerpo que no es JSON, servidor no disponible, coma enviada como punto, sin sesión no se llama a la API.
   - Los controladores con un servicio doble (error de negocio solo con mensaje, red, token vencido, `Ocupado`).
   - **`CanExecute` de cada comando que cargue datos al abrir** (molde: `ComandosHabilitadosPruebas`).
   - La vista se verifica a mano (§7): un recurso mal escrito en un XAML puede **compilar** y fallar al abrir la pantalla.

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP y luego por el código propio.** Nunca `EnsureSuccessStatusCode()`: descartaría el JSON de error. `ServicioBase` ya lo cumple.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se muestra. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente. Va en `Authorization: Bearer` **por petición**, nunca en `DefaultRequestHeaders`.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual): el teclado numérico puede ofrecer la coma.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de la interfaz:** `async/await`. Un fallo de red no cierra la aplicación.
- La **contraseña** no se enlaza: la vista entrega a `LoginControlador.LeerContrasenia` cómo leerla, y no se guarda en el controlador.
- Un solo `HttpClient` de larga vida (ya registrado como `Singleton` en `MauiProgram.cs`), no uno por petición.

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

- **Tema oscuro** fijo por defecto (`UserAppTheme = Dark` en `App.xaml.cs`); no hay selector. **Disposición:** una sola columna; márgenes de 24 en el layout, controles de 48 de alto mínimo, radio 8, sin sombras. Los paneles usan `PanelSobreImagen` porque hay imagen debajo.
- **Sin fuentes propias:** se usa la del sistema (Roboto en Android). No agregar `OpenSans` ni la paleta de ejemplo de MAUI.
- **La imagen** va en `Resources/Images/` (MAUI la convierte a recurso de cada plataforma; nombre en minúsculas, sin espacios). Si falta, la pantalla queda con el color `Fondo` plano.
- Los enlaces con `x:DataType` se compilan; si el compilador avisa `MAUIG2045` (propiedad no encontrada) el enlace caerá a reflexión, más lenta y frágil con el recorte de Release: corregir el `x:DataType`.

## 6. Servidor, emulador y teléfono real

`ConfiguracionApi.UrlPorDefecto()` arma `http://<host>:<Puerto>/<Ruta>/` (con «/» final) con el host de la plataforma. Sin recompilar: variable de entorno `API_BASE_URL` (solo útil en el destino de Windows).

| Entorno | Host | Notas |
|---|---|---|
| Emulador de Android | `10.0.2.2` (el PC anfitrión; **no** `localhost`) | automático (`DeviceType.Virtual`) |
| Teléfono físico | `IpPc` (IPv4 del PC en la red local, `ipconfig`) | misma Wi-Fi; la IP cambia al cambiar de red |
| Windows | `localhost` | |

- **HTTP sin cifrar:** `android:usesCleartextTraffic="true"` en el manifiesto (ya está) y los permisos `INTERNET`/`ACCESS_NETWORK_STATE`. Si el servidor pasa a HTTPS, quitar el primero y asegurar que el dispositivo confía en el certificado del servidor (el certificado de desarrollo de `dotnet dev-certs` es de confianza solo en el PC). *HTTPS no se probó en esta plantilla.*
- **El servidor debe escuchar fuera de `localhost`:** con Kestrel, perfil `http` con `applicationUrl` `http://0.0.0.0:<puerto>`; con IIS Express, un binding comodín (`*:<puerto>:*` en `applicationhost.config`), Visual Studio como administrador (o `netsh http add urlacl url=http://*:<puerto>/ user=Todos`) y la regla de firewall.
- **Firewall de Windows** (PowerShell como administrador): `netsh advfirewall firewall add rule name="Servidor REST" dir=in action=allow protocol=TCP localport=<puerto>`; la red debe estar en categoría **Privada**. Retirar la regla al terminar.
- **Probar antes de tocar la app:** una ruta protegida sin token debe responder `401` con un JSON. Desde el navegador del emulador (`http://10.0.2.2:<puerto>/<Ruta>/<ruta-protegida>`) o del teléfono (IP del PC). Si responde, la app también conectará. `400 Invalid Hostname` indica un IIS Express sin el binding comodín.
- **HTTP y HTTPS:** el esquema de la URL base lo decide todo; no hay binding. La validación del certificado **no** se desactiva; si falla, el cliente solo muestra «Servidor no disponible, intente más tarde» (la excepción original queda en `InnerException`).
- **Cambiar de servidor** (por ejemplo, de .NET a Java) es cambiar `Puerto`/`Ruta` (o `API_BASE_URL`), siempre que ambos cumplan `CONVENCION_REST.md`.

## 7. Comandos y verificación

```
dotnet build -f net10.0-android                          # compilar para Android
dotnet test -f net10.0-windows10.0.19041.0               # pruebas locales (sin servidor ni interacción)
dotnet build -f net10.0-android -t:Run                   # compilar, instalar y abrir en el emulador o teléfono conectado
dotnet build -c Release -f net10.0-android -t:Run -p:AndroidPackageFormat=apk   # lo mismo en Release (con recorte)
```
En Visual Studio: elegir el emulador y *Run* (F5). Si el emulador no se ve, `adb devices` debe listarlo (`XA0010: No hay ningún dispositivo disponible` es que no hay ninguno).

Probar el servidor sin la aplicación (una ruta protegida sin token debe responder `401` con un JSON, lo que confirma que la API está arriba):
```
curl -i http://localhost:5267/<Ruta>/<ruta-protegida>
curl -s -X POST http://localhost:5267/<Ruta>/auth/login -H "Content-Type: application/json" -d '{"usuario":"…","contrasenia":"…"}'
```

Verificación con la aplicación en marcha (la vista se revisa a mano; los controladores, con pruebas):
1. Acceso correcto lleva a la primera pantalla; credenciales malas muestran el mensaje del servidor y conservan el usuario.
2. Un botón de una pantalla que carga datos al abrirse **se habilita** al terminar la carga.
3. Error de negocio (`400`/`404`): se muestra el mensaje y la pantalla sigue usable, con la sesión abierta.
4. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» (≈ 10 s) y la aplicación sigue respondiendo.
5. Token vencido (`AUTH-003`): vuelve al acceso con el mensaje.
6. **Atrás** de Android no devuelve al acceso con la sesión abierta; «Cerrar sesión» deja el acceso limpio.
7. Tildes y «ñ» en pantalla; el teclado numérico acepta el signo menos y la coma.
8. **Repetir los puntos 1 y 3 en Release** (el recorte de código puede romper enlaces por reflexión o la serialización JSON, y no falla en Debug).
9. En teléfono físico: `IpPc`, firewall y red Privada.

Un agente que no pueda abrir el emulador debe decir explícitamente qué verificó (pruebas, compilación, arranque, contra qué servidor) y que **no vio la aplicación real**.
Con `adb` se puede automatizar el recorrido (`adb shell input tap/text`, `adb exec-out screencap -p`, `adb shell uiautomator dump`); en Git Bash definir `MSYS_NO_PATHCONV=1` para que no convierta rutas como `/sdcard`.
Las coordenadas de `input tap` son las de la pantalla real (p. ej. 1080 × 2400), no las de una captura reducida.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicio`. Es la convención común de la familia (`CONVENCION_REST.md`):

- HTTP + JSON (`application/json; charset=utf-8`), verbos `GET` (consultas) y `POST` (login y operaciones). **La URL base** se arma por configuración; no se asume puerto ni prefijo.
- Cada respuesta, de éxito o de error, es un objeto con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los errores de la plataforma (ruta inexistente, método no permitido, `Content-Type` incorrecto, error interno) traen **solo** esos tres campos.
- **Código HTTP + código propio:** `200` éxito; `400` datos inválidos; `401` credenciales (`AUTH-001`) o token (`AUTH-002` inválido / `AUTH-003` vencido); `404` recurso inexistente; `405`/`415` error de programación del cliente; `500` error del servidor.
- El token viaja en `Authorization: Bearer <token>`, no como parámetro ni cuerpo. Existe un login público (`POST /auth/login`) que devuelve `token`.
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Todos los datos viajan como **texto**; los decimales con punto. Los campos que pueden faltar (`token`, listas) llegan `null` o ausentes en un error: no darlos por existentes.
- Si el servidor usa otro esquema de autenticación (cookies, OAuth/JWT con renovación), ampliar `ClienteApi`/`ServicioBase`.
- Para un servicio **SOAP** usar `soap_dotnet_movil`, no esta (la capa `servicio` es la que cambia).

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `APT2098` / `APT2261` al compilar para Android (`…microsoft_maui_essentials_fileprovider_file_paths.xml.flat: failed to open file`) | La ruta del proyecto es demasiado larga para `aapt2` (no soporta rutas largas; `LongPathsEnabled` no ayuda). Mover el proyecto a una ruta corta, borrar `bin/` y `obj/` |
| `XA0010: No hay ningún dispositivo disponible` | No hay emulador ni teléfono conectado: arrancar el emulador; `adb devices` debe listarlo |
| «Servidor no disponible, intente más tarde» | Servidor apagado, URL distinta de la real, host equivocado (`localhost` no sirve en Android), firewall o red Pública, o `IpPc` desactualizada. Probar la URL con el navegador del dispositivo |
| `Cleartext HTTP traffic to … not permitted` | Falta `android:usesCleartextTraffic="true"` en `Platforms/Android/AndroidManifest.xml` (la plantilla lo trae) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín (§6) |
| Conexión rechazada desde `10.0.2.2` con el perfil `http` | Kestrel escucha solo en `localhost`: `applicationUrl` con `0.0.0.0` y firewall (§6) |
| Otro dispositivo recibe timeout y desde el PC con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| Las rutas dan `404` o se concatenan mal | La URL base no termina en «/» (`ConfiguracionApi` lo corrige) o la ruta lleva barra inicial: usar `"auth/login"`, no `"/auth/login"` |
| La URL base queda con `CAMBIAR_PREFIJO_API` | Falta configurar `ConfiguracionApi.Ruta` (§1, paso 5) |
| Campos `null` en una respuesta correcta | `[JsonPropertyName]` distinto del nombre del contrato, o el servidor cambió el campo |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `415` / `ERR-415` | El cuerpo no se envió como JSON: usar `ClienteApi` (no armar `HttpRequestMessage` en otra clase) |
| Muestra «El servidor respondió con un error inesperado (HTTP nnn)» | El cuerpo del error no era JSON (página de IIS Express, proxy): revisar la URL y el servidor |
| Un botón aparece deshabilitado y no se habilita nunca | El comando depende de `Ocupado` y se creó sin `this` (`new ComandoAsincrono(ejecutar, () => !Ocupado, this)`) |
| La pantalla compila pero falla al abrirse | Un recurso (`StaticResource`) inexistente o una clave repetida: se detecta al abrir la vista, no al compilar. Probar cada pantalla nueva |
| Aviso `MAUIG2045: Property … not found on "object"` | El enlace no tiene `x:DataType` y se hará por reflexión (más lento y frágil en Release); declarar `x:DataType` en la página o en el enlace |
| Funciona en Debug pero falla en Release | Recorte de código (*trimming*): enlaces por reflexión o serialización JSON. Corregir los `x:DataType`; serializar con DTO de propiedades públicas |
| `CS1729` / `CS0118` en el destino de Windows | Falta `<CsWinRTAotOptimizerEnabled>false</CsWinRTAotOptimizerEnabled>` en el `.csproj` (ya está) |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el `.csproj` |
| El teclado del emulador abre un diálogo «Try out your stylus» y ensucia los campos | Gboard / Android muestra su tutorial de lápiz la primera vez: cancelarlo; `adb shell settings put secure stylus_handwriting_enabled 0` lo desactiva. Si la prueba automatizada deja texto basura, reiniciar la aplicación |
| La aplicación instalada no cambia de nombre o de ícono | `ApplicationTitle`/`ApplicationId` se aplican al instalar: desinstalar la anterior y volver a ejecutar |
| `dotnet test` en Android no encuentra pruebas | Las pruebas solo existen en el destino de Windows: `-f net10.0-windows10.0.19041.0` |
| La carpeta `pruebas` no aparece en el Explorador de soluciones de Visual Studio | Falta en el `.csproj` el `<None Include="ec\edu\monster\pruebas\**\*.cs" />` junto al `Compile Remove` (en Android la carpeta se excluye de la compilación y, sin él, deja de ser un elemento del proyecto). Si se quieren ver compilables (con IntelliSense), cambiar el destino activo a Windows |
