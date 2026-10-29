# Plantilla: cliente de escritorio Java (Swing) para APIs REST

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos. Trae el transporte, el armado y la lectura de las peticiones, los tipos comunes, la verificación de la envoltura,
la ventana principal con sus estilos y la ejecución asíncrona, y deja marcado lo que cambia con el **dominio**.
No incluye inicio de sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1) y `ESPECIFICACION_CLIENTES.md`. Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java 21. **Interfaz:** Swing + FlatLaf, vistas **escritas a código** (sin editor visual). **Arquitectura:** capas, sin frameworks.
- **Transporte:** `java.net.http.HttpClient` (JDK, sin dependencia), con un solo cliente compartido y tiempo límite de 10 s. **JSON:** Gson 2.11.0.
  `api/ConfiguracionApi` fija la URL base, `api/TransporteHttp` es la única clase que abre conexiones y `api/TraductorApi` arma las peticiones y lee las respuestas (sin red), traduciendo los fallos a `RED` y `SRV-000`.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida).
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`): URL base completa con prefijo y «/» final (R1), todo dato como texto (R9), rutas con variables codificadas (R10). El contrato concreto del servidor es su tabla de endpoints (`ENDPOINTS_REST.md`), que el proyecto copia en `docs/`. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su login y su sesión (en `modelo`); la plantilla no trae ninguno (§8). Deja el punto de enganche: `InicioVista` (pantalla inicial)
  y `LoginVista`, una pantalla de acceso **pasiva** (sin controlador) lista para usar.
- **Verificada:** `mvn test` → BUILD SUCCESS, 22 pruebas JUnit en verde, sin servidor ni pantalla (ejecutor asíncrono, molde de controlador, verificación de la envoltura, configuración de la URL y armado y lectura de las peticiones).
  **No se ha ejecutado:** la ventana real abierta en pantalla, el cliente contra un servidor real, ninguna petición HTTP real (`TransporteHttp`) ni el jar `-shaded` con `java -jar`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias.
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**; cada capa es un paquete directo bajo él.
4. **Configurar el servidor:** en `api/ConfiguracionApi.java`, `URL_BASE_POR_DEFECTO` = **URL base completa de la API** (host, puerto, contexto y prefijo, **con «/» final**), tal como la entrega la tabla de endpoints del servidor.
   Guardar una copia de esa tabla en `docs/` como contrato.
5. **Poner el nombre de la aplicación** en `vista/Textos.java` y, si se quiere, **sustituir la imagen** `src/main/resources/ec/edu/monster/estaticos/fondo_app.png`
   (requisitos en `docs/CLIENT_STYLES_JAVA.md` §6). La imagen es un **PNG** (admite transparencia); si se cambia por otra, con otro dibujo o proporciones, **se conserva como `fondo_app.png`** para no tocar el código ni los estilos. El tamaño con que se dibuja (60 % de su contenedor) se ajusta en `Tema.ESCALA_IMAGEN`.
6. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
7. **Probar el servidor** (curl o Postman, ver §7) y ejecutar: `mvn compile exec:java`.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Núcleo de servicios** | `servicios/ServicioBase` (verifica la envoltura: no OK → `ExcepcionServicio`; nula → `SRV-000`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (códigos propios `RED` y `SRV-000`) | No |
| **Configuración de la URL** | `api/ConfiguracionApi` (`urlBase()`: propiedad `api.base.url`, variable `API_BASE_URL` o la constante) | `URL_BASE_POR_DEFECTO` siempre; el resto no |
| **Transporte** | `api/Transporte` (interfaz de un método), `api/TransporteHttp` (única clase que abre conexiones), `api/RespuestaHttp` (código y cuerpo) | No |
| **Armado y lectura** | `api/TraductorApi`: ruta codificada, cuerpo JSON, cabecera `Basic`, lectura de la envoltura; traduce los fallos a `RED` y `SRV-000`. No toca la red | No |
| **Interfaz e implementación del dominio** | — (`api/XCliente.java` y `api/XClienteApi.java`) | **Se crean** por proyecto, una firma por fila de la tabla de endpoints (§4, pasos 2 y 3) |
| **DTO base** | `dto/RespuestaBaseDto` (POJO sin anotaciones) | Se agregan DTO del dominio al lado |
| **Asincronía y molde de controlador** | `controlador/EjecutorAsincrono`, `controlador/ControladorBase` (`ejecutar`: segundo plano, estado «ocupado», segunda acción ignorada, error como mensaje) | No; los controladores del dominio la extienden |
| **Composición y navegación** | `Main`, `vista/Pantalla`, `vista/VentanaPrincipal` | Se agregan pantallas, servicios y controladores del dominio |
| **Estilos** | `vista/estilo/*` (`Tema`, `EstilosUi`, `PanelMensaje`, `ZonaEstado`, `PanelRedondeado`, `PanelImagen`, `IndicadorProgreso`, `TarjetaResultado`, `TablaDatos`, `FlechaDireccion`), `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos** | `vista/Textos` | `NOMBRE_APLICACION` y los textos del dominio |
| **Base de las vistas** | `vista/VistaBase` (zona de estado, «ocupado» que deshabilita los controles, `Cancelar` con Esc, auxiliares de disposición) | No; las vistas del dominio la extienden |
| **Punto de enganche** | `vista/InicioVista` (pantalla inicial de ejemplo), `vista/LoginVista` (pasiva, sin controlador) | Se reemplazan/usan según el proyecto |
| **Dominio (vacío)** | `modelo/`, `controlador/` | Nuevos `dto/`, `servicios/`, `controlador/`, `vista/`, e interfaz `XCliente` e implementación `XClienteApi` en `api/` |

Las zonas a adaptar están marcadas con **`DOMINIO`** o **`TODO`** en el código (`grep -rn "DOMINIO\|TODO" src`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (cliente de la API, servicios, vistas, controladores) y navegación
├── modelo/                     datos propios del cliente (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio
├── dto/           RespuestaBaseDto                 POJOs sin anotaciones para Gson, todos los campos como texto
├── servicios/     ServicioBase                     única capa que conoce la interfaz del cliente y los dto
├── api/           ConfiguracionApi, Transporte, TransporteHttp, RespuestaHttp, TraductorApi     + XCliente / XClienteApi por proyecto
├── controlador/   EjecutorAsincrono, ControladorBase        + un controlador por pantalla
└── vista/         Pantalla, Textos, VentanaPrincipal (dos modos), VistaBase, InicioVista, LoginVista
    └── estilo/    Tema, EstilosUi, PanelRedondeado, PanelMensaje, ZonaEstado, PanelImagen, IndicadorProgreso, TarjetaResultado, TablaDatos, FlechaDireccion
src/main/resources/ec/edu/monster/estaticos/   fondo_app.png (imagen de ejemplo)
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor ni pantalla), misma estructura
docs/                                   CLIENT_STYLES_JAVA, CONVENCION_REST, CONVENCION_PRUEBAS y PROJECT (esqueleto del proyecto)
README.md                               esta guía: el único documento que queda en la raíz
```

Dependencias: `vista → controlador → servicios → api | dto`; `modelo`, `dto` y `excepciones` son transversales.
**La vista nunca importa `api` ni `dto`.** **`servicios` solo conoce la interfaz del cliente (`XCliente`), no su implementación**: solo `XClienteApi` conoce el protocolo, HTTP, JSON y las excepciones de red.

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET items` que devuelve una lista y `POST items/{codigo}/pedidos` con un cuerpo. Siempre en este orden, de abajo hacia arriba:

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
   El ejemplo usa `Authorization: Basic` porque es lo que hace `TraductorApi.autorizacionBasic`; si el servidor usa otro esquema, el valor de la cabecera se arma aquí. Las credenciales **nunca** van en la ruta ni se registran.
   `TraductorApi.llamar` lee la envoltura **también con HTTP de error** (nunca descarta el cuerpo por el código), `IOException` → `RED`, cuerpo vacío o que no es JSON o sin `estado` → `SRV-000`.
   Se crea en `Main`: `XClienteApi.crear()`.
4. **Servicio** — `servicios/XServicio extends ServicioBase`: valida lo trivial (campos vacíos), convierte los datos al formato del contrato (coma decimal → punto) y llama:
   `return verificar(cliente.obtenerItems(usuario, clave));`. Las reglas del dominio las decide el servidor; el cliente muestra su `mensaje`. No captura excepciones de red: ya llegan traducidas a `RED`.
5. **Vista** — `XxxVista extends VistaBase` **pasiva**, construida solo con `EstilosUi` y `Tema` (ver `InicioVista` y `LoginVista`): getters de lo escrito, métodos `alXxx(...)`, `marcarVacio(campo)` y, de `VistaBase`, la zona de estado, `setOcupado` y `alCancelar`. Los controles que se deshabilitan mientras espera se registran con `registrar(...)`.
6. **Controlador** — `extends ControladorBase`: registra los eventos de la vista, valida los campos vacíos en el orden de la pantalla y llama al servicio **con `ejecutar(textoOcupado, tarea, alExito)`**, que usa `EjecutorAsincrono` (nunca en el hilo de eventos), marca la pantalla «ocupada», ignora una segunda acción y muestra el error como mensaje. Un proyecto con login sobrescribe `manejar` para tratar el código de sesión no válida de su servidor.
   Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar campos vacíos antes de llamar; mostrar `error.getMessage()`, nunca el código; conservar los datos ante un error salvo los sensibles (claves);
   estado «ocupado» que deshabilita los controles e **ignora una nueva acción mientras dura**; no conocer la pantalla siguiente (la navegación llega como `Runnable`/`Consumer`).
7. **Pantalla** — constante en `vista/Pantalla` y, en `Main`: crear vista y controlador, `ventana.agregar(Pantalla.X, vista)` y conectar la navegación.
8. **Pruebas** — unitarias, sin servidor ni pantalla (`docs/CONVENCION_PRUEBAS.md`): `XCliente` es una interfaz, sirve un doble para el servicio (OK, error de negocio, red, respuesta nula, coma enviada como punto);
   para `XClienteApi`, un `Transporte` falso (una lambda) que registra verbo, ruta, cuerpo y cabecera y devuelve una `RespuestaHttp` (ver `TraductorApiTest`: no hace falta red ni servidor);
   los controladores, con un doble del servicio (`SwingUtilities.invokeAndWait` y un `esperar(...)`). El contrato **no** se prueba con código ni se levanta un servidor HTTP: se verifica a mano con la lista de `docs/CONVENCION_PRUEBAS.md` §6.3.

Reglas que se repiten en todo dominio:
- **La envoltura decide, no el código HTTP:** el cliente lee `estado`/`codigo`/`mensaje` del cuerpo también cuando el HTTP es 4xx o 5xx; el HTTP de error solo clasifica. Solo la falta de respuesta es `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Si el proyecto tiene login: la sesión vive solo en memoria (`modelo`), nunca en disco ni en logs; las credenciales se envían según el contrato del servidor.
- Todo dato viaja como **texto**; los decimales con **punto**. Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
- Mostrar solo el **mensaje**, nunca el código. Textos con tildes, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- **Nunca red en el hilo de eventos.** Los fallos de red no cierran la aplicación.
- `EstilosUi.instalarLookAndFeel()` **antes** de crear cualquier componente.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Las vistas **solo usan la fábrica** `EstilosUi` y las constantes de `Tema`; no colorean ni dimensionan componentes sueltos.

| Fábrica / clase | Para qué |
|---|---|
| `EstilosUi.botonPrimario` | Acción principal de la pantalla |
| `EstilosUi.botonSecundario` | Salida o regreso: «Cerrar sesión», «Cancelar» |
| `EstilosUi.encabezadoAplicacion` / `encabezadoPantalla` | Título de la aplicación (acceso) y de pantalla |
| `EstilosUi.etiqueta`, `campoTexto`, `campoContrasenia`, `selector`, `marcarError` | Etiqueta de campo, campos, lista desplegable, borde de error |
| `PanelMensaje` (`mostrarError`, `mostrarExito`, `mostrarResultado`) | Mensaje y resultado; oculto hasta que hay texto |
| `IndicadorProgreso` (`setActivo`) | Indicador de carga; deshabilitar los controles mientras dura |
| `ZonaEstado` | Un solo lugar que muestra, de una en una, el indicador de ocupado con su texto, un error o un éxito (la incluye `VistaBase`) |
| `EstilosUi.botonPrimarioGrande` | Botón de una cuadrícula de acciones (96 de alto) |
| `TarjetaResultado` | Resumen de un cambio de valor (título centrado, monto con signo, variación, pie); recibe los textos ya formateados |
| `TablaDatos` | Tabla con encabezado fijo y columnas numéricas a la derecha (`setDatos(columnas, alDerecha, pesos, filas)`) |
| `FlechaDireccion` | Flecha `→` de 24 × 24 dibujada en código (indica un sentido, p. ej. origen → destino) |

- **Disposición:** dos modos en `VentanaPrincipal`. El **acceso** (`Pantalla.LOGIN`) va dividido: pantalla a la izquierda (columna de 420) e imagen a la derecha. El **resto de la aplicación** usa el ancho completo y la imagen es el fondo de la ventana: completa, centrada, con opacidad (`Tema.OPACIDAD_IMAGEN`) y reescalable, con los paneles sobre ella (`PANEL_SOBRE_IMAGEN`); si no cabe en alto, se desplaza (`Pantalla` indica el modo y si el contenido va a todo el ancho). Tamaño inicial 1100 × 720, mínimo 900 × 640 (`Tema`).
- **Presentación clara:** cambiar los valores de `Tema` por los de `docs/CLIENT_STYLES_JAVA.md` §2.3 y usar `FlatLightLaf`.
- FlatLaf no admite algunas propiedades en ciertos componentes: no falla al compilar, puede fallar al construir la pantalla; probar cada pantalla nueva.
- Un texto con HTML (mensajes, tarjetas) usa `EstilosUi.anchoHtml(px)` para su ancho: Swing escala los px de HTML según la resolución de la pantalla y un ancho fijo cortaría el texto con escalas como 125 %.
- Antes de construir las pantallas de un proyecto conviene especificarlas en su `docs/PANTALLAS.md` (orden de los elementos, anchos, estados y decisiones).

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.URL_BASE_POR_DEFECTO` (URL base completa, con «/» final). Sin recompilar: `-Dapi.base.url=http://host:8080/Contexto/api` o la variable de entorno `API_BASE_URL` (se normaliza: sin espacios y con una sola «/» final).
- Crear el cliente no abre ninguna conexión: con el servidor apagado la ventana abre y el fallo llega al **llamar** una operación, donde `TraductorApi` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (`exec.mainClass`). **Terminal:** `mvn compile exec:java` (`exec:java` no compila); con otro servidor: `mvn compile exec:java -Dapi.base.url=http://192.168.1.10:8080/Contexto/api`.
- **Jar ejecutable:** `mvn package` genera `target/<artifactId>-<version>-shaded.jar` (`maven-shade-plugin`, incluye Gson y FlatLaf): `java -jar target/…-shaded.jar`. El `dependency-reduced-pom.xml` que genera Maven en la raíz es un artefacto: no se versiona. **Probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor ni pantalla)
mvn compile exec:java        # ejecutar
mvn package                  # pruebas + jar normal y jar -shaded
```
Probar el servidor sin la aplicación: `curl -i -X POST <BASE>auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"clave\":\"...\"}"` (la ruta y el cuerpo son los de la tabla de endpoints del proyecto).
Un agente que no pueda abrir la ventana debe decir qué verificó y que **no vio la ventana real**.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- **URL base completa** con prefijo común y «/» final (R1); las rutas de las operaciones son relativas y sin barra inicial; los segmentos variables van codificados (R10).
- Toda respuesta (también las de error) trae `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación; los errores usan códigos HTTP 4xx/5xx por categoría (R5 a R7). En un error los campos propios se omiten y las listas llegan vacías.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R9), los nombres de campos son fijos y en `camelCase` (R4) y los errores de plataforma (ruta, verbo, `Content-Type`, JSON mal formado, error interno) también traen la envoltura (R6).
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (cabecera `Basic` por petición, token, credenciales en el cuerpo, nada) lo define cada servidor y su cliente lo implementa en `api` (la cabecera) y `servicios`/`modelo` (la sesión); se documenta en el `docs/PROJECT.md` del proyecto. `TraductorApi.autorizacionBasic` solo arma el valor `Basic`.
- Para un servicio **SOAP** usar la plantilla `soap_java_escritorio`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «Servidor no disponible, intente más tarde» | Servidor apagado, API no desplegada o `URL_BASE_POR_DEFECTO` distinta de la real (contexto + prefijo). Probar la URL con `curl` |
| «No se pudo interpretar la respuesta del servidor (HTTP 404)» | Ruta mal escrita o prefijo de la API ausente en la URL base: el servidor respondió con una página que no es JSON. Probar la ruta con `curl` |
| Respuesta con campos vacíos | El nombre de un campo del DTO no coincide con el del JSON (Gson no avisa: lo deja `null`); los nombres son el contrato con el servidor |
| `IllegalArgumentException` al armar la URI / «URL no válida» | Segmento de ruta sin codificar (espacios, tildes): usar `TraductorApi.codificar`; o `URL_BASE_POR_DEFECTO` mal escrita |
| El servidor rechaza la petición por `Content-Type` o por JSON mal formado | Cuerpo armado a mano: usar `TraductorApi.cuerpo` (genera un objeto JSON con campos de texto) |
| La ventana no se ve bien con escala de pantalla distinta de 100 % | Un texto con HTML usa un ancho fijo: `EstilosUi.anchoHtml(px)` |
