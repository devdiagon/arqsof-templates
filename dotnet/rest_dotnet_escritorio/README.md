# Plantilla: cliente de escritorio .NET (WPF) para APIs REST

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos (`CONVENCION_REST.md`). Contiene lo que **todo** cliente necesita (acceso, navegación, estilos,
transporte HTTP, sesión y pruebas) y deja claramente marcado lo que cambia con el **dominio**. Es la contraparte REST de `soap_dotnet_escritorio` y la versión de escritorio de `rest_dotnet_consola`.

- **Lenguaje:** C# 14, `net10.0-windows`, `Nullable` e `ImplicitUsings` habilitados. **Interfaz:** WPF (XAML) sin librerías de terceros. **Arquitectura:** MVC por capas con MVVM (el controlador hace de ViewModel).
- **Transporte:** `HttpClient` + `System.Net.Http.Json` + `System.Text.Json`, todo incluido en el SDK: **ningún paquete** en producción. No hay proxy ni WSDL: los DTO se escriben a mano. La capa `api/`, `dto/RespuestaBaseDto`, `excepciones/`, `modelo/Sesion` y `servicio/ServicioBase` son **las mismas** de `rest_dotnet_consola` (copiarlas entre plantillas, no reescribirlas).
- **Estilo:** `docs/CLIENT_STYLES_DOTNET.md`, en su **presentación clara** y con la **pantalla a la izquierda y la imagen a la derecha** (§5). Implementado en `estilos/Tema.xaml` y `estilos/Controles.xaml`.
- **Convención REST:** sigue `CONVENCION_REST.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla; cambiar de servidor es cambiar solo la URL base.
- **Verificada:**
  - `dotnet test` → **65 pruebas xUnit en verde**, sin servidor ni interacción: URL base, transporte con un `HttpMessageHandler` falso, traducción de fallos de red, `ServicioBase` (códigos HTTP, token, cuerpos que no son JSON), navegación, acceso (con un autenticador falso), habilitación de comandos y **carga real de las pantallas con los estilos** (`VistasCargaPruebas`).
  - **No se ha hecho:** ver la ventana ni hacer clic en ella (el aspecto no se comprobó), ni una llamada a un servidor real: la plantilla no tiene DTO ni dominio. La receta de §4 es la que se siguió y verificó en el cliente de escritorio del conversor de unidades.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**.
2. **Renombrar** `rest_dotnet_escritorio.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** (`RootNamespace`) es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10 y el escritorio de Windows; la primera vez restaura los paquetes de prueba (Internet).
4. **Configurar el servidor:** en `ec/edu/monster/api/ConfiguracionApi.cs`, `UrlBasePorDefecto` = **URL base de la API** (host, puerto y prefijo, **con «/» final**; p. ej. `http://localhost:57592/api/`).
5. **Poner el nombre de la aplicación** en `vista/Textos.cs` (`NombreAplicacion`: título de la ventana y de la pantalla de acceso) y, si se quiere, **sustituir la imagen** `ec/edu/monster/estaticos/fondo_app.png` conservando el nombre (requisitos en `docs/CLIENT_STYLES_DOTNET.md` §6).
6. **Crear los DTO y el servicio del dominio** (§4) y conectarlos en `App.xaml.cs`.
7. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
8. **Probar el servidor** (una petición con curl, §7) y ejecutar (F5 en Visual Studio o `dotnet run`). Con la plantilla sin tocar abre la ventana y el acceso responde que falta conectar el dominio.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Acceso (compartido)** | `vista/LoginVista`, `controlador/LoginControlador`, `servicio/IAutenticador` | No; el servicio del dominio implementa `IAutenticador` |
| **Transporte HTTP/JSON** | `api/ClienteApi`, `api/IClienteApi`, `api/RespuestaHttp` (Bearer por petición, JSON UTF-8, fallos de red → `ServidorNoDisponible`) | No |
| **URL base** | `api/ConfiguracionApi` (`UrlBasePorDefecto`, variable `API_BASE_URL`, normalización con «/» final, timeout 10 s) | `UrlBasePorDefecto` siempre; el resto no |
| **Respuesta común** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`) | No; los DTO del dominio la extienden |
| **DTO del dominio** | — (`dto/SolicitudXxxDto`, `dto/RespuestaXxxDto`) | **Se crean** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`Codigo`, `EstadoHttp`, `ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`EnviarAsync<T>`, `ExigirToken`, decisión por código HTTP, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Base MVVM** | `controlador/ControladorBase` (`Ocupado`, `Mensaje`, `HayMensaje`), `ComandoAsincrono`, `Comando` | No |
| **Navegación** | `controlador/NavegadorControlador`, `vista/VentanaPrincipal` (un `DataTemplate` por pantalla) | Se agregan los `DataTemplate` de las pantallas del dominio |
| **Estilos** | `estilos/Tema.xaml`, `estilos/Controles.xaml`, `vista/NegacionConvertidor`, `docs/CLIENT_STYLES_DOTNET.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NombreAplicacion` |
| **Composición** | `App.xaml`, `App.xaml.cs` | Se conecta el servicio y se agregan las pantallas del dominio |
| **Pantalla de ejemplo (dominio)** | `vista/InicioVista`, `controlador/InicioControlador` | **Se reemplaza** por las pantallas del dominio |
| **Pruebas** | `pruebas/` (URL, transporte, `ServicioBase`, doble `ClienteApiFalso`, navegación, acceso, comandos, carga de vistas) | Se conservan; se agregan las del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/`, `modelo/` |

Las zonas a adaptar están marcadas con **`DOMINIO`** y **`CAMBIAR`** en el código (`grep -rnE "DOMINIO|CAMBIAR" ec App.xaml App.xaml.cs`).

## 3. Estructura

```
App.xaml, App.xaml.cs              punto de composición (Sesion, HttpClient, servicio, controladores) y orden de las pantallas
docs/CLIENT_STYLES_DOTNET.md       guía de estilos
CONVENCION_REST.md                 convención común de los servidores REST de la familia
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion                                  entidades: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje + estado HTTP; ExigeLogin, ServidorNoDisponible
├── api/           ClienteApi, IClienteApi, RespuestaHttp, ConfiguracionApi     transporte HTTP: URL base, timeout, cabeceras, código + cuerpo
├── dto/           RespuestaBaseDto (+ los DTO del dominio)     cuerpos JSON del contrato, escritos a mano; no salen de servicio/
├── servicio/      ServicioBase, IAutenticador (+ los servicios del dominio)    única capa que conoce api y dto
├── controlador/   ControladorBase, ComandoAsincrono, Comando, NavegadorControlador, LoginControlador, InicioControlador
├── vista/         VentanaPrincipal, LoginVista, InicioVista, Textos, NegacionConvertidor
├── estilos/       Tema.xaml (tokens), Controles.xaml (estilos de controles)
├── estaticos/     fondo_app.png (imagen de ejemplo)
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo
```

Dependencias permitidas: `vista → controlador (ViewModel) → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**Los DTO no salen de `servicio/`**: el servicio los traduce a `modelo` (`record`) o a tipos simples. Los controladores nunca importan `api`, `dto` ni `System.Windows.Controls`: hablan con los servicios y exponen propiedades y comandos.
La vista (XAML) no tiene lógica de negocio ni llama al servidor; su código subyacente solo hace tareas de plataforma (foco inicial, entregar la lectura del `PasswordBox`).
Si el contrato cambia (rutas, campos, códigos) solo se tocan `servicio`, `api` y `dto`.

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas` (`GenerateProgramFile=false` porque el punto de entrada lo genera WPF desde `App.xaml`). Detener cualquier instancia en ejecución antes de `dotnet test`.

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
   - Recibe el servicio y la navegación como **delegados** (`Action`, `Action<string>`); los controladores no se conocen entre sí.
   - Comandos con `ComandoAsincrono(ejecutar, () => !Ocupado, this)`: el **`this` es obligatorio** en todo comando cuya habilitación dependa de `Ocupado`. Sin él, un botón enlazado mientras la pantalla cargaba queda deshabilitado para siempre (WPF solo vuelve a consultar cuando el comando avisa).
   - Una pantalla que **carga datos al abrirse** expone `Task CargarAsync()` que **no lanza**: pone `Ocupado`, captura `ExcepcionServicio` (solo `ex.Message` en `Mensaje`, nunca el código) y cualquier otra excepción (mensaje genérico), y si `ex.ExigeLogin` avisa con un delegado `Action<string> alExpirarSesion` para volver al login con ese mensaje.
   - Un fallo de red (`ServidorNoDisponible`) o un error de negocio (`400`, `404`) muestra el mensaje y **conserva lo que el usuario capturó** y la sesión.
4. **Vista** — `vista/ItemsVista.xaml` (`UserControl`) **sin lógica**, construida solo con los estilos de `Controles.xaml` (`PanelEncabezado`, `TextoEtiqueta`, `CajaTexto`, `Selector`, `BotonPrimario`, `BotonSecundario`, `PanelError`, `PanelResultado`, `Progreso`). Moldes: `LoginVista` e `InicioVista`. Enlazar `IsEnabled` de la raíz a `Ocupado` con `NegacionConvertidor` y mostrar el mensaje con `HayMensaje`.
5. **Navegación y composición** — en `vista/VentanaPrincipal.xaml` agregar el `DataTemplate` (controlador → vista); en `App.xaml.cs` crear **un** `HttpClient` (`new ClienteApi(ClienteApi.CrearHttpClient())`) y el servicio (`_autenticador = servicio`), el método `MostrarItems()` que hace `_navegador.Mostrar(pantalla); _ = pantalla.CargarAsync();`, y reemplazar `MostrarInicio` (y borrar `InicioVista`/`InicioControlador` y `AutenticadorPendiente`). La `Sesion` es **una sola** para toda la ejecución.
6. **Pruebas** (en `ec/edu/monster/pruebas/`):
   - El servicio con `ClienteApiFalso` (`Responder` por ruta): `200`, `400`, `401` con `AUTH-002/003`, `404`, error de plataforma, cuerpo que no es JSON, servidor no disponible, coma enviada como punto, sin sesión no se llama a la API.
   - Los controladores con un servicio doble (error de negocio solo con mensaje, red, token vencido, `Ocupado`).
   - **`CanExecute` de cada comando que cargue datos al abrir** (molde: `ComandosHabilitadosPruebas`).
   - **Una prueba de carga por vista** en `VistasCargaPruebas`: es la que detecta recursos repetidos o plantillas inválidas, que no fallan al compilar.

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP y luego por el código propio.** Nunca `EnsureSuccessStatusCode()`: descartaría el JSON de error. `ServicioBase` ya lo cumple.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se muestra. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente. Va en `Authorization: Bearer` **por petición**, nunca en `DefaultRequestHeaders`.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de la interfaz:** `async/await`. Un fallo de red no cierra la aplicación.
- La **contraseña** no se enlaza (`PasswordBox`): la vista entrega a `LoginControlador.LeerContrasenia` cómo leerla, y no se guarda en el controlador.
- Un solo `HttpClient` de larga vida (`ClienteApi.CrearHttpClient()`), no uno por petición.

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

- **Nombres únicos:** los pinceles de `Tema.xaml` llevan el nombre del token (`CampoTexto`) y los estilos el del control (`CajaTexto`); repetir una clave hace caer la aplicación al cargar la vista (`docs/CLIENT_STYLES_DOTNET.md` §7.3).
- **Disposición:** ventana dividida: **la pantalla a la izquierda** (560, fija; columna de máximo 420, centrada) y **la imagen a la derecha** (el resto, completa y centrada, sin deformar). Tamaño inicial 1100 × 720, **mínimo 1040 × 640** (560 + 420 + márgenes). La imagen es de la **ventana**, no de cada pantalla. Para no usarla, quitar el `Image` de `VentanaPrincipal.xaml`.
- **Las imágenes** van en `estaticos/` con acción de compilación `Resource` (ya declarada en el `.csproj` para `fondo_app.png`). La imagen de ejemplo es un PNG con transparencia: sobre el fondo claro no necesita un fondo propio.
- Una plantilla de control con una propiedad o recurso inválido **compila** y falla al cargar la vista: probar cada pantalla nueva con `VistasCargaPruebas` y abrirla.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.UrlBasePorDefecto` (URL base con «/» final). Sin recompilar: variable de entorno `API_BASE_URL`
  (PowerShell: `$env:API_BASE_URL = "http://<ip>:57592/api/"; dotnet run`). La barra final se agrega sola si falta.
- **HTTP y HTTPS:** el esquema de la URL base lo decide todo; no hay binding. **HTTPS exige un certificado de servidor válido y de confianza** (en desarrollo, `dotnet dev-certs https --trust`, y que el nombre al que se conecta, `localhost` o la IP, esté en el certificado).
  La validación del certificado **no** se desactiva en el cliente. Si falla, el cliente solo muestra «Servidor no disponible, intente más tarde» (la excepción original queda en `InnerException`).
- **Cambiar de servidor** (por ejemplo, de .NET a Java) es cambiar la URL base, siempre que ambos cumplan `CONVENCION_REST.md`.
- **Entorno de desarrollo de la familia:** IIS Express por HTTP (`http://localhost:57592/api/`); el perfil `http` (Kestrel) usa el puerto `5267`. Reglas de red y firewall: `IIS_CONFIG.md` del proyecto del servidor.
- **Visual Studio:** *Run* (F5). Detener la ejecución antes de compilar desde la terminal (el `.dll` queda bloqueado).
- **Terminal:** `dotnet run`.
- **Ejecutable único:** no incluido. `dotnet publish -c Release -r win-x64 --self-contained` lo genera; **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor ni interacción)
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación (una ruta protegida sin token debe responder `401` con un JSON, lo que confirma que la API está arriba):
```
curl -i http://localhost:57592/api/<ruta-protegida>
curl -s -X POST http://localhost:57592/api/auth/login -H "Content-Type: application/json" -d '{"usuario":"…","contrasenia":"…"}'
```

Verificación con la aplicación en marcha (la vista se revisa a mano; los controladores, con pruebas):
1. Acceso correcto lleva a la primera pantalla; credenciales malas muestran el mensaje del servidor y conservan el usuario.
2. Un botón de una pantalla que carga datos al abrirse **se habilita** al terminar la carga.
3. Error de negocio (`400`/`404`): se muestra el mensaje y la pantalla sigue usable, con la sesión abierta.
4. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» (≈ 10 s) y la aplicación sigue respondiendo.
5. Token vencido (`AUTH-003`): vuelve al acceso con el mensaje.
6. Ventana al tamaño inicial, maximizada y al mínimo (1040 × 640); imagen sin deformar; tildes y «ñ» en pantalla.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, carga de vistas, arranque del proceso, contra qué servidor) y que **no vio la ventana real**.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicio`. Es la convención común de la familia (`CONVENCION_REST.md`):

- HTTP + JSON (`application/json; charset=utf-8`), verbos `GET` (consultas) y `POST` (login y operaciones). **La URL base** se recibe completa por configuración; no se asume puerto ni prefijo.
- Cada respuesta, de éxito o de error, es un objeto con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los errores de la plataforma (ruta inexistente, método no permitido, `Content-Type` incorrecto, error interno) traen **solo** esos tres campos.
- **Código HTTP + código propio:** `200` éxito; `400` datos inválidos; `401` credenciales (`AUTH-001`) o token (`AUTH-002` inválido / `AUTH-003` vencido); `404` recurso inexistente; `405`/`415` error de programación del cliente; `500` error del servidor.
- El token viaja en `Authorization: Bearer <token>`, no como parámetro ni cuerpo. Existe un login público (`POST /auth/login`) que devuelve `token`.
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Todos los datos viajan como **texto**; los decimales con punto. Los campos que pueden faltar (`token`, listas) llegan `null` o ausentes en un error: no darlos por existentes.
- Si el servidor usa otro esquema de autenticación (cookies, OAuth/JWT con renovación), ampliar `ClienteApi`/`ServicioBase`.
- Para un servicio **SOAP** usar `soap_dotnet_escritorio`, no esta (la capa `servicio` es la que cambia).

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| La aplicación se cae al abrir una pantalla con `"System.Windows.Style" no es un valor válido para la propiedad…` | Una clave de recurso repetida entre un pincel de `Tema.xaml` y un estilo de `Controles.xaml`; renombrar el estilo con el nombre del control (`docs/CLIENT_STYLES_DOTNET.md` §7.3) |
| Un botón aparece deshabilitado y no se habilita nunca | El comando depende de `Ocupado` y se creó sin `this` (`new ComandoAsincrono(ejecutar, () => !Ocupado, this)`) |
| «Servidor no disponible, intente más tarde» | Servidor apagado, URL base distinta de la real, firewall, o (HTTPS) certificado sin confiar/vencido/de otro host. Probar la URL con curl; ver la `InnerException` |
| Por HTTPS falla y por HTTP funciona | Certificado de desarrollo sin confiar: `dotnet dev-certs https --trust`; consultar por el mismo nombre que figura en el certificado |
| Otro equipo recibe timeout y desde el PC del servidor con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín |
| Las rutas dan `404` o se concatenan mal | La URL base no termina en «/» (`ConfiguracionApi` lo corrige) o la ruta lleva barra inicial: usar `"auth/login"`, no `"/auth/login"` |
| Campos `null` en una respuesta correcta | `[JsonPropertyName]` distinto del nombre del contrato, o el servidor cambió el campo |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `415` / `ERR-415` | El cuerpo no se envió como JSON: usar `ClienteApi` (no armar `HttpRequestMessage` en otra clase) |
| Muestra «El servidor respondió con un error inesperado (HTTP nnn)» | El cuerpo del error no era JSON (página de IIS Express, proxy): revisar la URL y el servidor |
| `MSB3027`/`MSB3021`: no se puede copiar el `.dll` | Hay una instancia de la aplicación en ejecución: cerrarla antes de compilar |
| `CS0017` (más de un punto de entrada) al agregar pruebas | Falta `<GenerateProgramFile>false</GenerateProgramFile>` en el `.csproj` |
| La ventana abre pero la zona izquierda queda vacía | Falta el `DataTemplate` del controlador de la pantalla en `VentanaPrincipal.xaml` |
