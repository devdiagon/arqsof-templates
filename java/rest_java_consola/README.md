# Plantilla: cliente de consola Java para APIs REST

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, el armado y la lectura de las peticiones, los tipos comunes, la verificación de la envoltura
y la entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java 21. **Interfaz:** texto por la consola (`Scanner` / `System.out`). **Arquitectura:** capas, sin frameworks.
- **Transporte:** `java.net.http.HttpClient` (JDK, sin dependencia), con un solo cliente compartido y tiempo límite de 10 s. **JSON:** Gson 2.11.0.
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`): URL base completa con prefijo y «/» final (R1), todo dato como texto (R9), rutas con variables codificadas (R10). El contrato concreto del servidor es su tabla de endpoints (`ENDPOINTS_REST.md`), que el proyecto copia en `docs/`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su propio login y su propia sesión (en `modelo`); la plantilla no trae ninguno (§8).
- **Verificada:** `mvn test` → BUILD SUCCESS, 17 pruebas JUnit en verde, sin servidor: 3 de configuración de la URL (`ConfiguracionApi`), 9 de armado y lectura (`TraductorApi`: ruta, `Basic`, cuerpo, envoltura con éxito y con HTTP de error, `SRV-000`, `RED`) y 5 de la verificación de la envoltura (`ServicioBase`).
  **No se ha ejecutado:** el cliente contra un servidor real, ninguna petición HTTP real (`TransporteHttp`) ni la secuencia de pantallas (la plantilla no trae ninguna). El jar `-shaded` (`mvn package`) se probó con `java -jar`: arranca e imprime la URL configurada.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId`; opcionalmente agregar `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**: es la raíz y cada capa es un paquete directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.api`…), sin paquete intermedio de módulo.
4. **Configurar el servidor:** en `api/ConfiguracionApi.java`, `URL_BASE_POR_DEFECTO` = **URL base completa de la API** (host, puerto, contexto y prefijo, **con «/» final**), tal como la entrega la tabla de endpoints del servidor. Guardar una copia de esa tabla en `docs/` como contrato.
5. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
6. **Probar el servidor** (curl o Postman, ver §7) y ejecutar: `mvn compile exec:java`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración de la URL** | `api/ConfiguracionApi` (`urlBase()`: propiedad `api.base.url`, variable `API_BASE_URL` o la constante) | `URL_BASE_POR_DEFECTO` siempre; el resto no |
| **Transporte** | `api/Transporte` (interfaz de un método), `api/TransporteHttp` (única clase que abre conexiones), `api/RespuestaHttp` (código y cuerpo) | No |
| **Armado y lectura** | `api/TraductorApi`: ruta codificada, cuerpo JSON, cabecera `Basic`, lectura de la envoltura; traduce los fallos a `RED` y `SRV-000`. No toca la red | No |
| **Interfaz e implementación del dominio** | — (`api/XCliente.java` y `api/XClienteApi.java`) | **Se crean** por proyecto, una firma por fila de la tabla de endpoints (§4, pasos 2 y 3) |
| **DTO base** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`, `esOk()`) | No; se agregan DTO del dominio al lado |
| **Verificación de la envoltura** | `servicios/ServicioBase` (`verificar`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (código + mensaje; códigos propios `RED` y `SRV-000`) | No |
| **Entrada/salida** | `vista/VistaBase` (`pedir`, `elegir`, `pausar`, `mostrarTitulo`, `mostrarPortada`, `mostrar`, `mostrarError`) y `vista/Textos` | `VistaBase` no; las vistas del dominio la extienden y `Textos` crece con los textos del dominio (la portada se cambia por la del proyecto) |
| **Composición** | `Main` (UTF-8 en la salida, manejo de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (cliente de la API, servicios, vistas, controladores) y bucle principal
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; servidorNoDisponible(), respuestaIlegible()
├── dto/           RespuestaBaseDto (+ los del dominio)    POJOs sin anotaciones para Gson, todos los campos como texto: lo que ve el resto del cliente
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── api/           ConfiguracionApi, Transporte, TransporteHttp, RespuestaHttp, TraductorApi (+ XCliente y XClienteApi del dominio)
├── controlador/                                           coordinan vista y servicio; devuelven un Resultado
└── vista/         VistaBase, Textos (+ las del dominio)   única capa que lee el teclado y escribe en la consola
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor), misma estructura
docs/                                  documentos de referencia: CONVENCION_REST.md y CONVENCION_PRUEBAS.md (copias vigentes) y PROJECT.md (esqueleto del proyecto)
README.md                              esta guía: el único documento que queda en la raíz
```

Dependencias permitidas: `vista → controlador → servicios → api`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca importa `api` ni `dto`**: recibe y devuelve texto y tipos simples; solo conoce a su controlador y a `Textos`.
**`servicios` solo conoce la interfaz del cliente (`XCliente`), no su implementación**; solo la implementación de `api` conoce el protocolo, HTTP, JSON y las excepciones de red.
Si el contrato cambia solo se tocan `api`, `dto` y `servicios`. **Solo la vista usa `System.in`/`System.out`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación REST y su pantalla de consola

Ejemplo: `GET items` que devuelve una lista y `POST items/{codigo}/pedidos` con un cuerpo. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **DTO propio** — clase `…Dto` en el paquete `dto` que extienda `RespuestaBaseDto`, como **POJO sin anotaciones**: los nombres de campo son los del JSON del servidor (Gson los mapea por nombre) y todos son texto.
   ```java
   public class RespuestaListaDto extends RespuestaBaseDto {
       protected List<String> elementos;
       public List<String> getElementos() { if (elementos == null) { elementos = new ArrayList<>(); } return elementos; }   // nunca nulo
   }
   ```
2. **Interfaz del cliente** — `api/XCliente.java`: **un método por fila de la tabla de endpoints, con el nombre de la operación SOAP equivalente** si existe y tipos de retorno = DTO propios. Las credenciales, si el servidor las pide en cada operación, son **parámetros** del método (como en SOAP): así `servicios` no cambia entre protocolos.
   ```java
   public interface XCliente {
       RespuestaListaDto obtenerItems(String usuario, String clave);
       RespuestaPedidoDto realizarPedido(String codigo, String cantidad, String usuario, String clave);
   }
   ```
3. **Implementación** — `api/XClienteApi.java`, con el `Transporte` por constructor y una fábrica `crear()` que usa el real. Cada método es una llamada a `TraductorApi.llamar`; **aquí, y solo aquí**, se decide verbo, ruta, cuerpo y cabecera:
   ```java
   public final class XClienteApi implements XCliente {
       private final Transporte transporte;
       public XClienteApi(Transporte transporte) { this.transporte = transporte; }
       public static XCliente crear() { return new XClienteApi(TransporteHttp.crear()); }

       @Override public RespuestaListaDto obtenerItems(String usuario, String clave) {
           return TraductorApi.llamar(transporte, "GET", "items", null,
                   TraductorApi.autorizacionBasic(usuario, clave), RespuestaListaDto.class);
       }
       @Override public RespuestaPedidoDto realizarPedido(String codigo, String cantidad, String usuario, String clave) {
           return TraductorApi.llamar(transporte, "POST", "items/" + TraductorApi.codificar(codigo) + "/pedidos",
                   TraductorApi.cuerpo("cantidad", cantidad), TraductorApi.autorizacionBasic(usuario, clave),
                   RespuestaPedidoDto.class);
       }
   }
   ```
   El ejemplo usa `Authorization: Basic` porque es lo que hace `TraductorApi.autorizacionBasic`; si el servidor usa otro esquema, el valor de la cabecera se arma aquí (el transporte recibe el valor completo, o `null`). Las credenciales **nunca** van en la ruta ni se registran.
   `TraductorApi.llamar` ya cumple lo que pide la arquitectura: lee la envoltura **también con HTTP de error** (nunca descarta el cuerpo por el código), `IOException` → `RED`, cuerpo vacío o que no es JSON o sin `estado` → `SRV-000` (con el código HTTP en el mensaje).
4. **Servicio** — `servicios/XServicio extends ServicioBase`:
   ```java
   public List<String> items() {
       return List.copyOf(verificar(cliente.obtenerItems(usuario, clave)).getElementos());
   }
   ```
   - `verificar(...)` devuelve la respuesta si `esOk()`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`.
   - **No captura excepciones de red**: ya llegan traducidas a `RED` desde `api`.
   - Aquí van las reglas **del cliente**: validar lo trivial (campo vacío), convertir los datos de pantalla al formato del contrato (texto, decimales con **punto**, fechas ISO-8601, cultura invariante). Las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
5. **Vista** — `vista/XVista extends VistaBase`: pide datos con `pedir(...)` / `elegir(...)`, muestra con `mostrar(...)` y `mostrarError(...)`, con los textos de `Textos`. Sin lógica de negocio ni llamadas al servidor.
6. **Controlador** — `controlador/XControlador`: valida campos vacíos **antes** de llamar (si faltan, no se llama al servicio), pide a la vista, llama al servicio, muestra el resultado o `e.getMessage()` (solo el mensaje, nunca el código) y devuelve un `enum Resultado` (p. ej. `SEGUIR`, `IR_AL_LOGIN`, `SALIR`).
   Si `e.servidorNoDisponible()` se muestra el mensaje y no se cierra el programa. Cualquier otra excepción inesperada se muestra con el texto genérico de `Textos`.
7. **Composición** — en `Main`: crear el cliente (`XClienteApi.crear()`), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Todo `RuntimeException` no previsto se captura en el bucle (mensaje por `System.err`), nunca termina con una traza.
8. **Pruebas** — `XCliente` es una interfaz: sirve un doble (lambda o `java.lang.reflect.Proxy`) para el servicio (OK, error de negocio, red, respuesta nula, coma enviada como punto); para `XClienteApi`, un `Transporte` falso (una lambda) que registra verbo, ruta, cuerpo y cabecera y devuelve una `RespuestaHttp`
   (ver `TraductorApiTest`: no hace falta red ni servidor). Los controladores se prueban con una vista falsa (subclase de `VistaBase` que devuelve respuestas fijas), sin tocar la consola.
   El contrato **no** se prueba con código ni se levanta un servidor HTTP: se verifica a mano con la lista de `docs/CONVENCION_PRUEBAS.md` §6.3.
   Mockito no está en el `pom.xml`: la arquitectura lo prevé, pero se agrega solo cuando una prueba lo necesite.

Reglas que se repiten en todo dominio:
- **La envoltura decide, no el código HTTP:** el cliente lee `estado`/`codigo`/`mensaje` del cuerpo también cuando el HTTP es 4xx o 5xx; el HTTP de error solo clasifica. Solo la falta de respuesta es `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se vuelve al menú.

## 5. Convenciones de consola

- **Codificación:** `Main` fuerza UTF-8 en `System.out`/`System.err` y `VistaBase` lee el teclado como UTF-8. Las tildes y la «ñ» salen bien solo si la terminal también está en UTF-8 (`chcp 65001` en cmd; PowerShell y Windows Terminal suelen estarlo).
- **Menús:** listas numeradas con `elegir(titulo, opciones)` (o `elegir(titulo, opciones, textoCero)` para rotular el 0, p. ej. «Salir»): devuelve el índice (base 0) o `-1` si se elige `0`. «0» es siempre cancelar / volver / salir.
- **Portada:** `Textos.PORTADA` guarda el arte ASCII de arranque; el proyecto la cambia por la suya. `Main` llama una vez a `mostrarPortada()` de cualquier vista antes del bucle.
- **Errores:** se muestran con `mostrarError(...)` (prefijo `[!] `) y solo con el mensaje. Una entrada inválida se vuelve a pedir; no se lanza excepción.
- **Datos pedidos:** `pedir(etiqueta)` devuelve el texto ya recortado (`trim`); validar lo trivial (vacío, número) en el servicio o el controlador antes de llamar al servidor.
- **Claves:** `pedir(...)` hace eco del texto. `System.console().readPassword()` las oculta, pero solo existe en una terminal real (no en el IDE); si un proyecto decide mostrarlas en texto plano, lo registra en su `docs/PROJECT.md`. Nunca se imprimen ni se registran de otra forma.
- **Entrada cerrada:** `pedir` y `pausar` lanzan `NoSuchElementException` al terminar la entrada (Ctrl+Z / Ctrl+D o `stdin` agotado), así un bucle de menú no gira sin fin. `Main` la captura y termina el programa en silencio.
- **Esperas:** la llamada al servidor bloquea la consola (hasta el tiempo límite de 10 s); mostrar «Consultando…» antes de llamar si la operación puede tardar.
- **Salida:** presentar el resultado en pocas líneas, con el mismo formato y los mismos textos que los demás clientes del proyecto (escritorio, web, móvil), tomados de `Textos`.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.URL_BASE_POR_DEFECTO` (URL base completa, con «/» final). Sin recompilar: `-Dapi.base.url=http://host:8080/Contexto/api` o la variable de entorno `API_BASE_URL` (se normaliza: sin espacios y con una sola «/» final).
- `Main` no comprueba la conexión al arrancar: con el servidor apagado el programa abre y el error aparece en la primera operación («Servidor no disponible, intente más tarde»).
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`). La consola de NetBeans no es una terminal: no hay `System.console()`.
- **Terminal:** `mvn compile exec:java` (`exec:java` no compila); con otro servidor: `mvn compile exec:java -Dapi.base.url=http://192.168.1.10:8080/Contexto/api`. El `exec-maven-plugin` se **mantiene declarado** en el `pom.xml`: es la forma sencilla de ejecutar el cliente desde una terminal.
- **Jar ejecutable:** `mvn package` genera `target/<artifactId>-<version>-shaded.jar` (`maven-shade-plugin`, incluye Gson): `java -jar target/…-shaded.jar`. El `dependency-reduced-pom.xml` que genera Maven en la raíz es un artefacto: no se versiona. **Probarlo** antes de darlo por bueno.
- **Pruebas manuales** (`ec.edu.monster.pruebas`, si el proyecto las tiene): las clases que usan `api` necesitan Gson en el *classpath* (con *Run File* del IDE o `mvn exec:java` se resuelve; con `java -cp` hay que añadirlo).

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor)
mvn compile exec:java        # ejecutar contra el servidor configurado
mvn package                  # pruebas + jar normal y jar -shaded
```
Probar el servidor sin la aplicación: `curl -i -X POST <BASE>auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"clave\":\"...\"}"` (la ruta y el cuerpo son los de la tabla de endpoints del proyecto).

Verificación con el programa en marcha (las vistas y los controladores se prueban a mano o con vistas falsas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor (llegue con el HTTP que llegue) y se vuelve al menú.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y el programa sigue respondiendo.
4. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.
5. Tildes y «ñ» en entrada y salida (también en segmentos de ruta); entrada vacía y opción inválida en los menús.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- **URL base completa** con prefijo común y «/» final (R1); las rutas de las operaciones son relativas y sin barra inicial; los segmentos variables van codificados (R10).
- Toda respuesta (también las de error) trae `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación; los errores usan códigos HTTP 4xx/5xx por categoría (R5 a R7). En un error los campos propios se omiten y las listas llegan vacías.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R9), los nombres de campos son fijos y en `camelCase` (R4) y los errores de plataforma (ruta, verbo, `Content-Type`, JSON mal formado, error interno) también traen la envoltura (R6).
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (cabecera `Basic` por petición, token, credenciales en el cuerpo, nada) lo define cada servidor y su cliente lo implementa en `api` (la cabecera) y `servicios`/`modelo` (la sesión); se documenta en el `docs/PROJECT.md` del proyecto. `TraductorApi.autorizacionBasic` solo arma el valor `Basic`.
- Para un servicio **SOAP** usar la plantilla `soap_java_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «Servidor no disponible, intente más tarde» | Servidor apagado, API no desplegada o `URL_BASE_POR_DEFECTO` distinta de la real (contexto + prefijo). Probar la URL con `curl` |
| «No se pudo interpretar la respuesta del servidor (HTTP 404)» | Ruta mal escrita o prefijo de la API ausente en la URL base: el servidor respondió con una página que no es JSON. Probar la ruta con `curl` |
| Respuesta con campos vacíos | El nombre de un campo del DTO no coincide con el del JSON (Gson no avisa: lo deja `null`); los nombres son el contrato con el servidor |
| `IllegalArgumentException` al armar la URI / «URL no válida» | Segmento de ruta sin codificar (espacios, tildes): usar `TraductorApi.codificar`; o `URL_BASE_POR_DEFECTO` mal escrita |
| El servidor rechaza la petición por `Content-Type` o por JSON mal formado | Cuerpo armado a mano: usar `TraductorApi.cuerpo` (genera un objeto JSON con campos de texto) |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó el `PrintStream` UTF-8 de `Main` |
| El menú repite «Opción inválida» sin parar | Ya no debería ocurrir: al cerrarse la entrada el programa termina (ver §5, «Entrada cerrada») |
| `System.console()` es `null` | Se ejecuta desde el IDE o con la entrada redirigida: usar `pedir(...)` como alternativa |
| `ClassNotFoundException: com.google.gson…` al ejecutar a mano con `java -cp` | Falta Gson en el *classpath*: añadir el jar o ejecutar con `mvn exec:java` / el jar `-shaded` |
