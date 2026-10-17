# Plantilla: cliente de consola .NET para servicios SOAP

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga un servicio **SOAP** con el esquema de
respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, los tipos comunes, la base de los servicios y la
entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye proxy generado, pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

- **Lenguaje:** C# 14, `net10.0` (`Nullable` e `ImplicitUsings` habilitados). **Interfaz:** texto por la consola (`Console`), sin librerías de terceros. **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** WCF cliente (`System.ServiceModel.Http` y `Primitives` `10.0.652802`) con un **proxy generado desde el WSDL** por `dotnet-svcutil` (§4, paso 1). No se escribe el contrato a mano.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla. Con un proxy generado, el generador cumple por sí mismo R3 y R4.
- **Verificada:** `dotnet test` → 25 pruebas xUnit en verde (URL, binding http/https, traducción de fallo de red a `RED` con un contrato de ejemplo, `ServicioBase`).
  También se comprobó que `ClienteWS.LlamarAsync` compila y traduce el fallo de red usando el `Reference.cs` generado de un proyecto real.
  **No se ha ejecutado:** la plantilla contra un servidor real (no hay proxy ni dominio) ni la secuencia de pantallas (la plantilla no trae ninguna). La receta de §4 es la que se siguió y verificó en el cliente de consola del conversor de unidades.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo, **sin `bin/`, `obj/` ni `.vs/`**. Usar una **ruta corta**: `dotnet-svcutil` falla con rutas muy largas (§9).
2. **Renombrar** `soap_dotnet_consola.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`. El espacio de nombres **`ec.edu.monster`** es la convención y **no se cambia**.
3. **Abrir el `.slnx`** en Visual Studio 2026 (o usar `dotnet` desde la carpeta). Requiere el SDK de .NET 10; la primera vez restaura los paquetes (Internet).
4. **Configurar el servidor:** en `ec/edu/monster/ws/ConfiguracionServicio.cs`, `UrlPorDefecto` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor).
5. **Generar el proxy** desde el WSDL del servidor (§4, paso 1).
6. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
7. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar: `dotnet run`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Transporte SOAP** | `ws/ClienteWS` (binding por esquema http/https, timeouts, `LlamarAsync` con traducción de fallos de red) | No |
| **URL del servicio** | `ws/ConfiguracionServicio` (`UrlPorDefecto`, variable `WS_URL`, normalización) | `UrlPorDefecto` siempre; el resto no |
| **Proxy generado** | — (`ws/Reference.cs`, generado por `dotnet-svcutil` desde `wsdl/`) | **Se genera** por proyecto; **no se edita** |
| **Fábrica del cliente del dominio** | — (`ws/ClienteXxx`, implementa una interfaz `IClienteXxx` sobre el proxy) | **Se crea** (receta §4) |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`ExigeLogin`, `ServidorNoDisponible`) | No |
| **Base de servicios** | `servicio/ServicioBase` (`ExigirToken`, `Verificar`, `ExigirRespuesta`, `NormalizarValor`, `HaySesion`, `CerrarSesion`) | No; los servicios del dominio la extienden |
| **Entrada/salida** | `vista/VistaBase` (`Pedir`, `Elegir`, `Mostrar`, `MostrarError`, `Titulo`) | No; las vistas del dominio la extienden |
| **Composición** | `Program.cs` (UTF-8, manejo de entrada cerrada y de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas** | `servicio/`, `controlador/` (vacía, `.gitkeep`), `vista/` | **Se crean** (receta §4) |
| **Pruebas** | `pruebas/` (URL, binding, transporte, `ServicioBase`) | Se conservan; se agregan las del dominio |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" ec Program.cs`).

## 3. Estructura

```
Program.cs                         punto de composición (Sesion, cliente, servicio, vistas, controladores) y bucle principal
wsdl/                              copia del WSDL del servidor (fuente de verdad del contrato); versionada
ec/edu/monster/                    todos los directorios en minúscula; el espacio de nombres coincide con la ruta
├── modelo/        Sesion                                  entidades: solo propiedades, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; ExigeLogin, ServidorNoDisponible
├── ws/            ClienteWS, ConfiguracionServicio (+ Reference.cs generado, IClienteXxx y ClienteXxx del dominio)
├── servicio/      ServicioBase (+ los servicios del dominio)    única capa que conoce los tipos generados
├── controlador/   (vacía)                                 coordinan vista y servicio; devuelven un Resultado
├── vista/         VistaBase (+ las del dominio y sus interfaces IXxxVista)    única capa que lee el teclado y escribe en la consola
└── pruebas/       xUnit en el mismo proyecto, sin servidor externo
```

Dependencias permitidas: `vista ← controlador → servicio → ws`; `modelo` y `excepciones` no dependen de nadie.
**Los tipos generados (el «Dto» de la familia) viven en `ws/Reference.cs` y no salen de `servicio/`**; no hay carpeta `dto` ni clases `Dto` a mano.
La vista nunca importa `ws`: recibe y devuelve texto y tipos simples (o `modelo`). Si el WSDL cambia solo se tocan `ws` y `servicio`.
**Solo la vista usa `Console`** para leer y escribir; ni el controlador ni el servicio imprimen ni leen.

> **Las pruebas viven en este mismo proyecto**, en el espacio de nombres `ec.edu.monster.pruebas` (`GenerateProgramFile=false` porque el punto de entrada es `Program.cs`). Detener cualquier instancia en ejecución antes de `dotnet test`.

## 4. Receta: agregar una operación SOAP y su pantalla de consola

Ejemplo: operación `obtenerItems(token)` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba.

1. **Proxy generado** — guardar el WSDL y generar:
   ```
   curl -o wsdl/Servicio.wsdl "<URL del servicio>?singleWsdl"      # servidor .NET: un solo archivo con los esquemas
   dotnet tool install dotnet-svcutil --tool-path <carpeta corta>\herr
   dotnet-svcutil wsdl/Servicio.wsdl -n "*,ec.edu.monster.ws" -d ec/edu/monster/ws -o Reference.cs -nl
   ```
   - Un servidor **Java** no entrega `?singleWsdl`: guardar `?wsdl` **y** el XSD que importa (`schemaLocation`) y cambiar el `schemaLocation` a un nombre relativo.
   - La herramienta modifica el `.csproj` (añade `System.ServiceModel.*` `4.10.*`): **dejar solo** `System.ServiceModel.Http` y `Primitives` `10.0.652802`.
   - Generar desde una copia en una ruta corta si la del repositorio es larga (§9); puede hacer falta `DOTNET_ROLL_FORWARD=Major`. Conservar `dotnet-svcutil.params.json` (documenta cómo regenerar) y **no editar** `Reference.cs`.
   - El proxy trae `RespuestaBase` (`estado`, `codigo`, `mensaje`), los tipos de cada operación, la interfaz del contrato y `ServicioXxxClient` (derivada de `ClientBase<T>`).
2. **Interfaz y fábrica del cliente** — en `ws/`, una interfaz `IClienteXxx` con las operaciones que usa el cliente (permite el doble de pruebas) y `ClienteXxx` que la implementa sobre el proxy:
   ```csharp
   public class ClienteXxx : IClienteXxx
   {
       private readonly string _url = ConfiguracionServicio.ObtenerUrl();

       public Task<RespuestaLista> ObtenerItemsAsync(string token) =>
           ClienteWS.LlamarAsync(CrearCliente, c => c.obtenerItemsAsync(token));

       private ServicioXxxClient CrearCliente() =>
           new(ClienteWS.CrearBinding(_url), new EndpointAddress(_url));   // URL completa; no usar la del código generado
   }
   ```
   Un cliente por llamada (un cliente en estado *Faulted* no se reutiliza); `LlamarAsync` lo cierra, o hace `Abort()` y convierte el fallo de red en `ExcepcionServicio` con código `RED`.
3. **Servicio** — `servicio/ServicioXxx : ServicioBase` (recibe `IClienteXxx` y `Sesion`):
   ```csharp
   public async Task<IReadOnlyList<string>> ObtenerItemsAsync()
   {
       var token = ExigirToken();
       var r = ExigirRespuesta(await _cliente.ObtenerItemsAsync(token));
       Verificar(r.estado, r.codigo, r.mensaje);
       return r.elementos?.ToList() ?? [];     // las listas pueden venir ausentes
   }
   ```
   - `ExigirToken()` lanza `AUTH-002` sin llamar al servidor si la `Sesion` no tiene token.
   - `Verificar(...)` deja pasar si `estado` es `OK`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor y descarta el token si `ExigeLogin`.
   - Aquí van las reglas **del cliente** (campo vacío, formato, `NormalizarValor`: coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
   - Es la **única** capa que ve los tipos generados: traduce a `modelo` (`record`) o a tipos simples.
4. **Vista** — `vista/XxxVista : VistaBase` (con una interfaz `IXxxVista` para probar el controlador): pide datos con `Pedir(...)` / `Elegir(...)`, muestra con `Mostrar(...)` y `MostrarError(...)`. Sin lógica de negocio ni llamadas al servidor.
5. **Controlador** — `controlador/XxxControlador`: ejecuta **una sola vuelta**, pide a la vista, llama al servicio y muestra el resultado o `ex.Message` (solo el mensaje, nunca el código). Devuelve un `enum Resultado` (p. ej. `Continuar`, `SesionExpirada`, `Salir`).
   Si `ex.ExigeLogin` → `SesionExpirada`. Si `ex.ServidorNoDisponible` se muestra el mensaje y se conservan los datos ya capturados (no se cierra el programa).
6. **Composición** — en `Program.cs`: crear la `Sesion` (**una sola** para toda la ejecución), el cliente, los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Capturar `EndOfStreamException` (entrada cerrada) y cualquier excepción no prevista (mensaje por `Console.Error`), nunca terminar con una traza.
7. **Pruebas** (en `ec/edu/monster/pruebas/`) — un doble escrito a mano de `IClienteXxx` que devuelve respuestas simuladas, para el servicio (OK, error de negocio, red, sesión vencida, coma enviada como punto, sin sesión no se consulta nada);
   los controladores se prueban con una vista falsa que implementa `IXxxVista`, sin tocar la consola. La **vista** no se prueba con xUnit: se verifica a mano.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se imprime. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (`ServicioBase.NormalizarValor`, o `CultureInfo.InvariantCulture`, nunca `double.Parse` con la cultura actual).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se vuelve al menú.

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

- **Servidor por defecto:** `ConfiguracionServicio.UrlPorDefecto` (URL completa). Sin recompilar: variable de entorno `WS_URL`
  (PowerShell: `$env:WS_URL = "http://192.168.1.10:57592/Ruta"; dotnet run`; Bash: `WS_URL=http://… dotnet run`).
- **HTTP y HTTPS:** el esquema de la URL decide el modo de seguridad del binding (`ClienteWS.CrearBinding`): `http://` sin seguridad, `https://` con seguridad de transporte. **HTTPS exige un certificado de servidor válido y de confianza** (en desarrollo, `dotnet dev-certs https --trust`, y que el nombre al que se conecta, `localhost` o la IP, esté en el certificado).
  La validación del certificado **no** se desactiva en el cliente. Si falla, el cliente solo muestra «No se pudo comunicar con el servidor» (no distingue la causa).
- **El WSDL no se usa en ejecución:** un cliente WCF no lo descarga; usa el binding y la URL indicados. Con el servidor apagado el proxy se crea igual y la llamada falla con `RED`.
- **Entorno de desarrollo del proyecto de origen:** IIS Express por HTTP (`http://localhost:57592/<Ruta>`), también desde otros equipos de la red; los cambios de red necesarios (binding comodín, reserva de URL o Visual Studio como administrador, regla de firewall que aplique al perfil de red) y cómo retirarlos están en la documentación del servidor (`IIS_CONFIG.md` del proyecto, o el README de `soap_server_dotnet`).
  El perfil `http` (Kestrel) usa el puerto `5267`.
- **Visual Studio:** *Run* ejecuta el proyecto; la consola de VS es una terminal normal. Detener la ejecución antes de compilar desde la terminal (el `.dll` queda bloqueado).
- **Terminal:** `dotnet run` (o `dotnet bin/Debug/net10.0/<proyecto>.dll`).
- **Ejecutable único:** no incluido. `dotnet publish -c Release -r win-x64 --self-contained` lo genera; **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
dotnet test                  # pruebas locales (sin servidor)
dotnet run                   # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con el programa en marcha (las vistas se prueban a mano; los controladores, con vistas falsas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor y se vuelve al menú.
3. Con el servidor apagado: aparece «No se pudo comunicar con el servidor» y el programa sigue respondiendo.
4. Token vencido: vuelve al login con el mensaje.
5. Tildes y «ñ» en entrada y salida; entrada vacía y opción inválida en los menús; entrada cerrada (Ctrl+Z) termina sin traza.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws` y `servicio`. Es la convención común de la familia (`CONVENCION_SOAP.md`):

- SOAP **1.1**, *document/literal wrapped*, `BasicHttpBinding`. Elementos **calificados** con el namespace del servicio y `SOAPAction` = `TNS + Servicio + "/" + operación`: con el proxy generado los cumple el generador (R3, R4); un servidor .NET rechaza la acción vacía con HTTP 500.
- **El cliente no depende del nombre del puerto ni del binding** (R10): usa el proxy con una URL explícita. **La URL completa** del servicio se recibe por configuración; no se asume puerto ni «base + servicio» (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Éxito y error usan el mismo tipo.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R6); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R7); los nombres de operaciones, parámetros y campos son fijos (R5/R8).
  Si el servidor cambia su contrato hay que **regenerar el proxy** y revisar `servicio`.
- Los campos que pueden faltar (`token`, `resultado`, listas) llegan `null` en un error: no darlos por existentes.
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.ExigeLogin`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (comportamientos del cliente WCF).
- Para un servicio **REST** usar una plantilla REST, no esta.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «No se pudo comunicar con el servidor» | Servidor apagado, URL distinta de la real, firewall, o (HTTPS) certificado sin confiar/vencido/de otro host. Abrir `<URL>?wsdl` en el navegador |
| Por HTTPS falla y por HTTP funciona | Certificado de desarrollo sin confiar: `dotnet dev-certs https --trust`; consultar por el mismo nombre que figura en el certificado |
| Otro equipo recibe timeout y desde el PC del servidor con la IP funciona | La red está en categoría Pública y la regla de firewall solo vale para el perfil Privado (o el router aísla a los clientes) |
| `400 Invalid Hostname` | IIS Express solo atiende `localhost`: falta el binding comodín |
| Respuesta con campos vacíos / `null` | El servidor no califica los campos (R3) o el proxy está desactualizado: regenerar desde el WSDL actual |
| HTTP 500 *ActionNotSupported* | Se envía `SOAPAction` vacía: el proxy no es el generado o se alteró el atributo `Action` |
| El servidor responde `DATA-001` con un número | Se envió coma decimal: usar `ServicioBase.NormalizarValor` |
| `dotnet-svcutil` falla (`MSB3030`, ruta demasiado larga, o pide otro runtime) | Generar en una copia con ruta corta (p. ej. `C:\Temp\gen`) y copiar `Reference.cs`; definir `DOTNET_ROLL_FORWARD=Major` |
| Avisos `NU1902`/`NU1903` de `System.Security.Cryptography.*` | Dependencia transitiva de los paquetes WCF; no impide funcionar. Revisar si hay una versión corregida |
| `MSB3027`/`MSB3021`: no se puede copiar el `.dll` | Hay una instancia del cliente en ejecución: detenerla antes de compilar |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 o se quitó la configuración de `Program.cs` |
| El menú repite «Opción inválida» sin parar | No debería ocurrir: `Pedir` lanza `EndOfStreamException` al cerrarse la entrada. Si se cambia `VistaBase`, conservarlo |
