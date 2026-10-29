# Plantilla: cliente móvil Android (Java) para APIs REST

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos. Trae el transporte HTTP, el armado y la lectura de las peticiones, los tipos comunes,
la verificación de la envoltura, el molde de los controladores (`ViewModel` + `LiveData`) y los estilos, y deja marcado lo que cambia con el **dominio**.
No incluye inicio de sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1) y `ESPECIFICACION_CLIENTES.md`. Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java (sin Kotlin ni Compose), compilación Java 11, `minSdk` 24. **Vistas:** XML + ViewBinding. **Arquitectura:** capas con `ViewModel` y `LiveData`, sin frameworks.
- **Transporte:** REST/JSON sobre `HttpURLConnection` (Android no incluye `java.net.http`). `api/TransporteHttp` es la única clase que abre conexiones, `api/TraductorApi` arma las peticiones y lee las respuestas
  sin tocar la red (traduciendo los fallos a `RED` y `SRV-000`) y `api/ConfiguracionApi` guarda la URL base, que la aplicación lee de `res/values/config.xml`.
  **JSON:** Gson 2.11.0 (única dependencia de terceros), que lee los DTO por nombre de campo.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes).
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`): URL base completa con prefijo y «/» final (R1), todo dato como texto (R9), rutas con variables codificadas (R10). El contrato concreto del servidor es su tabla de endpoints (`ENDPOINTS_REST.md`), que el proyecto copia en `docs/`. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su login y su sesión (en `modelo`); la plantilla no trae ninguno (§8). Deja el punto de enganche: `InicioActivity` (pantalla inicial, es la de entrada)
  y `LoginActivity`, una pantalla de acceso **pasiva** (sin controlador) lista para usar.
- **Verificada:** `./gradlew testDebugUnitTest lintDebug assembleDebug` → BUILD SUCCESSFUL, **30 pruebas JUnit en verde** (molde de controlador, verificación de la envoltura, configuración de la URL, armado y lectura de las peticiones y Base64) y lint con 0 errores (solo avisos de versión de dependencias). **No se ha ejecutado:** la app en el emulador o en un teléfono, ni contra un servidor real del dominio.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `build/`, `.gradle/` ni `local.properties`; Android Studio los regenera).
2. **Abrir en Android Studio** (*Open*) y esperar la sincronización de Gradle. La primera vez descarga un JDK 25 para el *daemon* de Gradle
   (`gradle/gradle-daemon-jvm.properties`) y necesita Internet; es independiente del JDK del servidor.
3. **Renombrar** (Android Studio → *Refactor → Rename*, nunca a mano):

   | Qué | Dónde | Valor de la plantilla |
   |---|---|---|
   | Nombre del proyecto | `settings.gradle.kts` → `rootProject.name` | `ClienteRestMovil` |
   | Nombre visible | `res/values/strings.xml` → `app_name` y `vista/Textos.NOMBRE_APLICACION` (deben coincidir) | `Cliente REST` |
   | Tema | `res/values/themes.xml` y `AndroidManifest.xml` | `Theme.ClienteRest` |
   | Paquete / `applicationId` | `app/build.gradle.kts` (`namespace`, `applicationId`) | `ec.edu.monster` (la convención: **no cambiarlo**) |

4. **Configurar el servidor:** la **URL base completa de la API** (host, puerto, contexto y prefijo, **con «/» final**) en `app/src/main/res/values/config.xml` (`url_base_api`), tal como la entrega la tabla de endpoints del servidor.
   `AplicacionCliente` la entrega a `ConfiguracionApi` al arrancar. Guardar una copia de esa tabla en `docs/` como contrato. Ver §6.
5. **Permitir el host** si es HTTP sin cifrar: `app/src/main/res/xml/network_security_config.xml` (ver §6).
6. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
7. **Probar el servidor con `curl`** antes de tocar la app (ver el ejemplo de §7) y luego ejecutar en el emulador.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Núcleo de servicios** | `servicios/ServicioBase` (verifica la envoltura: no OK → `ExcepcionServicio`; nula → `SRV-000`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (códigos propios `RED` y `SRV-000`) | No |
| **Configuración de la URL** | `api/ConfiguracionApi` (`fijarUrlBase`, `urlBase`), `res/values/config.xml` | `config.xml` siempre (URL); el resto no |
| **Transporte** | `api/Transporte` (interfaz de un método), `api/TransporteHttp` (única clase que abre conexiones), `api/RespuestaHttp` (código y cuerpo) | No |
| **Armado y lectura** | `api/TraductorApi`: ruta codificada, cuerpo JSON, cabecera `Basic`, lectura de la envoltura; traduce los fallos a `RED` y `SRV-000`. No toca la red | No |
| **Interfaz e implementación del dominio** | — (`api/XCliente.java` y `api/XClienteApi.java`) | **Se crean** por proyecto, una firma por fila de la tabla de endpoints (§4, pasos 2 y 3) |
| **DTO base** | `dto/RespuestaBaseDto` (POJO sin anotaciones) | Se agregan DTO del dominio al lado |
| **Molde de controlador** | `controlador/ControladorBase` (`ejecutar`: segundo plano, estado «ocupado», segunda acción ignorada, error como mensaje; `rechazar`: validación) | No; los controladores del dominio la extienden |
| **Composición** | `AplicacionCliente`, `controlador/FabricaControladores` | Se agregan clientes, servicios y controladores del dominio |
| **Estilos** | `res/values/{colors,styles,themes}.xml`, `res/color/`, `res/drawable/`, `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se repiten valores en los layouts |
| **Textos** | `vista/Textos` | `NOMBRE_APLICACION` y los textos del dominio |
| **Punto de enganche** | `vista/InicioActivity` + `activity_inicio.xml` (pantalla inicial de ejemplo), `vista/LoginActivity` + `activity_login.xml` (pasiva, sin controlador) | Se reemplazan/usan según el proyecto |
| **Dominio (vacío)** | `modelo/`, `controlador/` | Nuevos `dto/`, `servicios/`, `controlador/`, `vista/`, interfaz `XCliente` e implementación `XClienteApi` en `api/`, layouts |

Las zonas a adaptar están marcadas con **`DOMINIO`** o **`TODO`** en el código (`grep -rn "DOMINIO\|TODO" app/src`).

## 3. Estructura

```
app/src/main/java/ec/edu/monster/
├── AplicacionCliente.java      punto de composición (URL base, clientes REST, servicios, fábrica)
├── modelo/                     datos propios del cliente (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio
├── dto/           RespuestaBaseDto                 POJOs sin anotaciones para Gson, todos los campos como texto
├── servicios/     ServicioBase                     única capa que conoce la interfaz del cliente y los dto
├── api/           ConfiguracionApi, Transporte, TransporteHttp, RespuestaHttp, TraductorApi     + XCliente / XClienteApi por proyecto
├── controlador/   ControladorBase, FabricaControladores        + un controlador (ViewModel) por pantalla
├── vista/         Textos, InicioActivity, LoginActivity, ZonaEstado, Teclado        + una Activity por pantalla
└── pruebas/       Verificador, PruebasTodas        pruebas manuales (réplicas de JUnit), Java puro
app/src/main/res/                       layouts, estilos (values, color, drawable), config.xml (URL base), network_security_config.xml
app/src/test/java/ec/edu/monster/       JUnit 4 local (sin emulador ni servidor), misma estructura
docs/                                   CLIENT_STYLES_JAVA, CONVENCION_REST, CONVENCION_PRUEBAS, PANTALLAS y PROJECT (esqueletos)
```

Dependencias: `vista → controlador → servicios → api | dto`; `modelo`, `dto` y `excepciones` son transversales.
**La vista nunca importa `api`.** Solo `XClienteApi` conoce HTTP y JSON; `servicios` ve únicamente la interfaz `XCliente`.
Si el contrato cambia solo se tocan `api`, `dto` y, si cambia un parámetro, `servicios`.

## 4. Receta: agregar una operación REST y su pantalla

Siempre en este orden, de abajo hacia arriba. Ejemplo: `GET items` que devuelve una lista y `POST items/{codigo}/pedidos` con un cuerpo.

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
   `TraductorApi` es de acceso de paquete: `XClienteApi` va en `ec.edu.monster.api`. El ejemplo usa `Authorization: Basic` porque es lo que hace `TraductorApi.autorizacionBasic`; si el servidor usa otro esquema, el valor de la cabecera se arma aquí.
   Las credenciales **nunca** van en la ruta ni se registran. `TraductorApi.llamar` lee la envoltura **también con HTTP de error**, `IOException` → `RED`, cuerpo vacío o que no es JSON o sin `estado` → `SRV-000`.
   Se crea en `AplicacionCliente`: `XClienteApi.crear()`.
4. **Servicio** — `servicios/XServicio extends ServicioBase`: valida lo trivial (campos vacíos), convierte los datos al formato del contrato (coma decimal → punto) y llama:
   `return verificar(cliente.obtenerItems(usuario, clave));`. Las reglas del dominio las decide el servidor; el cliente muestra su `mensaje`. No captura excepciones de red: ya llegan traducidas a `RED`. Se crea en `AplicacionCliente` y se pasa a la fábrica.
5. **Controlador** — `XControlador extends ControladorBase<Datos>` (los datos de la pantalla: lo escrito, el resultado, el destino de navegación). Valida los campos vacíos en el orden de la pantalla con
   `rechazar(Textos.campoObligatorio(...))` y llama al servicio **con `ejecutar(textoOcupado, tarea, alExito)`**, que usa el `Executor` inyectado (nunca en el hilo principal), publica el estado «ocupado», ignora una
   segunda acción y muestra el error como mensaje. Un proyecto con login sobrescribe `manejar` para tratar el código de sesión no válida de su servidor. Registrarlo en `FabricaControladores.create(...)`.
   Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar campos vacíos antes de llamar; mostrar `mensaje`, nunca el código; conservar los datos ante un error salvo los sensibles (claves, que la vista limpia);
   estado «ocupado» que deshabilita los controles; no conocer la pantalla siguiente (la navegación es un dato del estado que consume la actividad).
6. **Pantalla** — layout con los estilos (§5), `Activity` que pide su controlador a la fábrica (`new ViewModelProvider(this, app.getFabrica())`), observa el estado (dibuja `ocupado`, `mensaje` con `error`, `datos`) y delega los eventos.
   Los textos salen de `Textos`, no de `strings.xml`. Declarar la `Activity` en `AndroidManifest.xml` (`exported="false"`).
7. **Pruebas** — unitarias, sin servidor ni emulador (`docs/CONVENCION_PRUEBAS.md`): `XCliente` es una interfaz, sirve un doble para el servicio (OK, error de negocio, red, respuesta nula, coma enviada como punto);
   para `XClienteApi`, un `Transporte` falso (una lambda) que registra verbo, ruta, cuerpo y cabecera y devuelve una `RespuestaHttp` (ver `TraductorApiTest`: no hace falta red ni servidor);
   los controladores, con un doble del servicio y un `Executor` sincrónico (`Runnable::run`) o manual (ver `ControladorBaseTest`). El contrato **no** se prueba con código ni se levanta un servidor HTTP: se verifica a mano con `docs/CONVENCION_PRUEBAS.md` §6.3.
   Las pruebas manuales (`ec.edu.monster.pruebas`, Java puro) se generan al final con `JAVA_TESTS_CONVENTIONS.md` y se agregan a `PruebasTodas`.

Reglas que se repiten en todo dominio:
- **La envoltura decide, no el código HTTP:** el cliente lee `estado`/`codigo`/`mensaje` del cuerpo también cuando el HTTP es 4xx o 5xx. Solo la falta de respuesta es `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Si el proyecto tiene login: la sesión vive solo en memoria (`modelo`), nunca en disco ni en logs; las credenciales se envían según el contrato del servidor.
- Todo dato viaja como **texto**; los decimales con **punto**. Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
- Mostrar solo el **mensaje**, nunca el código. Textos con tildes, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- **Nunca red en el hilo principal.** Los fallos de red no cierran la app.
- **Solo APIs válidas con `minSdk` 24 y Java 11** en `main`: no `java.util.Base64`, `URLEncoder.encode(String, Charset)`, `String.isBlank`, `List.of` ni `record` (el lint avisa `NewApi`); y nada de `android.*` en lo que se prueba en local (`ENTORNO_ANDROID.md` §5).

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Los layouts **solo referencian estilos**; márgenes laterales de `24dp` en el layout. Componentes disponibles:

| Estilo | Para qué |
|---|---|
| `Boton.Primario` | Acción principal de la pantalla (relleno `primario`, texto blanco) |
| `Boton.Secundario` | Salida o regreso: «Cerrar sesión», «Cancelar» (sólido `secundario`) |
| `Panel`, `Panel.Titulo`, `Panel.Resultado` | Mensaje, encabezado de pantalla y resultado |
| `Etiqueta`, `Campo`, `Selector` | Etiqueta de campo, campo de texto y lista desplegable (`item_spinner.xml` para el adaptador) |
| `ProgressBar` con `@color/acento` | Indicador de carga; deshabilitar los controles mientras dura |

- **Fondo:** `res/drawable-nodpi/fondo_app.png` (opcional; imagen de ejemplo) sobre el color `fondo` de respaldo. Es una imagen **de ejemplo**: sustituirla o quitar el `ImageView` de los layouts según el proyecto (no se usa imagen en la presentación clara).
- **Presentación clara:** redefinir los mismos nombres de `colors.xml` con los valores de `CLIENT_STYLES_JAVA.md` §2.3. No hay selector de tema.
- `fitsSystemWindows` reemplaza el `padding` del mismo `View`: usar márgenes en los hijos.
- Los recursos de la biblioteca que las pantallas de ejemplo no usan están exentos del aviso `UnusedResources` en `app/lint.xml`.
- El móvil es de una columna y no usa la disposición dividida ni los componentes exclusivos del escritorio (cuadrícula de acciones, tabla de ancho completo); si un resultado necesita una tarjeta o una tabla, se construye con `Panel`/`Panel.Resultado`.
- Antes de construir las pantallas de un proyecto conviene especificarlas en su `docs/PANTALLAS.md` (orden de los elementos, estados y decisiones; viene un esqueleto).

## 6. Servidor y emulador

- **URL base:** `res/values/config.xml` (`url_base_api`), una sola línea. En el emulador el host es `10.0.2.2` (la PC anfitriona; **no** `localhost`). Cómo llegar al servidor desde un teléfono físico está en `context/entornos/ENTORNO_ANDROID.md` §4.
- **HTTP sin cifrar:** Android 9+ lo bloquea; el host de desarrollo se permite en `res/xml/network_security_config.xml` —sin esto la app falla con `CLEARTEXT communication ... not permitted`—. El permiso `INTERNET` ya está en el manifiesto.
- Con el servidor apagado la app abre y el fallo llega al **llamar** una operación, donde `TransporteHttp` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **Sin modo de transmisión fija:** `TransporteHttp` envía el cuerpo sin `setFixedLengthStreamingMode`; un servidor que responda 401 con `WWW-Authenticate` podría, con ese modo, hacer que `HttpURLConnection` falle en lugar de entregar la respuesta (`CONVENCION_REST.md` §8.3).
- **R8:** `optimization` de `release` está desactivada porque Gson lee los DTO por reflexión; si se activa, conservar `ec.edu.monster.dto.**` en `app/src/main/keepRules/rules.keep` **(no verificado)**.

## 7. Comandos y verificación

```
./gradlew testDebugUnitTest      # pruebas locales (sin emulador ni servidor)
./gradlew assembleDebug lintDebug    # compilar + lint
```
En Windows también `gradlew.bat`. Si Gradle no encuentra el SDK, abrir el proyecto una vez en Android Studio (crea `local.properties`) o definir `ANDROID_HOME`.

Probar una operación del servidor sin la app: `curl -i -X POST <BASE>auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"clave\":\"...\"}"` (la ruta y el cuerpo son los de la tabla de endpoints del proyecto).

Un agente que no pueda ejecutar el emulador debe decir explícitamente que **solo verificó compilación, lint y pruebas locales** y que no vio la app real.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- **URL base completa** con prefijo común y «/» final (R1); las rutas de las operaciones son relativas y sin barra inicial; los segmentos variables van codificados (R10).
- Toda respuesta (también las de error) trae `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación; los errores usan códigos HTTP 4xx/5xx por categoría (R5 a R7). En un error los campos propios se omiten y las listas llegan vacías.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R9), los nombres de campos son fijos y en `camelCase` (R4) y los errores de plataforma (ruta, verbo, `Content-Type`, JSON mal formado, error interno) también traen la envoltura (R6).
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (cabecera `Basic` por petición, token, credenciales en el cuerpo, nada) lo define cada servidor y su cliente lo implementa en `api` (la cabecera) y `servicios`/`modelo` (la sesión); se documenta en el `docs/PROJECT.md` del proyecto. `TraductorApi.autorizacionBasic` solo arma el valor `Basic`.
- Para un servicio **SOAP** usar la plantilla `soap_java_movil`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «Servidor no disponible, intente más tarde» siempre | Se usó `localhost` en lugar de `10.0.2.2`, el servidor está apagado, o la URL base no coincide con la real (contexto + prefijo). Probar la URL con `curl` |
| `CLEARTEXT communication ... not permitted` | Falta el host en `network_security_config.xml` (§6) |
| «No se pudo interpretar la respuesta del servidor (HTTP 404)» | Ruta mal escrita o prefijo de la API ausente en la URL base: el servidor respondió con una página que no es JSON |
| Campos vacíos tras una respuesta correcta | El nombre de un campo del DTO no coincide con el del JSON (Gson no avisa: lo deja `null`), o R8 renombró los DTO (§6) |
| `IllegalStateException: La URL base de la API no fue fijada` | `AplicacionCliente` no llamó a `ConfiguracionApi.fijarUrlBase(...)` antes de crear los clientes |
| Lint: `NewApi` en una API de Java moderna | Con `minSdk` 24 evitar `java.util.Base64`, `String.isBlank`, `List.of`, `record`, etc. en `main` |
| `Unresolved reference` / no existe `ActivityXBinding` | ViewBinding genera la clase desde el nombre del layout (`activity_x.xml` → `ActivityXBinding`): sincronizar Gradle |
| Las pruebas locales devuelven valores por defecto en una clase de Android (`org.json`, `Log`, `android.util.Base64`) | El `android.jar` de las pruebas locales es un *stub*: usar solo Java puro en lo que se prueba (Gson lo es) |
