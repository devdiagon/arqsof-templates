# Plantilla: cliente móvil Android (Java) para servicios SOAP

Base reutilizable para construir el cliente móvil de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Trae el transporte SOAP armado a mano, los tipos comunes,
la verificación de la envoltura, el molde de los controladores (`ViewModel` + `LiveData`) y los estilos, y deja marcado lo que cambia con el **dominio**.
No incluye inicio de sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1) y `ESPECIFICACION_CLIENTES.md`. Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java (sin Kotlin ni Compose), compilación Java 11. **Vistas:** XML + ViewBinding. **Arquitectura:** capas con `ViewModel` y `LiveData`, sin frameworks.
- **Transporte:** SOAP 1.1 armado a mano sobre `HttpURLConnection` (Android no incluye JAX-WS, así que no hay proxy generado ni `ws.generado`). `ws/ClienteSoapHttp` envía,
  `ws/SoapXml` arma el sobre y lee la respuesta, `ws/TraductorRed` traduce los fallos de red a `RED` y `ws/ConfiguracionServicio` fija el TNS y los timeouts. Sin librerías de terceros.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes).
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (TNS fijo `http://ws.monster.edu.ec/`, R2; sobre a mano: §4 c). Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su login y su sesión (en `modelo`). La plantilla deja el punto de enganche: `InicioActivity` (pantalla inicial, es la de entrada)
  y `LoginActivity`, una pantalla de acceso **pasiva** (sin controlador) lista para usar.
- **Verificada:** `./gradlew testDebugUnitTest lintDebug assembleDebug` → BUILD SUCCESSFUL, **46 pruebas JUnit en verde** (molde de controlador, verificación de la envoltura, traducción de red y de respuesta ilegible, lectura y armado del XML y 6 de conformidad de ejemplo contra un servidor HTTP falso local), lint sin avisos y las 5 pruebas manuales (`ec.edu.monster.pruebas`) en verde ejecutando su `main` en una JVM. **No se ha ejecutado:** la app en el emulador o en un teléfono, ni contra un servidor real del dominio.
  lectura y armado del XML y 6 de conformidad de ejemplo contra un servidor HTTP falso local) y lint sin avisos. **No se ha ejecutado:** la app en el emulador o en un teléfono, ni contra un servidor real del dominio.

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
   | Nombre del proyecto | `settings.gradle.kts` → `rootProject.name` | `ClienteSoapMovil` |
   | Nombre visible | `res/values/strings.xml` → `app_name` y `vista/Textos.NOMBRE_APLICACION` (deben coincidir) | `Cliente SOAP` |
   | Tema | `res/values/themes.xml` y `AndroidManifest.xml` | `Theme.ClienteSoap` |
   | Paquete / `applicationId` | `app/build.gradle.kts` (`namespace`, `applicationId`) | `ec.edu.monster` (la convención: **no cambiarlo**) |

4. **Configurar el servidor:** la **URL completa de cada servicio** (host, puerto y ruta, sin `?wsdl`) en `app/src/main/res/values/config.xml` (`url_servicio`; una línea por servicio si el servidor publica varios:
   cada uno con su URL completa, R9). El TNS es fijo y ya está en `ws/ConfiguracionServicio`; el nombre de cada servicio (`<wsdl:service name>`) lo recibe su `XClienteWS` (§4, paso 2). Ver §8.
5. **Permitir el host** si es HTTP sin cifrar: `app/src/main/res/xml/network_security_config.xml` (ver §6).
6. **Crear `docs/PROJECT.md`** a partir de `docs/PROJECT.md` de esta carpeta (esqueleto) y mantenerlo al día. Retirar los ejemplos de conformidad (`ConvencionClienteTest`; las pruebas automáticas de conformidad están fuera de alcance, `docs/CONVENCION_PRUEBAS.md` §8).
7. **Probar el servidor con `curl`** antes de tocar la app (ver el ejemplo de §7) y luego ejecutar en emulador.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Núcleo de servicios** | `servicios/ServicioBase` (verifica la envoltura: no OK → `ExcepcionServicio`; nula → `SRV-000`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (códigos propios `RED` y `SRV-000`) | No |
| **Transporte SOAP** | `ws/ClienteSoapHttp`, `ws/SoapXml`, `ws/TraductorRed`, `ws/ConfiguracionServicio`, `res/values/config.xml` | `config.xml` siempre (URL); el resto no |
| **DTO base** | `dto/RespuestaBaseDto` (POJO sin anotaciones) | Se agregan DTO del dominio al lado |
| **Molde de controlador** | `controlador/ControladorBase` (`ejecutar`: segundo plano, estado «ocupado», segunda acción ignorada, error como mensaje; `rechazar`: validación) | No; los controladores del dominio la extienden |
| **Composición** | `AplicacionCliente`, `controlador/FabricaControladores` | Se agregan clientes, servicios y controladores del dominio |
| **Zona de estado y teclado** | `res/layout/zona_estado.xml`, `res/drawable/mensaje_{error,exito}_fondo.xml`, `vista/ZonaEstado`, `vista/Teclado` (ver `docs/PANTALLAS.md` §2) | No; las pantallas del dominio las incluyen/usan |
| **Pruebas manuales** | `pruebas/Verificador`, `pruebas/PruebasWs`, `pruebas/PruebasTodas` (§9) | `PruebasTodas` para agregar las del dominio |
| **Estilos** | `res/values/{colors,styles,themes}.xml`, `res/color/`, `res/drawable/`, `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se repiten valores en los layouts |
| **Textos** | `vista/Textos` | `NOMBRE_APLICACION` y los textos del dominio |
| **Punto de enganche** | `vista/InicioActivity` + `activity_inicio.xml` (pantalla inicial de ejemplo), `vista/LoginActivity` + `activity_login.xml` (pasiva, sin controlador) | Se reemplazan/usan según el proyecto |
| **Dominio (vacío)** | `modelo/`, `controlador/` | Nuevos `dto/`, `servicios/`, `controlador/`, `vista/`, interfaz `XCliente` e implementación `XClienteWS` en `ws/`, layouts |

Las zonas a adaptar están marcadas con **`DOMINIO`** o **`TODO`** en el código (`grep -rn "DOMINIO\|TODO" app/src`).

## 3. Estructura

```
app/src/main/java/ec/edu/monster/
├── AplicacionCliente.java      punto de composición (clientes SOAP, servicios, fábrica)
├── modelo/                     datos propios del cliente (p. ej. la sesión, si el proyecto tiene login)
├── excepciones/   ExcepcionServicio
├── dto/           RespuestaBaseDto                 POJOs sin anotaciones
├── servicios/     ServicioBase                     única capa que conoce la interfaz del cliente y los dto
├── ws/            ConfiguracionServicio, TraductorRed, ClienteSoapHttp, SoapXml      + XCliente / XClienteWS por proyecto
├── controlador/   ControladorBase, FabricaControladores        + un controlador (ViewModel) por pantalla
├── vista/         Textos, ZonaEstado, Teclado, InicioActivity, LoginActivity        + una Activity por pantalla
└── pruebas/       Verificador, PruebasWs, PruebasTodas        pruebas manuales (§9)
app/src/main/res/                       layouts, estilos (values, color, drawable), config.xml (URL), network_security_config.xml
app/src/test/java/ec/edu/monster/       JUnit 4 local (sin emulador ni servidor), misma estructura
docs/                                   CLIENT_STYLES_JAVA, CONVENCION_SOAP, CONVENCION_PRUEBAS, PANTALLAS, PROJECT
```

Dependencias: `vista → controlador → servicios → ws | dto`; `modelo`, `dto` y `excepciones` son transversales.
**La vista nunca importa `ws`.** Solo `XClienteWS` conoce `ClienteSoapHttp`, `SoapXml` y el protocolo; `servicios` ve únicamente la interfaz `XCliente`.
Si el WSDL cambia solo se tocan `ws`, `dto` y, si cambia un parámetro, `servicios`.

## 4. Receta: agregar una operación SOAP y su pantalla

Siempre en este orden, de abajo hacia arriba. Ejemplo: `obtenerItems(codigo)` del servicio `XServicio`, que devuelve una lista.

1. **Contrato** — copiar el WSDL del servidor y su `.xsd` a `app/src/main/resources/wsdl/` **solo como referencia** (no se genera nada: Android no tiene `wsimport`). De él salen el nombre del servicio,
   los nombres de operación y de parámetro (**en su orden**) y los campos de cada respuesta.
2. **Interfaz y envoltorio** — `ws/XCliente.java` (una operación por cada una que use el cliente, con los mismos nombres del servidor y DTO propios como retorno) y `ws/XClienteWS.java`, que arma la
   petición con `ClienteSoapHttp` y copia la respuesta al DTO leyendo por nombre local con `SoapXml`:
   ```java
   public class XClienteWS implements XCliente {
       private final ClienteSoapHttp soap;
       public XClienteWS(String urlServicio) { soap = new ClienteSoapHttp(urlServicio, "XServicio"); }

       @Override public RespuestaItemsDto obtenerItems(String codigo) {
           Element r = soap.enviar("obtenerItems", SoapXml.parametros("codigo", codigo));
           RespuestaItemsDto dto = new RespuestaItemsDto();
           SoapXml.llenarBase(dto, r);
           dto.setItems(SoapXml.lista(r, "items", "item"));      // lista de textos; para objetos con campos: SoapXml.elementos(...)
           return dto;
       }
   }
   ```
   `enviar` ya califica operación y parámetros con el TNS (R3), envía la `SOAPAction` con URI (R4), usa la URL completa tal cual (R9) y traduce red/Fault a `RED` y respuesta ilegible a `SRV-000`.
   Se crea en `AplicacionCliente`: `new XClienteWS(getString(R.string.url_servicio))`.
3. **DTO propio** — `…Dto` en `dto` que extienda `RespuestaBaseDto`; POJO con los campos de la respuesta. **Todos los campos son texto.**
4. **Servicio** — `servicios/XServicio extends ServicioBase`: valida lo trivial (campos vacíos), convierte los datos al formato del contrato (coma decimal → punto) y llama:
   `return verificar(cliente.obtenerItems(codigo));`. Las reglas del dominio las decide el servidor; el cliente muestra su `mensaje`. Se crea en `AplicacionCliente` y se pasa a la fábrica.
5. **Controlador** — `XControlador extends ControladorBase<Datos>` (los datos de la pantalla: lo escrito, el resultado, el destino de navegación). Valida los campos vacíos en el orden de la pantalla con
   `rechazar(Textos.campoObligatorio(...))` y llama al servicio **con `ejecutar(textoOcupado, tarea, alExito)`**, que usa el `Executor` inyectado (nunca en el hilo principal), publica el estado «ocupado», ignora una
   segunda acción y muestra el error como mensaje. Un proyecto con login sobrescribe `manejar` para tratar el código de sesión no válida de su servidor. Registrarlo en `FabricaControladores.create(...)`.
   Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar campos vacíos antes de llamar; mostrar `mensaje`, nunca el código; conservar los datos ante un error salvo los sensibles (claves, que la vista limpia);
   estado «ocupado» que deshabilita los controles; no conocer la pantalla siguiente (la navegación es un dato del estado que consume la actividad).
6. **Pantalla** — layout con los estilos (§5), `Activity` que pide su controlador a la fábrica (`new ViewModelProvider(this, app.getFabrica())`), observa el estado (dibuja `ocupado`, `mensaje` con `error`, `datos`) y delega los eventos.
   Los textos salen de `Textos`, no de `strings.xml`. Declarar la `Activity` en `AndroidManifest.xml` (`exported="false"`).
7. **Pruebas** — unitarias, sin servidor ni emulador (`docs/CONVENCION_PRUEBAS.md`): servicio con un doble de `XCliente`, controlador con un doble del servicio y un `Executor` sincrónico (`Runnable::run`) o manual
   (ver `ControladorBaseTest`), y la lectura de `XClienteWS`/`SoapXml` con el **XML real** del servidor copiado de `curl`.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- Si el proyecto tiene login: la sesión vive solo en memoria (`modelo`), nunca en disco ni en logs; las credenciales se envían según el contrato del servidor.
- Todo dato viaja como **texto**; los decimales con **punto**.
- Mostrar solo el **mensaje**, nunca el código. Textos con tildes, centralizados en `Textos` y **los mismos en todos los clientes** del proyecto.
- **Nunca red en el hilo principal.** Los fallos de red no cierran la app.
- Al enviar un formulario, ocultar el teclado (`Teclado.ocultar(this)`): con el teclado abierto tapa la zona de estado.
- Las pruebas manuales del proyecto se agregan al paquete `pruebas` (§9).

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Los layouts **solo referencian estilos**; márgenes laterales de `24dp` en el layout. Componentes disponibles:

| Estilo | Para qué |
|---|---|
| `Boton.Primario` | Acción principal de la pantalla (relleno `primario`, texto blanco) |
| `Boton.Secundario` | Salida o regreso: «Cerrar sesión», «Cancelar» (sólido `secundario`) |
| `Panel`, `Panel.Titulo`, `Panel.Resultado` | Mensaje, encabezado de pantalla y resultado |
| `Etiqueta`, `Campo`, `Selector` | Etiqueta de campo, campo de texto y lista desplegable (`item_spinner.xml` para el adaptador) |
| `ProgressBar` con `@color/acento` | Indicador de carga; deshabilitar los controles mientras dura |
| `zona_estado` + `ZonaEstado` | Zona de estado bajo los botones: indicador de ocupado, error o éxito, de uno en uno (`mostrar(ocupado, mensaje, error)`) |

- **Fondo:** `res/drawable-nodpi/fondo_app.jpeg` (opcional; imagen de referencia, recortada a pantalla completa) sobre el color `fondo` de respaldo. Es una imagen **de ejemplo**:
  sustituirla o quitar el `ImageView` de los layouts según el proyecto (no se usa imagen en la presentación clara).
- **Presentación clara:** redefinir los mismos nombres de `colors.xml` con los valores de `CLIENT_STYLES_JAVA.md` §2.3. No hay selector de tema.
- `fitsSystemWindows` reemplaza el `padding` del mismo `View`: usar márgenes en los hijos.
- Los recursos de la biblioteca que las pantallas de ejemplo no usan están exentos del aviso `UnusedResources` en `app/lint.xml`.
- El móvil es de una columna y no usa la disposición dividida ni los componentes exclusivos del escritorio (cuadrícula de acciones, tabla de ancho completo); si un resultado necesita una tarjeta o una tabla, se construye con `Panel`/`Panel.Resultado`.
- Antes de construir las pantallas de un proyecto conviene especificarlas en su `docs/PANTALLAS.md` (orden de los elementos, estados y decisiones); la del cliente de escritorio de EurekaBank sirve de modelo.

## 6. Servidor, emulador y teléfono real

| Entorno | Host en `res/values/config.xml` (`url_servicio`) |
|---|---|
| Emulador | `10.0.2.2` (la PC anfitriona; **no** `localhost`) |
| Teléfono real | IP de la PC en la red local (`ipconfig` → IPv4), misma red Wi-Fi |

Para HTTP sin cifrar (Android 9+ lo bloquea): agregar el **mismo host** en `res/xml/network_security_config.xml` (hay una línea de ejemplo comentada) —sin esto la
app falla con `CLEARTEXT communication ... not permitted`—. El permiso `INTERNET` ya está en el manifiesto. Para el teléfono real además:
- Abrir el puerto del servidor en el firewall de Windows (ejemplo para 8080, PowerShell como administrador):
  `netsh advfirewall firewall add rule name="Servidor 8080" dir=in action=allow protocol=TCP localport=8080`
- Probar primero desde el navegador del teléfono `http://<IP de la PC>:<puerto>/<contexto>/<Servicio>?wsdl`: si carga el XML, la app también conectará.
- La IP cambia al cambiar de red: repetir los cambios de `url_servicio` y `network_security_config.xml` y reinstalar.

Con el servidor apagado la app abre y el fallo llega al **llamar** una operación, donde `ClienteSoapHttp` lo traduce a `RED` («Servidor no disponible, intente más tarde»).

## 7. Comandos y verificación

```
./gradlew testDebugUnitTest      # pruebas locales (sin emulador ni servidor)
./gradlew assembleDebug lintDebug    # compilar + lint
```
En Windows también `gradlew.bat`. Si Gradle no encuentra el SDK, abrir el proyecto una vez en Android Studio (crea `local.properties`) o definir `ANDROID_HOME`.

Probar una operación del servidor sin la app (el sobre es el mismo que arma `SoapXml`):
```
curl -s -H "Content-Type: text/xml; charset=utf-8" -H 'SOAPAction: "http://ws.monster.edu.ec/NOMBRE_SERVICIO/OPERACION"' -d '<?xml version="1.0" encoding="UTF-8"?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><ns:OPERACION xmlns:ns="http://ws.monster.edu.ec/"><ns:PARAMETRO>valor</ns:PARAMETRO></ns:OPERACION></S:Body></S:Envelope>' URL_SERVICIO
```
Copiar las respuestas reales (OK, error de negocio, `Fault`) como datos de `SoapXmlTest`: así un nombre de elemento mal escrito falla en la prueba y no en el teléfono.

Un agente que no pueda ejecutar el emulador debe decir explícitamente que **solo verificó compilación, lint y pruebas locales** y que no vio la app real.

## 8. Qué asume la plantilla del servidor

Si el servidor no cumple alguno de estos puntos, el cambio se limita a `ws` y `dto` (`docs/CONVENCION_SOAP.md`):

- SOAP **1.1** por HTTP POST, `Content-Type: text/xml; charset=utf-8`, estilo *document/literal wrapped*.
- **Operación y parámetros calificados** con el TNS fijo `http://ws.monster.edu.ec/` (R2, R3; prefijo `ns`, hecho por `SoapXml.construirPeticion`). La respuesta se lee por nombre local, así que acepta campos con prefijo o con namespace por defecto.
- **SOAPAction = `TNS + Servicio + "/" + operación`**, entre comillas (`SoapXml.accionSoap`, la envía `ClienteSoapHttp`). Un servidor .NET rechaza la acción vacía con HTTP 500 *ActionNotSupported* (R4).
- **La URL completa del servicio** se recibe por configuración (`url_servicio`) y se usa tal cual: la app no asume puerto, binding ni nombre de puerto (R9, R10). El WSDL no se descarga.
- Cada respuesta es `<{operacion}Response><respuesta>…</respuesta></{operacion}Response>` y `respuesta` contiene `estado` (`OK`/`ERROR`), `codigo` y `mensaje`,
  más los datos de la operación (`SoapXml.ELEMENTO_RESPUESTA`).
- Todo dato viaja como texto (R7); los errores de negocio van en el cuerpo con HTTP 200 (R8); un `Fault`, HTTP distinto de 200/500 o la falta de respuesta son `RED`; un cuerpo vacío o que no es el XML esperado es `SRV-000`.
- Si el servidor usa **cabecera SOAP** para credenciales, WS-Security u otro estilo, ampliar `ClienteSoapHttp` / `SoapXml.construirPeticion`.
- Para un servicio **REST** se reutiliza todo salvo `ws/` y `dto/` (`servicios` solo depende de la interfaz del cliente).

## 9. Pruebas manuales (`ec.edu.monster.pruebas`)

Requisito de todos los proyectos (`JAVA_TESTS_CONVENTIONS.md`): clases con `main` que replican métodos JUnit de `src/test`, hasta **15 en total** (`min(15, JUnit significativos)`). La plantilla trae el andamiaje en `app/src/main/java/ec/edu/monster/pruebas/`:

| Clase | Para qué |
|---|---|
| `Verificador` | Sustituto de las aserciones de JUnit (`igual`, `verdadero`, `falso`, `lanza`); corre cada prueba sin detener a las demás e imprime `[OK]`/`[FALLO]` |
| `PruebasWs` | 5 réplicas de `TraductorRedTest` y `SoapXmlTest` (traducción de red, Fault, respuesta ilegible, armado de la petición); no dependen del dominio y quedan como parte de las 15 |
| `PruebasTodas` | Las ejecuta juntas, numeradas de corrido; el proyecto agrega aquí sus clases |

Para el dominio: crear `PruebasServicio` (con dobles de las interfaces `XCliente`, que registran cada llamada) y agregarla a `PruebasTodas`; cada clase expone `static Verificador ejecutar(int primera)` y el Javadoc de cada prueba nombra el método JUnit que replica. **Solo Java puro** (sin `android.*`) y sin red ni servidor: los controladores usan `LiveData` y no corren sin el entorno de Android, así que no tienen prueba manual. Se ejecutan con *Run File* sobre la clase en Android Studio. Las `[Fatal Error]` del parser XML que aparecen en la salida de `PruebasWs` son la salida de error esperada ante los cuerpos inválidos que la prueba envía a propósito.

## 10. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `CLEARTEXT communication … not permitted` | Falta el host en `res/xml/network_security_config.xml` (§6) |
| La app no conecta desde el emulador | Usar `10.0.2.2`, no `localhost`; el servidor debe estar en marcha en la PC |
| El mensaje de error no se ve al enviar un formulario | El teclado lo tapa: llamar a `Teclado.ocultar(this)` al enviar |
| Lint: `Missing accessibility label` / `LabelFor` en un layout | Las etiquetas y los `hint` se ponen desde `Textos` (en código), no en el XML: agregar el layout a la excepción `LabelFor` de `app/lint.xml` |
| Lint: `SetTextI18n` / `DefaultLocale` | No concatenar textos en `setText`: agregar un método con `String.format` a `Textos`, y usar `Locale.ROOT` en conversiones y formatos de números |
| `Unresolved reference` / no existe `ActivityXBinding` | ViewBinding genera la clase desde el nombre del layout (`activity_x.xml` → `ActivityXBinding`); sincronizar Gradle |
| El emulador muestra un diálogo «Try out your stylus» y no acepta texto desde `adb` | Desactivar el lápiz de Gboard: `adb shell settings put secure stylus_handwriting_enabled 0` |
| Gradle no encuentra el SDK | Definir `ANDROID_HOME` o abrir el proyecto una vez en Android Studio (crea `local.properties`) |
