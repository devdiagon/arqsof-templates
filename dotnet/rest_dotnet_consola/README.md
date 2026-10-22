# Plantilla: cliente de consola .NET para APIs REST

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga una API **REST/JSON** con el esquema de
respuesta de esta familia de proyectos (`CONVENCION_REST.md`). Es la plantilla **mínima**: trae el transporte HTTP, los tipos comunes, la base de los servicios
y la entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye DTO del dominio, pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

- **Lenguaje:** C# 14, `net10.0` (`Nullable` e `ImplicitUsings` habilitados). **Interfaz:** texto por la consola (`Console`). **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** `HttpClient` + `System.Net.Http.Json` + `System.Text.Json`, todo incluido en el SDK: **ningún paquete** en producción. No hay proxy ni WSDL: los DTO se escriben a mano.
- **Convención REST:** sigue `CONVENCION_REST.md` (en esta carpeta): así entiende a un servidor Java o .NET que la cumpla; cambiar de servidor es cambiar solo la URL base.
- **Verificada:** `dotnet test` → 48 pruebas xUnit en verde (URL base, transporte con un `HttpMessageHandler` falso, traducción de fallos de red, `ServicioBase`: códigos HTTP, token, cuerpos que no son JSON) y `dotnet run` imprime la URL configurada.
  **No se ha ejecutado:** la plantilla contra un servidor real (no hay dominio, login ni pantallas). La receta de §4 es la que se siguió en el cliente de origen.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**.
2. **Renombrar** `rest_dotnet_consola.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10; la primera vez restaura los paquetes de prueba (Internet).
4. **Configurar el servidor:** en `ec/edu/monster/api/ConfiguracionApi.cs`, `UrlBasePorDefecto` = **URL base de la API** (host, puerto y prefijo, **con «/» final**; p. ej. `http://localhost:57592/api/`).
5. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
6. **Probar el servidor** (una petición con curl o el navegador, §7) y construir el dominio con la receta de §4.
7. **Ejecutar:** `dotnet run`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Transporte HTTP/JSON** | `api/ClienteApi`, `api/IClienteApi`, `api/RespuestaHttp` (Bearer por petición, JSON UTF-8, fallos de red → `ServidorNoDisponible`) | No |
| **URL base** | `api/ConfiguracionApi` (`UrlBasePorDefecto`, variable `API_BASE_URL`, normalización con «/» final, timeout 10 s) | `UrlBasePorDefecto` siempre; el resto no |
| **Respuesta común** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`) | No; los DTO del dominio la extienden |
| **DTO del dominio** | — (`dto/SolicitudXxxDto`, `dto/RespuestaXxxDto`) | **Se crean** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`Codigo`, `EstadoHttp`, `ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`EnviarAsync<T>`, `ExigirToken`, decisión por código HTTP, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Entrada/salida** | `vista/VistaBase` (`Pedir`, `Elegir`, `Mostrar`, `MostrarError`, `Titulo`) | No; las vistas del dominio la extienden |
| **Composición** | `Program.cs` (UTF-8, un `HttpClient` y una `Sesion`, manejo de entrada cerrada y de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas** | `servicio/`, `controlador/` (vacía, `.gitkeep`), `vista/` | **Se crean** (receta §4) |
| **Pruebas** | `pruebas/` (URL, transporte, `ServicioBase`, doble `ClienteApiFalso`) | Se conservan; se agregan las del dominio |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" ec Program.cs`).

## 3. Estructura

```
Program.cs                         punto de composición (HttpClient, Sesion, cliente, servicios, vistas, controladores) y bucle principal
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion                                  entidades: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje + estado HTTP; ExigeLogin, ServidorNoDisponible
├── api/           ClienteApi, IClienteApi, RespuestaHttp, ConfiguracionApi     transporte HTTP: URL base, timeout, cabeceras, código + cuerpo
├── dto/           RespuestaBaseDto (+ los DTO del dominio)     cuerpos JSON del contrato, escritos a mano; no salen de servicio/
├── servicio/      ServicioBase (+ los servicios del dominio)   única capa que conoce api y dto
├── controlador/   (vacía)                                 coordinan vista y servicio; devuelven un Resultado
├── vista/         VistaBase (+ las del dominio y sus interfaces IXxxVista)    única capa que lee el teclado y escribe en la consola
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo
```

Dependencias permitidas: `vista ← controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**Los DTO no salen de `servicio/`**: el servicio los traduce a `modelo` (`record`) o a tipos simples. La vista nunca importa `api` ni `dto`: recibe y devuelve texto y tipos simples (o `modelo`).
Si el contrato cambia (rutas, campos, códigos) solo se tocan `servicio`, `api` y `dto`. **Solo la vista usa `Console`** para leer y escribir; ni el controlador ni el servicio imprimen ni leen.

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas` (`GenerateProgramFile=false` porque el punto de entrada es `Program.cs`). Detener cualquier instancia en ejecución antes de `dotnet test`.

## 4. Receta: agregar un endpoint y su pantalla de consola

Ejemplo: `GET /items` que devuelve una lista (protegido) y `POST /auth/login` (público). Se sigue **siempre el mismo orden**, de abajo hacia arriba.

1. **DTO** — en `dto/`, uno por cuerpo del contrato, con `[JsonPropertyName]` en cada propiedad (el contrato no depende de la política *camelCase*) y tipos que toleren `null`:
   ```csharp
   public class SolicitudLoginDto  { [JsonPropertyName("usuario")] public string? Usuario { get; set; }
                                     [JsonPropertyName("contrasenia")] public string? Contrasenia { get; set; } }
   public class RespuestaLoginDto : RespuestaBaseDto { [JsonPropertyName("token")] public string? Token { get; set; } }
   public class RespuestaListaDto : RespuestaBaseDto { [JsonPropertyName("elementos")] public List<string>? Elementos { get; set; } = []; }
   ```
2. **Servicio** — `servicio/ServicioXxx : ServicioBase` (recibe `IClienteApi` y `Sesion`). `EnviarAsync<T>` exige el token, lo envía en la cabecera, decide por el código HTTP y lanza `ExcepcionServicio` en cualquier error:
   ```csharp
   public async Task IniciarSesionAsync(string usuario, string contrasenia)
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
   - Es la **única** capa que ve los DTO: traduce a `modelo` o a tipos simples.
3. **Vista** — `vista/XxxVista : VistaBase` (con una interfaz `IXxxVista` para probar el controlador): pide datos con `Pedir(...)` / `Elegir(...)`, muestra con `Mostrar(...)` y `MostrarError(...)`. Sin lógica de negocio ni llamadas al servidor.
4. **Controlador** — `controlador/XxxControlador`: ejecuta **una sola vuelta**, pide a la vista, llama al servicio y muestra el resultado o `ex.Message` (solo el mensaje, nunca el código). Devuelve un `enum Resultado` (p. ej. `Continuar`, `SesionExpirada`, `Salir`).
   - `ex.ExigeLogin` → `SesionExpirada`.
   - `ex.ServidorNoDisponible` → mostrar el mensaje y **preguntar si continuar** (no reintentar en bucle ni cerrar el programa).
   - Los errores de negocio (`400`, `404`) → mostrar el mensaje y volver al menú sin cerrar la sesión.
5. **Composición** — en `Program.cs`: el `HttpClient` y la `Sesion` (**uno de cada uno** para toda la ejecución), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Capturar `EndOfStreamException` (entrada cerrada) y cualquier excepción no prevista (mensaje por `Console.Error`), nunca terminar con una traza.
6. **Pruebas** (en `ec/edu/monster/pruebas/`) — el servicio con `ClienteApiFalso` (`Responder` por ruta: 200, `400`, `401` con `AUTH-002/003`, `404`, error de plataforma, cuerpo que no es JSON, servidor no disponible, coma enviada como punto, sin sesión no se llama a la API);
   los controladores con una vista falsa que implementa `IXxxVista`, sin tocar la consola. La **vista** no se prueba con xUnit: se verifica a mano.

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP y luego por el código propio.** Nunca `EnsureSuccessStatusCode()`: descartaría el JSON de error. `ServicioBase` ya lo cumple.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se imprime. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente. Va en `Authorization: Bearer` **por petición**, nunca en `DefaultRequestHeaders`.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se pregunta si continuar.
- Un solo `HttpClient` de larga vida (`ClienteApi.CrearHttpClient()`), no uno por petición.

## 5. Convenciones de consola

- **Codificación:** `Program.cs` fuerza UTF-8 en entrada y salida. Las tildes y la «ñ» salen bien solo si la terminal también está en UTF-8 (PowerShell y Windows Terminal suelen estarlo; `chcp 65001` en cmd).
- **Menús:** listas numeradas con `Elegir(titulo, opciones)`: devuelve el índice (base 0) o `-1` si se elige `0. Cancelar`. «0» es siempre cancelar / volver / salir / cerrar sesión (el controlador decide qué significa).
- **Errores:** se muestran con `MostrarError(...)` (prefijo `[Error] `) y solo con el mensaje. Una entrada inválida se vuelve a pedir; no se lanza excepción.
- **Datos pedidos:** `Pedir(etiqueta)` devuelve el texto ya recortado; validar lo trivial (vacío) en el controlador o el servicio antes de llamar al servidor.
- **Contraseñas:** `Pedir(...)` hace eco del texto (texto plano, decisión del equipo del proyecto de origen). Para ocultarla, leer con `Console.ReadKey(intercept: true)`; nunca imprimir ni registrar la contraseña.
- **Entrada cerrada:** `Pedir` lanza `EndOfStreamException` si la entrada se cierra (Ctrl+Z o `stdin` agotado), en lugar de devolver `""`. Sin eso un bucle de menú giraría para siempre. `Program.cs` la captura y termina.
- **Esperas:** la llamada al servidor bloquea la consola (hasta el timeout de 10 s); mostrar «Consultando…» antes de llamar si la operación puede tardar.
- **Salida:** presentar el resultado en pocas líneas, con el mismo formato y los mismos textos que los demás clientes del proyecto (escritorio, web, móvil).

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.UrlBasePorDefecto` (URL base con «/» final). Sin recompilar: variable de entorno `API_BASE_URL`
  (PowerShell: `$env:API_BASE_URL = "http://<ip>:57592/api/"; dotnet run`; Bash: `API_BASE_URL=http://… dotnet run`). La barra final se agrega sola si falta.
- **HTTP y HTTPS:** el esquema de la URL base lo decide todo; no hay binding. **HTTPS exige un certificado de servidor válido y de confianza** (en desarrollo, `dotnet dev-certs https --trust`, y que el nombre al que se conecta, `localhost` o la IP, esté en el certificado).
  La validación del certificado **no** se desactiva en el cliente. Si falla, el cliente solo muestra «Servidor no disponible, intente más tarde» (no distingue la causa; la excepción original queda en `InnerException`).
- **Cambiar de servidor** (por ejemplo, de .NET a Java) es cambiar la URL base, siempre que ambos cumplan `CONVENCION_REST.md`.
- **Entorno de desarrollo del proyecto de origen:** IIS Express por HTTP (`http://localhost:57592/api/`), también desde otros equipos de la red; los cambios de red necesarios (binding comodín, reserva de URL o Visual Studio como administrador, regla de firewall que aplique al perfil de red) y cómo retirarlos están en la documentación del servidor (`IIS_CONFIG.md` del proyecto).
  El perfil `http` (Kestrel) usa el puerto `5267`.
- **Visual Studio:** *Run* ejecuta el proyecto; la consola de VS es una terminal normal. Detener la ejecución antes de compilar desde la terminal (el `.dll` queda bloqueado).
- **Terminal:** `dotnet run` (o `dotnet bin/Debug/net10.0/<proyecto>.dll`).
- **Ejecutable único:** no incluido. `dotnet publish -c Release -r win-x64 --self-contained` lo genera; **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor)
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación (una ruta protegida sin token debe responder `401` con un JSON, lo que confirma que la API está arriba):
```
curl -i http://localhost:57592/api/<ruta-protegida>
curl -s -X POST http://localhost:57592/api/auth/login -H "Content-Type: application/json" -d '{"usuario":"…","contrasenia":"…"}'
```

Verificación con el programa en marcha (las vistas se prueban a mano; los controladores, con vistas falsas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio (`400`/`404`): se muestra el mensaje del servidor y se vuelve al menú sin cerrar la sesión.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde», se pregunta si continuar y el programa sigue respondiendo.
4. Token vencido (`AUTH-003`): vuelve al login con el mensaje.
5. Tildes y «ñ» en entrada y salida; entrada vacía y opción inválida en los menús; entrada cerrada (Ctrl+Z) termina sin traza.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicio`. Es la convención común de la familia (`CONVENCION_REST.md`):

- HTTP + JSON (`application/json; charset=utf-8`), verbos `GET` (consultas) y `POST` (login y operaciones). **La URL base** se recibe completa por configuración; no se asume puerto ni prefijo.
- Cada respuesta, de éxito o de error, es un objeto con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los errores de la plataforma (ruta inexistente, método no permitido, `Content-Type` incorrecto, error interno) traen **solo** esos tres campos.
- **Código HTTP + código propio:** `200` éxito; `400` datos inválidos; `401` credenciales (`AUTH-001`) o token (`AUTH-002` inválido / `AUTH-003` vencido); `404` recurso inexistente; `405`/`415` error de programación del cliente; `500` error del servidor.
- El token viaja en `Authorization: Bearer <token>`, no como parámetro ni cuerpo. Existe un login público (`POST /auth/login`) que devuelve `token`.
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Todos los datos viajan como **texto** (`valor`, `resultado`); los decimales con punto. Los campos que pueden faltar (`token`, `resultado`, listas) llegan `null` o ausentes en un error: no darlos por existentes.
- Los nombres de rutas, campos y códigos son fijos; si el servidor los cambia hay que actualizar `api`/`dto`/`servicio`.
- Si el servidor usa otro esquema de autenticación (cookies, OAuth/JWT con renovación), ampliar `ClienteApi`/`ServicioBase`.
- Para un servicio **SOAP** usar la plantilla SOAP, no esta.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «Servidor no disponible, intente más tarde» | Servidor apagado, URL base distinta de la real, firewall, o (HTTPS) certificado sin confiar/vencido/de otro host. Probar la URL con curl; ver la `InnerException` |
| Por HTTPS falla y por HTTP funciona | Certificado de desarrollo sin confiar: `dotnet dev-certs https --trust`; consultar por el mismo nombre que figura en el certificado |
| Otro equipo recibe timeout y desde el PC del servidor con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín |
| Las rutas dan `404` o se concatenan mal | La URL base no termina en «/» (`ConfiguracionApi` lo corrige) o la ruta lleva barra inicial: usar `"auth/login"`, no `"/auth/login"` |
| Campos `null` en una respuesta correcta | `[JsonPropertyName]` distinto del nombre del contrato, o el servidor cambió el campo |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `415` / `ERR-415` | El cuerpo no se envió como JSON: usar `ClienteApi` (no armar `HttpRequestMessage` en otra clase) |
| Muestra «El servidor respondió con un error inesperado (HTTP nnn)» | El cuerpo del error no era JSON (página de IIS Express, proxy): revisar la URL y el servidor |
| `MSB3027`/`MSB3021`: no se puede copiar el `.dll` | Hay una instancia del cliente en ejecución: detenerla antes de compilar |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 o se quitó la configuración de `Program.cs` |
| El menú repite «Opción inválida» sin parar | No debería ocurrir: `Pedir` lanza `EndOfStreamException` al cerrarse la entrada. Si se cambia `VistaBase`, conservarlo |
