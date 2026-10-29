# Plantilla: cliente de consola .NET para servicios SOAP

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, los tipos comunes, la verificación de la envoltura
y la entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0` (`Nullable` e `ImplicitUsings` habilitados). **Interfaz:** texto por la consola (`Console`), sin librerías de terceros. **Arquitectura:** capas, sin frameworks.
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` 10.0.652802) con **proxy generado**: `dotnet-svcutil` genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto (`wsdl/`), y `ws/ConfiguracionServicio` solo fija la URL completa, el binding y los tiempos límite. El WSDL **no se descarga en ejecución**.
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (copia en `docs/`, con el TNS fijo `http://ws.monster.edu.ec/` de R2): así entiende a un servidor Java o .NET que la cumpla.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su propio login y su propia sesión (en `modelo`); la plantilla no trae ninguno (§8).
- **Verificada:** `dotnet test` → 37 pruebas xUnit en verde, sin servidor: configuración de la URL y del binding (incluido el origen de las variables inyectado), verificación de la envoltura (`ServicioBase`), traducción de fallos de red (`TraductorRed`, con un canal simulado), excepción del cliente y entrada/salida de `VistaBase` con una consola simulada. `dotnet build` sin errores ni advertencias.
  Las recetas de §4 son las que se siguieron y verificaron al construir un cliente de consola .NET sobre esta base (probado contra su servidor .NET y comparado, con la misma entrada, con su par Java).
  **No se ha ejecutado:** la plantilla contra un servidor real (no trae proxy ni dominio) ni la secuencia de pantallas (no trae ninguna).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta** para generar los proxies: `dotnet-svcutil` falla con rutas largas (§4, paso 1).
2. **Renombrar** `soap_dotnet_consola.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.ws`…), sin intermedio de módulo. Las carpetas van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); los archivos, en PascalCase.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10; la primera vez restaura los paquetes (Internet).
4. **Configurar el servidor:** en `ws/ConfiguracionServicio.cs`, `UrlPorDefecto` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor). Con el servidor **ya actualizado y en marcha**, guardar su WSDL y sus esquemas en `wsdl/` y generar el proxy (§4, paso 1).
   **Si el servidor publica varios servicios** (cada uno con su WSDL y su **URL completa**; R9 prohíbe asumir «base + nombre del servicio»):
   - Guardar el WSDL y los esquemas de cada uno en `wsdl/` y generar **un proxy por servicio, cada uno en su propio espacio de nombres** (p. ej. `ec.edu.monster.ws.generado.consulta`). Los esquemas de un mismo servidor suelen definir los mismos tipos (como `RespuestaBase`) en el mismo namespace: en un espacio común las clases generadas chocan.
   - En `ConfiguracionServicio`, una constante con la URL completa de cada servicio y un método que la lea con `UrlConfigurada(servicio, urlPorDefecto)`:
     ```csharp
     public const string UrlConsultaPorDefecto = "http://localhost:5267/Consulta";
     public static string UrlConsulta() => UrlConfigurada("consulta", UrlConsultaPorDefecto);
     ```
   - Cada servicio se sobrescribe sin recompilar con la variable de entorno `WS_URL_CONSULTA` (el nombre del servicio, en mayúsculas). Un servicio Java se configura igual que uno .NET: es solo otra URL completa.
   - Los nombres de los servicios y de sus URL se registran en el `docs/PROJECT.md` del proyecto.
5. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
6. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar: `dotnet run`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

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
| **Entrada/salida** | `vista/VistaBase` (`Pedir`, `Elegir`, `Pausar`, `MostrarTitulo`, `MostrarPortada`, `Mostrar`, `MostrarError`) y `vista/Textos` | `VistaBase` no; las vistas del dominio la extienden y `Textos` crece con los textos del dominio |
| **Composición** | `Program.cs` (UTF-8, fin de la entrada, error de arranque) y `Aplicacion.cs` (`TODO` del montaje) | `Aplicacion` se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" ec Program.cs`).

## 3. Estructura

```
Program.cs                      codificación UTF-8, fin de la entrada y error de arranque; llama a Aplicacion
ec/edu/monster/
├── Aplicacion.cs               punto de composición (cliente SOAP, servicios, vistas, controladores) y bucle principal
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto: lo que ve el resto del cliente
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── ws/            ConfiguracionServicio, TraductorRed (+ IXCliente y XClienteWS del dominio)
│   └── generado/  <servicio>/Reference.cs                 proxy de dotnet-svcutil: se regenera, no se edita
├── controlador/                                           coordinan vista y servicio; devuelven un Resultado
├── vista/         VistaBase, Textos (+ las del dominio)   única capa que lee el teclado y escribe en la consola
└── pruebas/                                               xUnit + Moq, en el mismo proyecto (ver §4, paso 8)
wsdl/                           copias del WSDL y de los esquemas de cada servicio (de aquí se genera el proxy)
docs/                           CONVENCION_SOAP.md y CONVENCION_PRUEBAS.md (copias vigentes) y PROJECT.md (esqueleto del proyecto)
README.md                       esta guía: el único documento que queda en la raíz
```

Dependencias permitidas: `vista → controlador → servicios → ws`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `ws` ni `dto`**: recibe y devuelve texto y tipos simples; solo conoce a su controlador y a `Textos`.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo la implementación de `ws` conoce el protocolo, los tipos generados y las excepciones de WCF.
Si el WSDL cambia solo se tocan `ws`, `dto` y `servicios`. **Solo la vista usa `Console`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación SOAP y su pantalla de consola

Ejemplo: operación `obtenerItems()` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **WSDL y proxy** — con el servidor en marcha, guardar por servicio el WSDL y sus esquemas: `<URL>?wsdl`, `<URL>?xsd=xsd0` y `<URL>?xsd=xsd1` (un servidor CoreWCF; uno Java publica un solo `.xsd`), como `wsdl/<Servicio>.wsdl`, `<Servicio>.xsd0.xsd`, `<Servicio>.xsd1.xsd`.
   En cada `.wsdl`, **reescribir los `schemaLocation` absolutos a los nombres de archivo locales**. Instalar la herramienta una vez (`dotnet tool install -g dotnet-svcutil`, 8.0.0; con el SDK 10 conviene `DOTNET_ROLL_FORWARD=LatestMajor`) y, **en una carpeta de ruta corta** con un proyecto `net10.0` descartable, por cada servicio:
   ```
   dotnet-svcutil wsdl/Consulta.wsdl -d . -o gen/consulta/Reference.cs -n "*,ec.edu.monster.ws.generado.consulta" --noLogo
   ```
   Copiar cada `Reference.cs` a `ec/edu/monster/ws/generado/<servicio>/`. **No dejar que `dotnet-svcutil` modifique este proyecto** (añade paquetes `System.ServiceModel.*` 4.10 que no se usan). Lo generado: la clase `…Client` con constructor `(Binding, EndpointAddress)`, métodos `<operación>Async` con el nombre de la operación en minúscula inicial (`obtenerItemsAsync`), tipos con propiedades en minúscula (`estado`, `codigo`, `mensaje`) y las listas como clases que heredan de `List<T>` (se usa `Count`, no `Length`).
   **El código generado no se edita.** Cuando el servidor cambia su contrato: volver a guardar el WSDL y repetir.
2. **Interfaz y envoltorio** — `ws/IXCliente.cs` (interfaz propia con las operaciones que usa el cliente, retorno `Task<DTO propio?>`) y `ws/XClienteWS.cs`, que implementa la interfaz sobre el cliente generado, copia cada respuesta generada a un DTO propio y **traduce los fallos de red con `TraductorRed`**. Lo único que ven el servicio y las vistas es la interfaz. **El cliente no usa la URL de `Reference.cs`**: se crea con su propio binding y la URL configurada (R9, R10):
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
   - Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante) y exigir la sesión si el proyecto tiene login. Las reglas del dominio las decide el servidor y el cliente muestra su mensaje.
5. **Vista** — `vista/XVista : VistaBase`: pide datos con `Pedir(...)` / `Elegir(...)`, muestra con `Mostrar(...)` y `MostrarError(...)`, con los textos de `Textos`. Sus métodos públicos, `virtual`. Sin lógica de negocio ni llamadas al servidor.
6. **Controlador** — `controlador/XControlador`: valida campos vacíos **antes** de llamar (si faltan, no se llama al servicio; se informa el primero vacío), pide a la vista, llama al servicio, muestra el resultado o `e.Message` (solo el mensaje, nunca el código). Devuelve un `enum Resultado` que indica el **siguiente paso** (el controlador no conoce las pantallas por nombre).
   `catch (ExcepcionServicio e)` para los errores del servidor y de red (el programa no se cierra) y `catch (Exception e) when (e is not EndOfStreamException)` para lo inesperado (texto genérico de `Textos`). **El cierre de la entrada (`EndOfStreamException`) nunca se trata como error inesperado**: debe llegar a `Program.cs`, que termina en silencio.
7. **Composición** — en `Aplicacion`: crear el cliente (`XClienteWS.Crear()`), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Toda `Exception` no prevista se captura en el bucle (mensaje por `Console.Error`), nunca termina con una traza; solo `EndOfStreamException` se relanza.
8. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto: `GenerateProgramFile=false` evita el choque de puntos de entrada):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto, sin sesión).
   - **Controladores:** la vista y el servicio concretos se sustituyen con Moq (sus métodos públicos son `virtual`; el servicio se crea con `new Mock<XServicio>(null!, null!)`, los constructores solo asignan campos). Una vista se simula sin tocar la consola.
   - **Envoltorio:** `Convertir` se prueba campo a campo con los tipos generados.
   - **Recorrido completo:** `ConsolaFalsa` (en `pruebas/`) reemplaza la entrada y la salida estándar; las pruebas que la usan van en la colección `[Collection(ColeccionConsola.Nombre)]`, que no corre en paralelo (la consola es global). Permite comparar la transcripción de una sesión con el documento de la consola de referencia.
   - El contrato y el envoltorio de protocolo **no** se prueban con código: se verifican a mano con la lista de `docs/CONVENCION_PRUEBAS.md` §6.3. El criterio de qué probar y con cuánto detalle está en ese documento.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, tiempo agotado y `Fault` son `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se vuelve al menú.
- **Un fallo al cerrar el canal después de una operación exitosa no es un error de la operación** (`TraductorRed` lo descarta con `Abort`): una operación que ya cambió datos nunca debe mostrarse como «servidor no disponible».

## 5. Convenciones de consola

- **Codificación:** `Program.cs` fuerza UTF-8 **sin BOM** en la entrada y la salida (se ignora el `IOException` si no hay consola asociada). Las tildes y la «ñ» salen bien solo si la terminal también está en UTF-8 (`chcp 65001` en cmd; Windows Terminal suele estarlo).
- **Menús:** listas numeradas con `Elegir(titulo, opciones)` (o `Elegir(titulo, opciones, textoCero)` para rotular el 0, p. ej. «Salir»): devuelve el índice (base 0) o `-1` si se elige `0`. «0» es siempre cancelar / volver / salir. `Pausar()` espera Enter; `MostrarTitulo(...)` imprime una línea en blanco y el título subrayado con guiones del mismo largo.
- **Prompts:** `Pedir(etiqueta)` escribe `etiqueta: ` **sin salto de línea** y devuelve el texto ya recortado. `Pausar()` escribe `Presione Enter para continuar` también sin salto.
- **Portada:** `Textos.Portada` guarda el arte ASCII de arranque (vacío por defecto: no se muestra nada); `Aplicacion` llama una vez a `MostrarPortada()` de cualquier vista antes del bucle. Si se define con un literal sin formato (`"""`), normalizar los saltos con `.ReplaceLineEndings("\n")`.
- **Errores:** se muestran con `MostrarError(...)` (prefijo `[!] `) y solo con el mensaje. Una entrada inválida se vuelve a pedir; no se lanza excepción.
- **Claves:** `Pedir(...)` hace eco del texto. `Console.ReadKey(intercept: true)` permitiría ocultarlas, pero no funciona con la entrada redirigida; si un proyecto decide mostrarlas en texto plano, lo registra en su `docs/PROJECT.md`. Nunca imprimir ni registrar la clave fuera de lo que la pantalla muestra al digitar.
- **Entrada cerrada:** `Pedir` y `Pausar` lanzan `EndOfStreamException` al terminar la entrada (Ctrl+Z + Enter en Windows, Ctrl+D, o `stdin` agotado), así un bucle de menú no gira sin fin. `Program.cs` la captura, imprime un salto de línea y termina el programa en silencio.
- **Esperas:** la llamada al servidor bloquea la consola (hasta el tiempo límite de 10 s); mostrar «Consultando…» / «Procesando…» antes de llamar si la operación puede tardar.
- **Salida:** presentar el resultado en pocas líneas, con el mismo formato y los mismos textos que los demás clientes del proyecto (escritorio, web, móvil), tomados de `Textos`. Los importes y las fechas se formatean con la cultura invariante.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionServicio.UrlPorDefecto` (URL completa). Sin recompilar: la variable de entorno `WS_URL`. Con varios servicios, una por servicio: `WS_URL_<SERVICIO>` (§1, paso 4). En PowerShell: `$env:WS_URL_CONSULTA = 'http://host:5267/Consulta'` antes de `dotnet run`.
- **Servidor .NET local:** con el perfil `http` (Kestrel) el puerto por defecto es 5267; con `IIS Express`, 57592 (solo HTTP; ver `context/entornos/IIS_CONFIG.md`). Si el servidor corre en IIS Express, es Visual Studio quien lo mantiene vivo: si se detiene, el servidor está apagado.
- **El WSDL está empaquetado** (`wsdl/`): crear el cliente no abre ninguna conexión y funciona con el servidor apagado. El fallo llega al **llamar** una operación (`CommunicationException`), que `TraductorRed` traduce a `RED`: el programa muestra «Servidor no disponible, intente más tarde» en vez de la traza.
- **Visual Studio:** *Run* ejecuta el proyecto; **detener la depuración antes de volver a compilar** (si no, `MSB3021`/`MSB3027`: el `.exe` queda bloqueado). La ventana de consola de VS no siempre es una terminal real: probar tildes y el fin de la entrada también en una terminal.
- **Terminal:** `dotnet run` (compila antes). Con la entrada redirigida: `cmd /c "dotnet run --no-build < entrada.txt > salida.txt"` (PowerShell no admite `<`).

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor)
dotnet build                 # compilar
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con el programa en marcha (las vistas y los controladores se prueban a mano o con las vistas simuladas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor y se vuelve al menú.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y el programa sigue respondiendo.
4. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.
5. Tildes y «ñ» en entrada y salida; entrada vacía, opción inválida y cierre de la entrada (Ctrl+Z) en los menús.
6. **Si existe un cliente de consola de referencia** (otra tecnología del mismo proyecto): ejecutar ambos con el mismo archivo de entrada, con solo operaciones que no modifican datos y siempre con el Enter de cada pausa, y comparar la salida. Las dos bases deben estar en el mismo estado.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_SOAP.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `servicios`.
Lo que **el código ya cumple** y lo que **debe cumplir el servidor**:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): lo declara el esquema del servidor (`elementFormDefault="qualified"`) y el proxy generado lo respeta. Si la copia del esquema no lo declara, los campos llegan `null` sin error (R3).
- **Namespace (TNS) fijo `http://ws.monster.edu.ec/`** en todos los servicios del equipo (R2).
- **SOAPAction = `TNS + Servicio + "/" + operación`**: viene en cada operación del WSDL y el proxy la envía (R4).
- **El cliente no depende del nombre del puerto ni del binding** (R10): se crea con su propio binding y la URL configurada.
- **La URL completa del servicio** se recibe por configuración y sustituye a la del `Reference.cs`; el cliente no asume puerto ni «base + servicio» (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los campos propios de la respuesta nunca se llaman `estado`, `codigo` ni `mensaje`.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R7); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R8); los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5). Si el servidor cambia su contrato hay que regenerar el proxy y actualizar el contrato y los DTO del cliente.
- Un WSDL de CoreWCF trae el esquema de serialización de Microsoft (`xsd1`) y una lista como clase que hereda de `List<T>`; un WSDL Java, un solo `.xsd`. Con campos `nillable` el proxy sigue usando `string`.
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (token, credenciales en cada operación, nada) lo define cada servidor y su cliente lo implementa en `servicios`, con la sesión en `modelo`.
- Para un servicio **REST** usar la plantilla `rest_dotnet_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `CommunicationException` / «Servidor no disponible» al llamar una operación | Servidor apagado (con IIS Express, Visual Studio detenido) o `UrlPorDefecto` distinta de la real. Abrir `<URL>?wsdl` en el navegador. Con `TraductorRed` el usuario ve «Servidor no disponible» |
| `dotnet-svcutil` falla (`MSB3030`, rutas demasiado largas) | Generar en una carpeta de ruta corta (§4, paso 1) |
| `dotnet-svcutil` añade paquetes `System.ServiceModel.*` 4.10 a un proyecto | Se ejecutó dentro del proyecto: generar en una carpeta descartable y copiar solo los `Reference.cs` |
| 26 avisos `NU1902`/`NU1903` al restaurar | Dependencias transitivas de `System.ServiceModel.*`: `System.Security.Cryptography.Pkcs` y `.Xml` ya van fijadas en 10.0.14 en el `.csproj` |
| Cambió el servidor y el cliente no ve la operación nueva | Guardar de nuevo el WSDL y los esquemas, regenerar el proxy y agregar la operación a la interfaz y al envoltorio (§4) |
| Respuesta con campos vacíos | Se guardó un WSDL antiguo, o el envoltorio no copia un campo de la respuesta generada al DTO propio |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | El WSDL guardado no trae el `soapAction` de la operación: guardar el WSDL del servidor actualizado y regenerar |
| El servidor no recibe los parámetros (llegan vacíos) | El esquema guardado no califica los elementos (`elementFormDefault="qualified"`): guardar el del servidor actualizado |
| `ListaX` no tiene `Length` | Es una lista (`List<T>`): usar `Count` |
| Errores por tipos duplicados (`RespuestaBase`…) o proxies que se pisan | Varios servicios generados en el mismo espacio de nombres: darle a cada uno el suyo (`-n "*,ec.edu.monster.ws.generado.<servicio>"`) |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó la configuración de `Program.cs` |
| El menú repite «Opción inválida» sin parar | Ya no debería ocurrir: al cerrarse la entrada el programa termina (§5, «Entrada cerrada») |
| Una prueba se cuelga | Un canal simulado cuyo `BeginClose` no invoca el callback: `Task.Factory.FromAsync` espera para siempre (ver `TraductorRedPruebas`) |
| Pruebas que usan la consola fallan de forma intermitente | Falta `[Collection(ColeccionConsola.Nombre)]`: la consola es global y esas pruebas no pueden correr en paralelo |
| `MSB3021`/`MSB3027`: no se puede copiar `apphost.exe` al compilar | El cliente sigue en ejecución (p. ej. depurando en Visual Studio): detenerlo antes de compilar |
