# Plantilla: cliente móvil Android (Java) para APIs REST

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga una API **REST/JSON** (JAX-RS en Jakarta EE)
con el esquema de respuesta de esta familia de proyectos (`estado`, `codigo`, `mensaje` + campos de la operación; token Bearer). Contiene lo que **todo**
cliente necesita y deja claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java (sin Kotlin ni Compose). **Vistas:** XML + ViewBinding. **Arquitectura:** MVC con `ViewModel` y `LiveData`.
- **Transporte:** `HttpURLConnection` (Android no incluye `java.net.http`). **JSON:** Gson 2.11.0.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes).
- **Verificada:** `./gradlew assembleDebug testDebugUnitTest lintDebug` → BUILD SUCCESSFUL, 24 pruebas JUnit en verde (servicio, controladores y transporte contra un servidor local de la JVM).
  **No se ha ejecutado:** la app en emulador o teléfono, la comunicación contra un servidor real ni el comportamiento de `HttpURLConnection` **de Android** ante un `401` (ver §9).
  La interfaz, los estilos, la navegación y los `ViewModel` vienen sin cambios de la plantilla SOAP, donde sí se verificaron en emulador.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
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
   | Nombre visible y título del login | `res/values/strings.xml` → `app_name` | `Cliente REST` |
   | Tema | `res/values/themes.xml` y `AndroidManifest.xml` | `Theme.ClienteRest` |
   | Paquete / `applicationId` | `app/build.gradle.kts` (`namespace`, `applicationId`) | `ec.edu.monster` (la convención: **no cambiarlo**) |

4. **Configurar el servidor** en un único archivo: `app/src/main/java/ec/edu/monster/api/ConfiguracionApi.java`
   (`BASE_POR_DEFECTO` = contexto + prefijo de la API, `RUTA_LOGIN`). Ver §8.
5. **Permitir el host** si es HTTP sin cifrar: `app/src/main/res/xml/network_security_config.xml` (ver §6).
6. **Crear `PROJECT.md`** a partir de `PROJECT.md` de esta carpeta (esqueleto) y mantenerlo al día.
7. **Probar el servidor con `curl`** antes de tocar la app (ver el ejemplo de §7) y luego ejecutar en emulador: el login debe funcionar tal cual.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Login (compartido)** | `vista/LoginActivity`, `controlador/LoginControlador`, `servicio/ServicioAutenticacion`, `dto/RespuestaLoginDto`, `res/layout/activity_login.xml` | Solo `INICIO` en `LoginActivity` (pantalla de destino) y, si el servidor difiere, `RUTA_LOGIN` y `SolicitudLoginDto` |
| **Núcleo de servicios** | `servicio/ServicioBase` (sesión, `token()`, `get`, `post`, `postPublico`, `codificar`, interpretación del código HTTP) | No; los servicios del dominio la extienden |
| **Sesión y errores** | `modelo/Sesion`, `excepciones/ExcepcionServicio` | No |
| **Transporte HTTP** | `api/ClienteApi` (interfaz), `api/ClienteApiHttp`, `api/RespuestaHttp`, `api/ConfiguracionApi` | `ConfiguracionApi` siempre; el resto no |
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaLoginDto`, `RespuestaListaDto`, `SolicitudLoginDto` | Se agregan DTO del dominio al lado |
| **Composición** | `AplicacionCliente`, `controlador/FabricaControladores`, `vista/Navegacion` | Se agregan servicios/controladores del dominio |
| **Estilos** | `res/values/{colors,styles,themes}.xml`, `res/color/`, `res/drawable/`, `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se repiten valores en los layouts |
| **Pantalla de ejemplo (dominio)** | `vista/InicioActivity`, `controlador/InicioControlador`, `activity_inicio.xml`, textos `inicio_*` | **Se reemplaza** por las pantallas del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/`, layouts y textos |

Las zonas a adaptar están marcadas con el comentario **`DOMINIO:`** en el código (`grep -rn "DOMINIO" app/src`).

## 3. Estructura

```
app/src/main/java/ec/edu/monster/
├── AplicacionCliente.java      punto de composición (Sesion, ClienteApi, servicios, fábrica)
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin(), servidorNoDisponible()
├── dto/           RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto, SolicitudLoginDto    POJOs para Gson
├── api/           ConfiguracionApi, ClienteApi, ClienteApiHttp, RespuestaHttp
├── servicio/      ServicioBase, ServicioAutenticacion     única capa que conoce api y dto
├── controlador/   LoginControlador, InicioControlador, FabricaControladores    ViewModel
└── vista/         LoginActivity, InicioActivity, Navegacion
app/src/test/java/ec/edu/monster/       JUnit 4 local (sin emulador ni servidor), misma estructura
```

Dependencias permitidas: `vista → controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `api` ni `dto`.** Si el contrato HTTP cambia solo se tocan `api`, `dto` y `servicio`.

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET /items` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **DTO** (si la respuesta no es login ni lista) — POJO que extienda `RespuestaBaseDto`; los nombres de campo son los del JSON del servidor.
   Para cuerpos de petición, un `SolicitudXxxDto` con los campos del JSON. Todo dato viaja como **texto** (`"valor": "1.5"`).
2. **Servicio** — `servicio/ServicioItems extends ServicioBase`:
   ```java
   public List<String> items() {
       return get("/items", RespuestaListaDto.class).getElementos();   // get() lanza AUTH-002 si no hay sesión
   }
   ```
   Para segmentos de ruta usar `codificar(texto)`; para `POST` protegido, `post(ruta, dto, Clase.class)`.
   Se crea en `AplicacionCliente` (`new ServicioItems(api, sesion)`) y se pasa a la fábrica. Aquí van las reglas **del cliente** (campo vacío, formato);
   las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
3. **Controlador** — copiar `LoginControlador` como molde: `Estado` inmutable (`cargando`, datos, `mensaje`, `sesionExpirada`), `LiveData`, `Executor` inyectado,
   `catch (ExcepcionServicio e)` → `e.getMessage()` y `e.exigeLogin()` → `sesionExpirada`. Registrarlo en `FabricaControladores.create(...)`.
4. **Pantalla** — layout con los estilos (§5), `Activity` que pide su controlador a la fábrica, observa el estado y delega eventos. Si `estado.sesionExpirada` →
   `Navegacion.irALogin(this, estado.mensaje)`. Declarar la `Activity` en `AndroidManifest.xml` (`exported="false"`) y los textos en `strings.xml`.
5. **Pruebas** — `ClienteApi` es una interfaz de un método: el doble es una lambda que devuelve `RespuestaHttp(codigo, json)` (ver `ServicioAutenticacionTest`) o
   `src/test/.../api/ApiFalsa` (agregar la ruta). Pruebas del servicio y del controlador, con **el JSON real** del servidor.

Reglas que se repiten en todo dominio (ver `context/CLIENTES_CONSTRUCT.md` del repositorio de contexto):
- **Se decide por el código HTTP** (`ServicioBase.interpretar`): 2xx → DTO; otro → `ExcepcionServicio` con `codigo` y `mensaje` del cuerpo (o «Error del servidor (HTTP n)» si no es JSON).
  Un `401` en una operación protegida descarta el token. Sin respuesta (servidor caído, timeout) → `RED` / «Servidor no disponible, intente más tarde».
- **El token** vive en `Sesion` (memoria), va en `Authorization: Bearer`, no se guarda en disco ni en logs. Ante `AUTH-002`/`AUTH-003` o un `401` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo principal.** Los fallos de red no cierran la app.
- Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
- Compatibilidad: `minSdk 24` y Java 11 → sin `record`, sin `String.isBlank()`, sin `URLEncoder.encode(String, Charset)`.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Los layouts **solo referencian estilos**; márgenes laterales de `24dp` en el layout. Componentes disponibles:

| Estilo | Para qué |
|---|---|
| `Boton.Primario` | Acción principal de la pantalla (relleno `primario`, texto blanco) |
| `Boton.Secundario` | Salida o regreso: «Cerrar sesión», «Volver» (sólido `secundario`) |
| `Panel`, `Panel.Titulo`, `Panel.Resultado` | Mensaje, encabezado de pantalla y resultado |
| `Etiqueta`, `Campo`, `Selector` | Etiqueta de campo, campo de texto y lista desplegable (`item_spinner.xml` para el adaptador) |
| `ProgressBar` con `@color/acento` | Indicador de carga; deshabilitar los controles mientras dura |

- **Fondo:** `res/drawable-nodpi/fondo_app.jpeg` (opcional; imagen de referencia, recortada a pantalla completa) sobre el color `fondo` de respaldo. Es una imagen **de ejemplo**:
  sustituirla o quitar el `ImageView` de los layouts según el proyecto (no se usa imagen en la presentación clara).
- **Presentación clara:** redefinir los mismos nombres de `colors.xml` con los valores de `CLIENT_STYLES.md` §2.3. No hay selector de tema.
- `fitsSystemWindows` reemplaza el `padding` del mismo `View`: usar márgenes en los hijos.
- Los recursos de la biblioteca que una pantalla de ejemplo no usa están exentos del aviso `UnusedResources` en `app/lint.xml`.

## 6. Servidor, emulador y teléfono real

| Entorno | Host en `ConfiguracionApi.BASE_POR_DEFECTO` |
|---|---|
| Emulador | `10.0.2.2` (la PC anfitriona; **no** `localhost`) |
| Teléfono real | IP de la PC en la red local (`ipconfig` → IPv4), misma red Wi-Fi |

Para HTTP sin cifrar (Android 9+ lo bloquea): agregar el **mismo host** en `res/xml/network_security_config.xml` (hay una línea de ejemplo comentada) —sin esto la
app falla con `CLEARTEXT communication ... not permitted`—. El permiso `INTERNET` ya está en el manifiesto. Para el teléfono real además:
- Abrir el puerto del servidor en el firewall de Windows (ejemplo para 8080, PowerShell como administrador):
  `netsh advfirewall firewall add rule name="Servidor 8080" dir=in action=allow protocol=TCP localport=8080`
- Probar primero desde el navegador del teléfono una ruta `GET` pública de la API (`http://<IP de la PC>:<puerto>/<contexto>/api/...`): si responde, la app también conectará.
- La IP cambia al cambiar de red: repetir los cambios de `ConfiguracionApi` y `network_security_config.xml` y reinstalar.

## 7. Comandos y verificación

```
./gradlew assembleDebug testDebugUnitTest lintDebug      # compilar + pruebas locales + lint
```
En Windows también `gradlew.bat`. Si Gradle no encuentra el SDK, abrir el proyecto una vez en Android Studio (crea `local.properties`) o definir `ANDROID_HOME`.

Probar el servidor sin la app:
```
curl -i -X POST <BASE>/auth/login -H "Content-Type: application/json" -d '{"usuario":"u","contrasenia":"c"}'
```
Copiar las respuestas reales (OK, error de negocio, token inválido, error sin cuerpo JSON) como datos de las pruebas: así un nombre de campo mal escrito falla en la prueba y no en el teléfono.

Un agente que no pueda ejecutar el emulador debe decir explícitamente que **solo verificó compilación y pruebas locales**.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `ServicioAutenticacion`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- Login: `POST` a `ConfiguracionApi.RUTA_LOGIN` con `{"usuario","contrasenia"}` → `200` con `{estado, codigo, mensaje, token}`.
- Las operaciones protegidas reciben `Authorization: Bearer <token>`.
- Toda respuesta (también las de error) trae `estado`, `codigo` y `mensaje`; los errores usan códigos HTTP 4xx/5xx.
- `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`); además, cualquier `401` en una operación protegida.
- Si el servidor usa otra forma de autenticación (cookies, API key, OAuth), ampliar `ClienteApiHttp` y `ServicioBase`.
- La plantilla **no está verificada** contra ningún servidor concreto: comprobar el contrato real (rutas, campos, códigos) antes de construir el dominio.

## 9. Puntos a vigilar en Android

- **`401` sin cabecera `WWW-Authenticate`:** algunas versiones de `HttpURLConnection` de Android lanzan `IOException("No authentication challenges found")` en lugar de devolver el código.
  `ClienteApiHttp` lo trata como `401` **sin cuerpo**: para operaciones protegidas funciona (token descartado), pero un login con credenciales incorrectas mostraría
  «Error del servidor (HTTP 401)» en vez del mensaje del servidor. Si ocurre en el dispositivo, hacer que el servidor envíe `WWW-Authenticate: Bearer` en sus `401`.
- **No se usa el modo *streaming* de la conexión** (`setFixedLengthStreamingMode`): con él un `401` en un `POST` lanza `HttpRetryException`. Los cuerpos son pequeños; no hace falta.
- Estos dos puntos no se han podido comprobar en emulador desde la plantilla: confirmarlos con un servidor real.
