# Plantilla: cliente móvil Android (Java) para servicios SOAP

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita y deja
claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java (sin Kotlin ni Compose). **Vistas:** XML + ViewBinding. **Arquitectura:** MVC con `ViewModel` y `LiveData`.
- **Transporte:** SOAP 1.1 armado a mano sobre `HttpURLConnection` (Android no incluye JAX-WS). Sin librerías de terceros.
- **Estilo:** `docs/CLIENT_STYLES.md` (paleta oscura/clara, tipografía, medidas, componentes).
- **Verificada** al crearla: `./gradlew assembleDebug testDebugUnitTest lintDebug` → BUILD SUCCESSFUL, 29 pruebas JUnit en verde, lint sin avisos.
  Además se comprobó en el emulador, apuntando temporalmente a un servidor real (el del Conversor de Unidades): login correcto → pantalla de ejemplo, y el
  transporte y el parser provienen de ese cliente móvil, que también se verificó en teléfono real. **Lo que no se ha ejecutado** es una operación de dominio
  distinta del login: la receta de §4 está probada en su forma original (lista, conversión) pero no con un dominio nuevo.

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
   | Nombre del proyecto | `settings.gradle.kts` → `rootProject.name` | `ClienteSoapMovil` |
   | Nombre visible y título del login | `res/values/strings.xml` → `app_name` | `Cliente SOAP` |
   | Tema | `res/values/themes.xml` y `AndroidManifest.xml` | `Theme.ClienteSoap` |
   | Paquete / `applicationId` | `app/build.gradle.kts` (`namespace`, `applicationId`) | `ec.edu.monster` (la convención: **no cambiarlo**) |

4. **Configurar el servidor** en un único archivo: `app/src/main/java/ec/edu/monster/ws/ConfiguracionServicio.java`
   (`URL_SERVICIO` y `NAMESPACE`). El namespace es el `targetNamespace` del `@WebService` del servidor (se ve en el WSDL).
5. **Permitir el host** si es HTTP sin cifrar: `app/src/main/res/xml/network_security_config.xml` (ver §6).
6. **Crear `PROJECT.md`** a partir de `PROJECT.md` de esta carpeta (esqueleto) y mantenerlo al día.
7. **Probar el servidor con `curl`** antes de tocar la app (ver el ejemplo de §7) y luego ejecutar en emulador: el login debe funcionar tal cual.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Login (compartido)** | `vista/LoginActivity`, `controlador/LoginControlador`, `servicio/ServicioAutenticacion`, `dto/RespuestaLoginDto`, `res/layout/activity_login.xml` | Solo `INICIO` en `LoginActivity` (pantalla de destino) y, si el servidor difiere, la operación `login` |
| **Núcleo de servicios** | `servicio/ServicioBase` (sesión, `token()`, `verificar(...)`) | No; los servicios del dominio la extienden |
| **Sesión y errores** | `modelo/Sesion`, `excepciones/ExcepcionServicio` | No |
| **Transporte SOAP** | `ws/SoapXml`, `ws/ClienteSoapHttp`, `ws/ConfiguracionServicio` | `ConfiguracionServicio` siempre; `ClienteSoapHttp` y `ContratoWS` para **agregar operaciones** |
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaListaDto` | Se agregan DTO del dominio al lado |
| **Composición** | `AplicacionCliente`, `controlador/FabricaControladores`, `vista/Navegacion` | Se agregan servicios/controladores del dominio |
| **Estilos** | `res/values/{colors,styles,themes}.xml`, `res/color/`, `res/drawable/`, `docs/CLIENT_STYLES.md` | Colores solo si se cambia de presentación; **no** se repiten valores en los layouts |
| **Pantalla de ejemplo (dominio)** | `vista/InicioActivity`, `controlador/InicioControlador`, `activity_inicio.xml`, textos `inicio_*` | **Se reemplaza** por las pantallas del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/`, layouts y textos |

Las zonas a adaptar están marcadas con el comentario **`DOMINIO:`** en el código (`grep -rn "DOMINIO" app/src`).

## 3. Estructura

```
app/src/main/java/ec/edu/monster/
├── AplicacionCliente.java      punto de composición (Sesion, contrato SOAP, servicios, fábrica)
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin()
├── dto/           RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto    POJOs, sin anotaciones
├── ws/            ConfiguracionServicio, ContratoWS, ClienteSoapHttp, SoapXml
├── servicio/      ServicioBase, ServicioAutenticacion     única capa que conoce ws y dto
├── controlador/   LoginControlador, InicioControlador, FabricaControladores    ViewModel
└── vista/         LoginActivity, InicioActivity, Navegacion
app/src/test/java/ec/edu/monster/       JUnit 4 local (sin emulador ni servidor), misma estructura
```

Dependencias permitidas: `vista → controlador → servicio → ws/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `ws` ni `dto`.** Si el WSDL cambia solo se tocan `ws`, `dto` y `servicio`.

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems(token)` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Contrato** — `ws/ContratoWS.java`: `RespuestaListaDto obtenerItems(String token);`
2. **Transporte** — `ws/ClienteSoapHttp.java` (tres líneas; los nombres de parámetro son los del servidor y van **en su orden**):
   ```java
   @Override
   public RespuestaListaDto obtenerItems(String token) {
       return SoapXml.leerLista(enviar("obtenerItems", parametros("token", token)));
   }
   ```
3. **DTO** (si la respuesta no es login ni lista) — clase `…Dto` que extienda `RespuestaBaseDto` y un método `leerXxx(xml)` en `SoapXml` que use los bloques
   `extraerRespuesta`, `llenarBase`, `texto`, `hijo` y `lista`.
4. **Servicio** — `servicio/ServicioItems extends ServicioBase`:
   ```java
   public List<String> items() {
       return verificar(ws.obtenerItems(token())).getElementos();   // token() lanza AUTH-002 si no hay sesión
   }
   ```
   Se crea en `AplicacionCliente` (`new ServicioItems(ws, sesion)`) y se pasa a la fábrica. Aquí van las reglas **del cliente** (campo vacío, formato);
   las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
5. **Controlador** — copiar `LoginControlador` como molde: `Estado` inmutable (`cargando`, datos, `mensaje`, `sesionExpirada`), `LiveData`, `Executor` inyectado,
   `catch (ExcepcionServicio e)` → `e.getMessage()` y `e.exigeLogin()` → `sesionExpirada`. Registrarlo en `FabricaControladores.create(...)`.
6. **Pantalla** — layout con los estilos (§5), `Activity` que pide su controlador a la fábrica, observa el estado y delega eventos. Si `estado.sesionExpirada` →
   `Navegacion.irALogin(this, estado.mensaje)`. Declarar la `Activity` en `AndroidManifest.xml` (`exported="false"`) y los textos en `strings.xml`.
7. **Pruebas** — doble en `src/test/.../ws/ContratoFalso` (agregar la operación), pruebas del servicio y del controlador, y de `SoapXml` con **el XML real** del servidor.

Reglas que se repiten en todo dominio (ver `context/CLIENTES_CONSTRUCT.md` del repositorio de contexto):
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo principal.** Los fallos de red no cierran la app.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES.md`)

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

| Entorno | Host en `ConfiguracionServicio.URL_SERVICIO` |
|---|---|
| Emulador | `10.0.2.2` (la PC anfitriona; **no** `localhost`) |
| Teléfono real | IP de la PC en la red local (`ipconfig` → IPv4), misma red Wi-Fi |

Para HTTP sin cifrar (Android 9+ lo bloquea): agregar el **mismo host** en `res/xml/network_security_config.xml` (hay una línea de ejemplo comentada) —sin esto la
app falla con `CLEARTEXT communication ... not permitted`—. El permiso `INTERNET` ya está en el manifiesto. Para el teléfono real además:
- Abrir el puerto del servidor en el firewall de Windows (ejemplo para 8080, PowerShell como administrador):
  `netsh advfirewall firewall add rule name="Servidor 8080" dir=in action=allow protocol=TCP localport=8080`
- Probar primero desde el navegador del teléfono `http://<IP de la PC>:<puerto>/<contexto>/<Servicio>?wsdl`: si carga el XML, la app también conectará.
- La IP cambia al cambiar de red: repetir los cambios de `ConfiguracionServicio` y `network_security_config.xml` y reinstalar.

## 7. Comandos y verificación

```
./gradlew assembleDebug testDebugUnitTest lintDebug      # compilar + pruebas locales + lint
```
En Windows también `gradlew.bat`. Si Gradle no encuentra el SDK, abrir el proyecto una vez en Android Studio (crea `local.properties`) o definir `ANDROID_HOME`.

Probar una operación del servidor sin la app (el sobre es el mismo que arma `SoapXml`):
```
curl -s -H "Content-Type: text/xml; charset=utf-8" -H 'SOAPAction: ""' -d '<?xml version="1.0" encoding="UTF-8"?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><ns:login xmlns:ns="<NAMESPACE>"><usuario>u</usuario><contrasenia>c</contrasenia></ns:login></S:Body></S:Envelope>' <URL_SERVICIO>
```
Copiar las respuestas reales (OK, error de negocio, token inválido, `Fault`) como datos de `SoapXmlTest`: así un nombre de elemento mal escrito falla en la prueba y no en el teléfono.

Un agente que no pueda ejecutar el emulador debe decir explícitamente que **solo verificó compilación y pruebas locales**.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `SoapXml` / `ClienteSoapHttp` / `ServicioAutenticacion`:

- SOAP **1.1** por HTTP POST, `Content-Type: text/xml; charset=utf-8`, `SOAPAction: ""`, estilo *document/wrapped* (JAX-WS por defecto).
- Los parámetros de cada operación son elementos **sin namespace** dentro del elemento de la operación (que sí lleva el namespace del servicio).
- Cada respuesta es `<{operacion}Response><respuesta>…</respuesta></{operacion}Response>` y `respuesta` contiene `estado` (`OK`/`ERROR`), `codigo` y `mensaje`,
  más los datos de la operación (`ELEMENTO_RESPUESTA` en `SoapXml`).
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteSoapHttp.enviar` / `SoapXml.construirPeticion`.
- Para un servicio **REST** reutilizar de esta plantilla todo salvo `ws/` y `dto/` (la capa `servicio` solo depende de `ContratoWS`).
