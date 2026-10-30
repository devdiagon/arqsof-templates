# Plantilla: cliente de consola .NET para servicios REST

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga una API **REST** con el esquema de respuesta de esta familia de proyectos (envoltura `estado` / `codigo` / `mensaje`). Es la plantilla **mínima**: trae el transporte HTTP, la lectura de la envoltura, los tipos comunes y la entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4. Es la contraparte de `soap_dotnet_consola`: todo es igual salvo la capa de protocolo (`ws` ↔ `api`).

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** C# 14, `net10.0` (`Nullable` e `ImplicitUsings` habilitados). **Interfaz:** texto por la consola (`Console`), sin librerías de terceros. **Arquitectura:** capas, sin frameworks.
- **Transporte:** `HttpClient` del SDK y `System.Text.Json`; ningún paquete extra. Un solo `HttpClient` de larga vida, con tiempo límite de 10 s.
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`). El contrato concreto de cada proyecto (rutas, verbos, cuerpos, autenticación) va en su `docs/ENDPOINTS_REST.md`, que se **escribe o se copia del servidor antes de implementar**.
- **Autenticación:** **fuera de la plantilla base.** Cada proyecto define cómo viajan las credenciales o el token (R8, §8) y su sesión (en `modelo`); la plantilla no trae ninguno.
- **Mapeo JSON ↔ DTO:** los DTO no llevan anotaciones; `api/TraductorApi` configura nombres en camelCase, lectura sin distinguir mayúsculas y `Populate` (`ARQUITECTURA_CLIENTE.md` §4.5).
- **Verificada:** `dotnet test` → **70 pruebas** xUnit en verde, sin servidor ni red: configuración de la URL, armado y lectura de peticiones (`TraductorApi`, incluidos todos los HTTP de error, `RED` y `SRV-000`), el transporte real contra un `HttpMessageHandler` simulado, verificación de la envoltura (`ServicioBase`), excepción del cliente y entrada/salida de `VistaBase` con una consola simulada. `dotnet build` sin errores ni advertencias. La capa `api` es la del cliente de consola .NET REST construido sobre esta base, probado a mano contra su servidor y con el mismo comportamiento que sus pares SOAP y Java.
  **No se ha ejecutado:** la plantilla contra un servidor real (no trae dominio ni clientes `…Api`) ni la secuencia de pantallas (no trae ninguna).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real, decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**.
2. **Renombrar** `rest_dotnet_consola.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** es la convención y **no se cambia**: es la raíz y cada capa es un espacio de nombres directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.api`…). Las carpetas van en minúscula y coinciden con el espacio de nombres (`ec/edu/monster/<capa>/`); los archivos, en PascalCase.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10.
4. **Configurar el servidor:** en `api/ConfiguracionApi.cs`, `UrlBasePorDefecto` = **URL base completa de la API**, con host, puerto, contexto (si lo hay) y prefijo `api`, **con «/» final** (R1). Se sobrescribe sin recompilar con la variable de entorno `API_BASE_URL`. Las rutas de las operaciones se le añaden relativas y sin barra inicial.
5. **Copiar el contrato del servidor** a `docs/ENDPOINTS_REST.md` (una fila por operación).
6. **Completar `docs/PROJECT.md`** (viene como esqueleto) y mantenerlo al día.
7. Con el servidor en marcha, ejecutar: `dotnet run`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

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
| **Entrada/salida** | `vista/VistaBase` (`Pedir`, `Elegir`, `Pausar`, `MostrarTitulo`, `MostrarPortada`, `Mostrar`, `MostrarError`) y `vista/Textos` | `VistaBase` no; las vistas del dominio la extienden y `Textos` crece |
| **Composición** | `Program.cs` (UTF-8, fin de la entrada, error de arranque) y `Aplicacion.cs` (`TODO` del montaje) | `Aplicacion` se completa con el cliente, los servicios, vistas y controladores |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" ec Program.cs`).

## 3. Estructura

```
Program.cs                      codificación UTF-8, fin de la entrada y error de arranque; llama a Aplicacion
ec/edu/monster/
├── Aplicacion.cs               punto de composición (cliente REST, servicios, vistas, controladores) y bucle principal
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; ServidorNoDisponible, RespuestaIlegible
├── dto/           RespuestaBaseDto (+ los del dominio)    clases sin atributos de serialización, todos los campos como texto
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── api/           ConfiguracionApi, ITransporte, TransporteHttp, RespuestaHttp, TraductorApi (+ IXCliente y XClienteApi del dominio)
├── controlador/                                           coordinan vista y servicio; devuelven un Resultado
├── vista/         VistaBase, Textos (+ las del dominio)   única capa que lee el teclado y escribe en la consola
└── pruebas/                                               xUnit + Moq, en el mismo proyecto (ver §4, paso 8)
docs/                           CONVENCION_REST.md y CONVENCION_PRUEBAS.md (copias vigentes), ENDPOINTS_REST.md (del proyecto) y PROJECT.md (esqueleto)
README.md                       esta guía: el único documento que queda en la raíz
```

Dependencias permitidas: `vista → controlador → servicios → api`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca usa `api` ni `dto`**: recibe y devuelve texto y tipos simples; solo conoce a su controlador y a `Textos`.
**`servicios` solo conoce la interfaz del cliente (`IXCliente`), no su implementación**; solo `api` conoce HTTP y JSON. Si el contrato cambia solo se tocan `api`, `dto` y `servicios`. **Solo la vista usa `Console`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación REST y su pantalla de consola

Ejemplo: `GET items` que devuelve una lista y `POST items` que crea uno. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Interfaz y cliente** — `api/IXCliente.cs` (interfaz propia con **un método por fila** de `ENDPOINTS_REST.md`, con el nombre de la operación, retorno `Task<DTO propio?>`) y `api/XClienteApi.cs`, que la implementa con `TraductorApi`. Lo único que ven el servicio y las vistas es la interfaz:
   ```csharp
   public sealed class XClienteApi : IXCliente
   {
       private readonly ITransporte _transporte;

       public XClienteApi(ITransporte transporte) { _transporte = transporte; }

       public static IXCliente Crear() => new XClienteApi(TransporteHttp.Crear());   // un solo HttpClient para todo el programa

       public async Task<RespuestaItemsDto?> ListarItemsAsync() =>
           await TraductorApi.LlamarAsync<RespuestaItemsDto>(_transporte, "GET", "items", null, null);

       public async Task<RespuestaItemDto?> RegistrarItemAsync(string codigo, string nombre) =>
           await TraductorApi.LlamarAsync<RespuestaItemDto>(_transporte, "POST", "items",
               TraductorApi.Cuerpo("codigoItem", codigo, "nombre", nombre), null);

       public async Task<RespuestaItemDto?> ConsultarItemAsync(string codigo) =>
           await TraductorApi.LlamarAsync<RespuestaItemDto>(_transporte, "GET",
               "items/" + TraductorApi.Codificar(codigo), null, null);      // cada variable de ruta, codificada (R10)
   }
   ```
   Reglas de la capa `api`:
   - **Se lee la envoltura también con HTTP de error** (400, 401, 404, 422…): el `mensaje` del servidor nunca se descarta. **Nunca `EnsureSuccessStatusCode()`.**
   - Sin respuesta (servidor caído, red, tiempo agotado) → `RED`; cuerpo vacío o que no es JSON (página de un proxy, `502`) → `SRV-000` (R11, R12). `TraductorApi` ya lo hace.
   - Todo dato viaja como **texto** (R9); los decimales con punto.
   - Un fallo de programación (`InvalidOperationException`…) **no** se disfraza de `RED`.
2. **DTO propio** — clase `…Dto` en `dto` que extienda `RespuestaBaseDto`, con propiedades `{ get; set; }`, todas texto y `string?` (los campos solo existen si hubo éxito); una lista se inicializa vacía y **sin setter** (`public List<string> Items { get; } = [];`): **nunca nula**. **Sin atributos de JSON**: el nombre JSON es el camelCase del nombre de la propiedad (`CodigoItem` ↔ `codigoItem`). `Populate` (en `TraductorApi`) rellena la lista de solo lectura.
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
   - Aquí van las reglas **del cliente**: preparar los datos de pantalla (recortar; decimales con **punto**, cultura invariante) y exigir la sesión si el proyecto tiene login. Las reglas del dominio las decide el servidor y el cliente muestra su mensaje.
4. **Vista** — `vista/XVista : VistaBase`: pide datos con `Pedir(...)` / `Elegir(...)`, muestra con `Mostrar(...)` y `MostrarError(...)`, con los textos de `Textos`. Sus métodos públicos, `virtual`. Sin lógica de negocio ni llamadas al servidor.
5. **Controlador** — `controlador/XControlador`: valida campos vacíos **antes** de llamar (si faltan, no se llama al servicio; se informa el primero vacío), pide a la vista, llama al servicio, muestra el resultado o `e.Message` (solo el mensaje, nunca el código). Devuelve un `enum Resultado` que indica el **siguiente paso**.
   `catch (ExcepcionServicio e)` para los errores del servidor y de red (el programa no se cierra) y `catch (Exception e) when (e is not EndOfStreamException)` para lo inesperado (texto genérico de `Textos`). **El cierre de la entrada (`EndOfStreamException`) nunca se trata como error inesperado**: debe llegar a `Program.cs`, que termina en silencio.
6. **Composición** — en `Aplicacion`: crear el cliente (`XClienteApi.Crear()`), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`. Toda `Exception` no prevista se captura en el bucle (mensaje por `Console.Error`); solo `EndOfStreamException` se relanza.
7. **Autenticación (si el proyecto la tiene)** — se define en `docs/ENDPOINTS_REST.md` (R8) y se implementa **solo en `api`**: las credenciales siguen siendo parámetros de la interfaz del cliente y `XClienteApi` las coloca donde el contrato diga. Con `Authorization: Basic` por petición, por ejemplo:
   ```csharp
   string Basic(string usuario, string clave) =>
       "Basic " + Convert.ToBase64String(Encoding.UTF8.GetBytes(usuario + ":" + clave));
   // ... TraductorApi.LlamarAsync<...>(_transporte, "GET", ruta, null, Basic(usuario, clave))
   ```
   Nunca en la URL, nunca en las cabeceras por defecto del `HttpClient`, nunca en un registro. La sesión (si la hay) vive en `modelo`.
8. **Pruebas** (xUnit + Moq, en `ec/edu/monster/pruebas`, en el mismo proyecto: `GenerateProgramFile=false` evita el choque de puntos de entrada):
   - **Servicios:** la interfaz del cliente (`IXCliente`) se sustituye con Moq (OK, error de negocio, red, respuesta nula, coma enviada como punto, sin sesión).
   - **Clientes `…Api`:** con `TransporteFalso` (en `pruebas/`) se comprueba el verbo, la ruta, el cuerpo y la cabecera de cada método, y que un HTTP de error llega como DTO y no como excepción.
   - **Controladores:** la vista y el servicio concretos se sustituyen con Moq (el servicio se crea con `new Mock<XServicio>(null!, null!)`). Una vista se simula sin tocar la consola.
   - **Recorrido completo:** `ConsolaFalsa` reemplaza la entrada y la salida estándar; las pruebas que la usan van en la colección `[Collection(ColeccionConsola.Nombre)]`, que no corre en paralelo (la consola es global).
   - El contrato **no** se prueba con código: se verifica a mano contra `docs/ENDPOINTS_REST.md` con la lista de `docs/CONVENCION_PRUEBAS.md` §6.3.

Reglas que se repiten en todo dominio:
- **Un error de negocio llega con HTTP 4xx o 5xx y con la envoltura en el cuerpo**; el cliente decide por el `codigo` y muestra el `mensaje`. Solo red, tiempo agotado y cuerpo ilegible son `RED` y `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se vuelve al menú.
- `ERR-404`, `ERR-405` y `ERR-415` (errores de plataforma) se muestran como cualquier otro error, pero indican un defecto del cliente (ruta, verbo o `Content-Type`).

## 5. Convenciones de consola

- **Codificación:** `Program.cs` fuerza UTF-8 **sin BOM** en la entrada y la salida (se ignora el `IOException` si no hay consola asociada). Las tildes y la «ñ» salen bien solo si la terminal también está en UTF-8 (`chcp 65001` en cmd; Windows Terminal suele estarlo).
- **Menús:** listas numeradas con `Elegir(titulo, opciones)` (o `Elegir(titulo, opciones, textoCero)` para rotular el 0, p. ej. «Salir»): devuelve el índice (base 0) o `-1` si se elige `0`. «0» es siempre cancelar / volver / salir. `Pausar()` espera Enter; `MostrarTitulo(...)` imprime una línea en blanco y el título subrayado con guiones del mismo largo.
- **Prompts:** `Pedir(etiqueta)` escribe `etiqueta: ` **sin salto de línea** y devuelve el texto ya recortado. `Pausar()` escribe `Presione Enter para continuar` también sin salto.
- **Portada:** `Textos.Portada` guarda el arte ASCII de arranque (vacío por defecto: no se muestra nada); `Aplicacion` llama una vez a `MostrarPortada()` de cualquier vista antes del bucle. Si se define con un literal sin formato (`"""`), normalizar los saltos con `.ReplaceLineEndings("\n")`.
- **Errores:** se muestran con `MostrarError(...)` (prefijo `[!] `) y solo con el mensaje. Una entrada inválida se vuelve a pedir; no se lanza excepción.
- **Claves:** `Pedir(...)` hace eco del texto. `Console.ReadKey(intercept: true)` permitiría ocultarlas, pero no funciona con la entrada redirigida; si un proyecto decide mostrarlas en texto plano, lo registra en su `docs/PROJECT.md`. Nunca imprimir ni registrar la clave fuera de lo que la pantalla muestra al digitar.
- **Entrada cerrada:** `Pedir` y `Pausar` lanzan `EndOfStreamException` al terminar la entrada (Ctrl+Z + Enter en Windows, Ctrl+D, o `stdin` agotado). `Program.cs` la captura, imprime un salto de línea y termina el programa en silencio.
- **Esperas:** la llamada al servidor bloquea la consola (hasta el tiempo límite de 10 s); mostrar «Consultando…» / «Procesando…» antes de llamar si la operación puede tardar.
- **Salida:** presentar el resultado en pocas líneas, con el mismo formato y los mismos textos que los demás clientes del proyecto, tomados de `Textos`. Los importes y las fechas se formatean con la cultura invariante.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.UrlBasePorDefecto` (URL base completa, con «/» final). Sin recompilar: la variable de entorno `API_BASE_URL`. En PowerShell: `$env:API_BASE_URL = 'http://host:5267/api/'` antes de `dotnet run`.
- **Servidor .NET local:** con el perfil `http` (Kestrel) el puerto por defecto es 5267; con `IIS Express`, 57592 (solo HTTP; ver `context/entornos/IIS_CONFIG.md`). Si el servidor corre en IIS Express, es Visual Studio quien lo mantiene vivo.
- **Crear el cliente no abre ninguna conexión**: funciona con el servidor apagado. El fallo llega al **llamar** una operación (`HttpRequestException` o tiempo agotado), que `TraductorApi` traduce a `RED`: el programa muestra «Servidor no disponible, intente más tarde» en vez de la traza.
- **Visual Studio:** *Run* ejecuta el proyecto; **detener la depuración antes de volver a compilar** (si no, `MSB3021`/`MSB3027`). La ventana de consola de VS no siempre es una terminal real: probar tildes y el fin de la entrada también en una terminal.
- **Terminal:** `dotnet run` (compila antes). Con la entrada redirigida: `cmd /c "dotnet run --no-build < entrada.txt > salida.txt"` (PowerShell no admite `<`).

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor ni red)
dotnet build                 # compilar
dotnet run                   # ejecutar contra el servidor configurado
```

Verificación con el programa en marcha (las vistas y los controladores se prueban a mano o con las vistas simuladas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor y se vuelve al menú.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y el programa sigue respondiendo.
4. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.
5. Tildes y «ñ» en entrada y salida; entrada vacía, opción inválida y cierre de la entrada (Ctrl+Z) en los menús.
6. **Si existe un cliente de consola de referencia** (otra tecnología del mismo proyecto): ejecutar ambos con el mismo archivo de entrada, con solo operaciones que no modifican datos y siempre con el Enter de cada pausa, y comparar la salida. Las dos bases deben estar en el mismo estado.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`.

- **URL base** con prefijo `api` y «/» final (R1); rutas relativas y sin barra inicial.
- **JSON UTF-8** en peticiones y respuestas (R3); el cuerpo de las peticiones lleva `Content-Type: application/json`, o el servidor responde `ERR-415`.
- **Toda respuesta, de éxito o de error, lleva la envoltura** `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación (R5). Los campos propios nunca se llaman `estado`, `codigo` ni `mensaje`. Las listas llegan vacías, nunca ausentes.
- **HTTP por categoría del error** (R7): el cliente decide por el `codigo` de la envoltura, no solo por el HTTP.
- **Errores de plataforma** (ruta inexistente, verbo no permitido, `Content-Type` incorrecto, JSON mal formado, excepción no prevista) también con la envoltura (R6).
- **Todo dato como texto** (R9): las respuestas nunca llevan números JSON.
- **Autenticación:** no la asume la plantilla (R8). Cómo se identifica quien opera lo define cada servidor en su `ENDPOINTS_REST.md` y su cliente lo implementa en `api`, con la sesión en `modelo`.
- Para un servicio **SOAP** usar la plantilla `soap_dotnet_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «Servidor no disponible» al llamar una operación | Servidor apagado (con IIS Express, Visual Studio detenido) o `UrlBasePorDefecto` distinta de la real. Probar la URL en el navegador o con `curl` |
| `SRV-000 … (HTTP 404)` con una página HTML | La URL base no incluye el prefijo `api` o el contexto del servidor; revisar `ConfiguracionApi` |
| `ERR-404` / `ERR-405` / `ERR-415` en pantalla | Defecto del cliente: ruta mal escrita, verbo equivocado o falta el `Content-Type` (el transporte lo envía en todo cuerpo) |
| Un campo llega `null` aunque el servidor lo envía | El nombre de la propiedad del DTO no es el camelCase del JSON (`CodigoItem` ↔ `codigoItem`) |
| Una lista del DTO llega siempre vacía | La lista tiene setter nulo o se declaró sin inicializar; debe ser `{ get; } = []` y se rellena con `Populate` (no quitarlo de `TraductorApi.Opciones`) |
| El servidor rechaza un importe con coma | La coma se normaliza a punto en el **servicio**, no en la vista |
| La clave aparece en la URL o en un registro | Las credenciales van solo en el cuerpo o en `Authorization`, por petición (§4, paso 7) |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó la configuración de `Program.cs` |
| El menú repite «Opción inválida» sin parar | Ya no debería ocurrir: al cerrarse la entrada el programa termina (§5, «Entrada cerrada») |
| Pruebas que usan la consola fallan de forma intermitente | Falta `[Collection(ColeccionConsola.Nombre)]`: la consola es global y esas pruebas no pueden correr en paralelo |
| `MSB3021`/`MSB3027`: no se puede copiar `apphost.exe` al compilar | El cliente sigue en ejecución (p. ej. depurando en Visual Studio): detenerlo antes de compilar |
