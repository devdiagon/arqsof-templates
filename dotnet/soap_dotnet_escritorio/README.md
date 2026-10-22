# Plantilla: cliente de escritorio .NET (WPF) para servicios SOAP

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita (acceso, navegación, estilos,
transporte, sesión y pruebas) y deja claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** C# 14, `net10.0-windows`, `Nullable` e `ImplicitUsings` habilitados. **Interfaz:** WPF (XAML) sin librerías de terceros. **Arquitectura:** MVC por capas con MVVM (el controlador hace de ViewModel).
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` `10.0.652802`) con un **proxy generado desde el WSDL** por `dotnet-svcutil` (§4, paso 1). No se escribe el contrato a mano.
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md` (tema **oscuro**, tipografía, medidas, componentes, disposición dividida). Implementado en `estilos/Tema.xaml` y `estilos/Controles.xaml`.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla. Con un proxy generado, el generador cumple por sí mismo R3 y R4.
- **Verificada:**
  - `dotnet test` → **42 pruebas xUnit en verde**, sin servidor ni interacción: URL, binding http/https, traducción del fallo de red a `RED`, `ServicioBase`, navegación, acceso (con un autenticador falso), habilitación de comandos y **carga real de las pantallas con los estilos** (`VistasCargaPruebas`).
  - La aplicación se **lanzó** y la ventana se creó (título y manejador de ventana comprobados).
  - **No se ha hecho:** ver la ventana ni hacer clic en ella (el aspecto no se comprobó), ni una llamada a un servidor real: la plantilla no tiene proxy ni dominio. La receta de §4 es la que se siguió y verificó en el cliente de escritorio del conversor de unidades (login, catálogo y conversiones contra un servidor .NET).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta**: `dotnet-svcutil` falla con rutas muy largas (§9).
2. **Renombrar** `soap_dotnet_escritorio.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 y el escritorio de Windows; la primera vez restaura los paquetes (Internet).
4. **Configurar el servidor:** en `ec/edu/monster/ws/ConfiguracionServicio.cs`, `UrlPorDefecto` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor).
5. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la ventana y de la pantalla de acceso) y, si se quiere, **sustituir la imagen** `ec/edu/monster/estaticos/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6).
6. **Generar el proxy** desde el WSDL del servidor (§4, paso 1) y conectar el servicio en `App.xaml.cs` (§4, paso 6).
7. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
8. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar (F5 en Visual Studio o `dotnet run`). Con la plantilla sin tocar abre la ventana y el acceso responde que falta conectar el dominio.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Acceso (compartido)** | `vista/LoginVista`, `controlador/LoginControlador`, `servicio/IAutenticador` | No; el servicio del dominio implementa `IAutenticador` |
| **Transporte SOAP** | `ws/ClienteWS` (binding por esquema http/https, timeouts, `LlamarAsync` con traducción de fallos de red) | No |
| **URL del servicio** | `ws/ConfiguracionServicio` (`UrlPorDefecto`, variable `WS_URL`, normalización) | `UrlPorDefecto` siempre; el resto no |
| **Proxy generado** | — (`ws/Reference.cs`, generado por `dotnet-svcutil` desde `wsdl/`) | **Se genera** por proyecto; **no se edita** |
| **Fábrica del cliente del dominio** | — (`ws/ClienteWSXxx` + `ws/IClienteXxx` sobre el proxy) | **Se crea** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`ExigirToken`, `Verificar`, `ExigirRespuesta`, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Base MVVM** | `controlador/ControladorBase` (`Ocupado`, `Mensaje`, `HayMensaje`), `ComandoAsincrono`, `Comando` | No |
| **Navegación** | `controlador/NavegadorControlador`, `vista/VentanaPrincipal` (un `DataTemplate` por pantalla) | Se agregan los `DataTemplate` de las pantallas del dominio |
| **Estilos** | `estilos/Tema.xaml`, `estilos/Controles.xaml`, `vista/NegacionConvertidor`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` |
| **Composición** | `App.xaml`, `App.xaml.cs` | Se conecta el servicio y se agregan las pantallas del dominio |
| **Pantalla de ejemplo (dominio)** | `vista/InicioVista`, `controlador/InicioControlador` | **Se reemplaza** por las pantallas del dominio |
| **Pruebas** | `pruebas/` (URL, binding, transporte, `ServicioBase`, navegación, acceso, comandos, carga de vistas) | Se conservan; se agregan las del dominio |
| **Dominio (vacío)** | — | Nuevos `ws/`, `servicio/`, `controlador/`, `vista/`, `modelo/` |

Las zonas a adaptar están marcadas con **`DOMINIO`**, **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "DOMINIO|CAMBIAR|TODO" ec App.xaml App.xaml.cs`).

## 3. Estructura

```
App.xaml, App.xaml.cs              punto de composición (Sesion, servicio, controladores) y orden de las pantallas
wsdl/                              copia del WSDL del servidor (fuente de verdad del contrato); versionada
docs/CLIENT_STYLES_DOTNET.md       guía de estilos
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion                                  entidades: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; ExigeLogin, ServidorNoDisponible
├── ws/            ClienteWS, ConfiguracionServicio (+ Reference.cs generado, IClienteXxx y ClienteWSXxx del dominio)
├── servicio/      ServicioBase, IAutenticador (+ los servicios del dominio)    única capa que conoce los tipos generados
├── controlador/   ControladorBase, ComandoAsincrono, Comando, NavegadorControlador, LoginControlador, InicioControlador
├── vista/         VentanaPrincipal, LoginVista, InicioVista, Textos, NegacionConvertidor
├── estilos/       Tema.xaml (tokens), Controles.xaml (estilos de controles)
├── estaticos/     fondo_app.png (imagen de ejemplo)
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo
```

Dependencias permitidas: `vista → controlador → servicio → ws`; `modelo` y `excepciones` no dependen de nadie.
**Los tipos generados (el «Dto» de la familia) viven en `ws/Reference.cs` y no salen de `servicio/`**; no hay carpeta `dto` ni clases `Dto` a mano.
Los controladores nunca importan `ws` ni `System.Windows.Controls`: hablan con los servicios y exponen propiedades y comandos. La vista (XAML) no tiene lógica de negocio ni llama al servidor;
su código subyacente solo hace tareas de plataforma (foco inicial, entregar la lectura del `PasswordBox`).

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas` (`GenerateProgramFile=false` porque el punto de entrada lo genera WPF desde `App.xaml`). Detener cualquier instancia en ejecución antes de `dotnet test`.

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
   - Recibe el servicio y la navegación como **delegados** (`Action`, `Action<string>`); los controladores no se conocen entre sí.
   - Comandos con `ComandoAsincrono(ejecutar, () => !Ocupado, this)`: el **`this` es obligatorio** en todo comando cuya habilitación dependa de `Ocupado`. Sin él, un botón enlazado mientras la pantalla cargaba queda deshabilitado para siempre (WPF solo vuelve a consultar cuando el comando avisa).
   - Una pantalla que **carga datos al abrirse** expone `Task CargarAsync()` que **no lanza**: pone `Ocupado`, captura `ExcepcionServicio` (solo `ex.Message` en `Mensaje`, nunca el código) y cualquier otra excepción (mensaje genérico), y si `ex.ExigeLogin` avisa con un delegado `Action<string> alExpirarSesion` para volver al login con ese mensaje.
   - Un fallo de red conserva lo que el usuario capturó.
5. **Vista** — `vista/ItemsVista.xaml` (`UserControl`) **sin lógica**, construida solo con los estilos de `Controles.xaml` (`PanelEncabezado`, `TextoEtiqueta`, `CajaTexto`, `Selector`, `BotonPrimario`, `BotonSecundario`, `PanelError`, `PanelResultado`, `Progreso`). Moldes: `LoginVista` e `InicioVista`. Enlazar `IsEnabled` de la raíz a `Ocupado` con `NegacionConvertidor` y mostrar el mensaje con `HayMensaje`.
6. **Navegación y composición** — en `vista/VentanaPrincipal.xaml` agregar el `DataTemplate` (controlador → vista); en `App.xaml.cs` crear el servicio (`_autenticador = servicio`), el método `MostrarItems()` que hace `_navegador.Mostrar(pantalla); _ = pantalla.CargarAsync();`, y reemplazar `MostrarInicio` (y borrar `InicioVista`/`InicioControlador` y `AutenticadorPendiente`). La `Sesion` es **una sola** para toda la ejecución.
7. **Pruebas** (en `ec/edu/monster/pruebas/`):
   - Un doble escrito a mano de `IClienteXxx` que devuelve respuestas simuladas, para el servicio (OK, error de negocio, red, sesión vencida, coma enviada como punto, sin sesión no se consulta nada).
   - Los controladores con ese servicio doble (error de negocio solo con mensaje, red, token vencido, `Ocupado`).
   - **`CanExecute` de cada comando que cargue datos al abrir** (molde: `ComandosHabilitadosPruebas`).
   - **Una prueba de carga por vista** en `VistasCargaPruebas`: es la que detecta recursos repetidos o plantillas inválidas, que no fallan al compilar.
   - Una **prueba de humo** contra un servidor falso local (CoreWCF) con el mismo contrato detecta un WSDL desactualizado (opcional).

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se muestra. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de la interfaz:** `async/await` con las operaciones `…Async`. Un fallo de red no cierra la aplicación.
- La **contraseña** no se enlaza (`PasswordBox`): la vista entrega a `LoginControlador.LeerContrasenia` cómo leerla, y no se guarda en el controlador.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_DOTNET.md`)

Las vistas **solo referencian recursos** (`{StaticResource …}`); no colorean ni dimensionan componentes sueltos. Se carga `estilos/Controles.xaml` en `App.xaml` (incorpora `Tema.xaml`).

| Recurso | Para qué |
|---|---|
| `BotonPrimario` | Acción principal de la pantalla (relleno `Primario`, texto blanco). En una lista de acciones, uno por elemento |
| `BotonSecundario` | Salida o regreso: «Cerrar sesión», «Regresar» (sólido `Secundario`) |
| `PanelEncabezado` + `TextoTituloApp` / `TextoTitulo` | Título de la aplicación (acceso, centrado) y de pantalla |
| `TextoEtiqueta`, `CajaTexto`, `CajaContrasenia`, `Selector` | Etiqueta de campo, campos y lista desplegable (foco en `Acento`, error en `Error`) |
| `PanelError`, `PanelResultado` + `TextoMensaje`, `TextoResultado` | Mensaje y resultado, con barra de 4 a la izquierda; ocultos hasta que hay texto (`HayMensaje`) |
| `Progreso` | Indicador de carga; deshabilitar la pantalla mientras `Ocupado` |

- **Tema oscuro** fijo; no hay selector. Para la presentación clara, cambiar los valores de `Tema.xaml` por los de `docs/CLIENT_STYLES_DOTNET.md` §2.3.
- **Nombres únicos:** los pinceles de `Tema.xaml` llevan el nombre del token (`CampoTexto`) y los estilos el del control (`CajaTexto`); repetir una clave hace caer la aplicación al cargar la vista (`docs/CLIENT_STYLES_DOTNET.md` §7.3).
- **Disposición:** ventana dividida: **imagen a la izquierda** (560, fija, completa y centrada) y la pantalla a la derecha (columna de máximo 420, centrada). Tamaño inicial 1100 × 720, **mínimo 1040 × 640** (560 + 420 + márgenes). La imagen es de la **ventana**, no de cada pantalla. Para no usarla, quitar el `Image` de `VentanaPrincipal.xaml`.
  Es la disposición del documento de estilos con las zonas invertidas (§7.0 las define con el contenido a la izquierda).
- **Las imágenes** van en `estaticos/` con acción de compilación `Resource` (ya declarada en el `.csproj` para `fondo_app.png`).
- Una plantilla de control con una propiedad o recurso inválido **compila** y falla al cargar la vista: probar cada pantalla nueva con `VistasCargaPruebas` y abrirla.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionServicio.UrlPorDefecto` (URL completa). Sin recompilar: variable de entorno `WS_URL`
  (PowerShell: `$env:WS_URL = "http://<ip>:57592/Ruta"; dotnet run`).
- **HTTP y HTTPS:** el esquema de la URL decide el modo de seguridad del binding (`ClienteWS.CrearBinding`): `http://` sin seguridad, `https://` con seguridad de transporte. **HTTPS exige un certificado de servidor válido y de confianza** (en desarrollo, `dotnet dev-certs https --trust`, y que el nombre al que se conecta esté en el certificado).
  La validación del certificado **no** se desactiva en el cliente. Si falla, el cliente solo muestra «No se pudo comunicar con el servidor» (no distingue la causa).
- **El WSDL no se usa en ejecución:** un cliente WCF no lo descarga; usa el binding y la URL indicados. Con el servidor apagado el proxy se crea igual y la llamada falla con `RED` tras el tiempo límite (10 s).
- **Entorno de desarrollo de la familia:** IIS Express por HTTP (`http://localhost:57592/<Ruta>`); el perfil `http` (Kestrel) usa el puerto `5267`. Reglas de red y firewall: `IIS_CONFIG.md` del proyecto del servidor.
- **Visual Studio:** *Run* (F5). Detener la ejecución antes de compilar desde la terminal (el `.dll` queda bloqueado).
- **Terminal:** `dotnet run`.
- **Ejecutable único:** no incluido. `dotnet publish -c Release -r win-x64 --self-contained` lo genera; **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor ni interacción)
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con la aplicación en marcha (la vista se revisa a mano; los controladores, con pruebas):
1. Acceso correcto lleva a la primera pantalla; credenciales malas muestran el mensaje del servidor y conservan el usuario.
2. Un botón de una pantalla que carga datos al abrirse **se habilita** al terminar la carga.
3. Error de negocio: se muestra el mensaje y la pantalla sigue usable.
4. Con el servidor apagado: aparece «No se pudo comunicar con el servidor» (≈ 10 s) y la aplicación sigue respondiendo.
5. Token vencido: vuelve al acceso con el mensaje.
6. Ventana al tamaño inicial, maximizada y al mínimo (1040 × 640); imagen sin deformar; tildes y «ñ» en pantalla.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, carga de vistas, arranque del proceso, contra qué servidor) y que **no vio la ventana real**.

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
- Para un servicio **REST** usar una plantilla REST, no esta (la capa `servicio` solo depende de `IClienteXxx`).

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| La aplicación se cae al abrir una pantalla con `"System.Windows.Style" no es un valor válido para la propiedad…` | Una clave de recurso repetida entre un pincel de `Tema.xaml` y un estilo de `Controles.xaml`; renombrar el estilo con el nombre del control (`docs/CLIENT_STYLES_DOTNET.md` §7.3) |
| Un botón aparece deshabilitado y no se habilita nunca | El comando depende de `Ocupado` y se creó sin `this` (`new ComandoAsincrono(ejecutar, () => !Ocupado, this)`) |
| «No se pudo comunicar con el servidor» | Servidor apagado, URL distinta de la real, firewall, o (HTTPS) certificado sin confiar/vencido/de otro host. Abrir `<URL>?wsdl` en el navegador |
| Por HTTPS falla y por HTTP funciona | Certificado de desarrollo sin confiar: `dotnet dev-certs https --trust`; consultar por el mismo nombre que figura en el certificado |
| Otro equipo recibe timeout y desde el PC del servidor con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín |
| Respuesta con campos vacíos / `null` | El servidor no califica los campos (R3) o el proxy está desactualizado: regenerar desde el WSDL actual |
| HTTP 500 *ActionNotSupported* | Se envía `SOAPAction` vacía: el proxy no es el generado o se alteró el atributo `Action` |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `dotnet-svcutil` falla (`MSB3030`, ruta demasiado larga, o pide otro runtime) | Generar en una copia con ruta corta (p. ej. `C:\Temp\gen`) y copiar `Reference.cs`; definir `DOTNET_ROLL_FORWARD=Major` |
| Avisos `NU1902`/`NU1903` de `System.Security.Cryptography.*` | Dependencia transitiva de los paquetes WCF; no impide funcionar. Revisar si hay una versión corregida |
| `MSB3027`/`MSB3021`: no se puede copiar el `.dll` | Hay una instancia de la aplicación en ejecución: cerrarla antes de compilar |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el `.csproj` |
| La ventana abre pero la zona derecha queda vacía | Falta el `DataTemplate` del controlador de la pantalla en `VentanaPrincipal.xaml` |
