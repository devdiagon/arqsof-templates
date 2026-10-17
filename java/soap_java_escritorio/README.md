# Plantilla: cliente de escritorio Java (Swing) para servicios SOAP

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita y deja
claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java 21. **Interfaz:** Swing + FlatLaf, vistas **escritas a código** (sin editor visual). **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** JAX-WS (`jaxws-rt` 4.0.3): el contrato es una interfaz Java y el proxy se genera en ejecución a partir del WSDL. No se generan clases con `wsimport`.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida).
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla.
- **Verificada:**
  - `mvn test` → BUILD SUCCESS, **23 pruebas JUnit en verde** (servicio, login, inicio, ejecutor asíncrono, configuración de la URL y 5 de **conformidad** con la convención),
    sin servidor externo ni pantalla. Las de conformidad usan un servidor falso local que cumple la convención y miden el XML real: parámetros y campos de la respuesta calificados,
    SOAPAction con URI, respuesta entendida, URL completa. Sin `dto/package-info.java` una de ellas falla.
  - Antes de adaptarla a la convención se probó contra un servidor real (login correcto/incorrecto, navegación y «Cerrar sesión», pantallas **renderizadas sin mostrar la ventana**).
    Tras la adaptación **no se ha repetido** contra un servidor real.
  - **No se ha ejecutado:** la ventana real abierta en pantalla, una operación de dominio distinta del login ni el empaquetado en un jar ejecutable (no incluido en la plantilla).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor** en un único archivo: `src/main/java/ec/edu/monster/ws/ConfiguracionServicio.java`
   (`URL_POR_DEFECTO`, `NOMBRE_SERVICIO`, `NAMESPACE`). `URL_POR_DEFECTO` es la **URL completa del servicio** (host, puerto y ruta, sin `?wsdl` ni «/» final);
   los otros dos salen del WSDL (`<URL>?wsdl`). No hay nombre de puerto ni «base + servicio»: ver §8.
5. **Poner el nombre de la aplicación** en `vista/Textos.java` (título de la ventana y de la pantalla de acceso) y, si se quiere, **sustituir la imagen**
   `src/main/resources/ec/edu/monster/estaticos/fondo_app.jpeg` (es una imagen de ejemplo; ver `docs/CLIENT_STYLES_JAVA.md` §6 para los requisitos de la imagen).
6. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
7. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar: `mvn compile exec:java`. El login debe funcionar tal cual.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Login (compartido)** | `vista/LoginVista`, `controlador/LoginControlador`, `servicio/ServicioAutenticacion`, `dto/RespuestaLoginDto` | Solo la pantalla de destino (en `Main`) y, si el servidor difiere, la operación `login` |
| **Núcleo de servicios** | `servicio/ServicioBase` (sesión, `token()`, `llamar(...)`, `verificar(...)`) | No; los servicios del dominio la extienden |
| **Sesión y errores** | `modelo/Sesion`, `excepciones/ExcepcionServicio` | No |
| **Transporte SOAP** | `ws/ClienteWS`, `ws/ConfiguracionServicio`, `ws/ContratoWS` | `ConfiguracionServicio` siempre; `ContratoWS` para **agregar operaciones** |
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaListaDto`, `dto/package-info.java` (elementos calificados) | Se agregan DTO del dominio al lado; **`package-info.java` no se borra** |
| **Asincronía** | `controlador/EjecutorAsincrono` | No |
| **Composición y navegación** | `Main`, `vista/Pantalla`, `vista/VentanaPrincipal` | Se agregan pantallas, servicios y controladores del dominio |
| **Estilos** | `vista/estilo/*` (`Tema`, `EstilosUi`, `PanelMensaje`, `PanelRedondeado`, `PanelImagen`, `IndicadorProgreso`), `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos comunes** | `vista/Textos` | `NOMBRE_APLICACION` |
| **Pantalla de ejemplo (dominio)** | `vista/InicioVista`, `controlador/InicioControlador` | **Se reemplaza** por las pantallas del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/` |

Las zonas a adaptar están marcadas con el comentario **`DOMINIO`** en el código (`grep -rn "DOMINIO" src`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (Sesion, contrato, servicios, vistas, controladores) y navegación
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin()
├── dto/           package-info, RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto    POJOs con anotaciones JAXB
├── ws/            ConfiguracionServicio, ContratoWS, ClienteWS
├── servicio/      ServicioBase, ServicioAutenticacion     única capa que conoce ws y dto
├── controlador/   EjecutorAsincrono, LoginControlador, InicioControlador
└── vista/         Pantalla, Textos, VentanaPrincipal, LoginVista, InicioVista
    └── estilo/    Tema, EstilosUi, PanelRedondeado, PanelMensaje, PanelImagen, IndicadorProgreso
src/main/resources/ec/edu/monster/estaticos/   fondo_app.jpeg (imagen de ejemplo)
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor ni pantalla), misma estructura
docs/CLIENT_STYLES_JAVA.md              guía de estilos
```

Dependencias permitidas: `vista → controlador → servicio → ws/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `ws` ni `dto`.** Si el WSDL cambia solo se tocan `ws`, `dto` y `servicio`.
Excepción conocida: los DTO leen `ConfiguracionServicio.NAMESPACE` (para que el espacio de nombres se cambie en un solo lugar).

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems(token)` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Contrato** — `ws/ContratoWS.java` (los nombres son los del servidor y van **en su orden**). En **cada** operación van las tres declaraciones de la convención:
   `action` (SOAPAction) y `targetNamespace` en `@WebResult` y en cada `@WebParam` (el `login` ya existente es el modelo):
   ```java
   @WebMethod(operationName = "obtenerItems", action = ConfiguracionServicio.NAMESPACE + ConfiguracionServicio.NOMBRE_SERVICIO + "/obtenerItems")
   @WebResult(name = "respuesta", targetNamespace = ConfiguracionServicio.NAMESPACE)
   RespuestaListaDto obtenerItems(@WebParam(name = "token", targetNamespace = ConfiguracionServicio.NAMESPACE) String token);
   ```
2. **DTO** (si la respuesta no es login ni lista) — clase `…Dto` **en el paquete `dto`** que extienda `RespuestaBaseDto`, con los mismos atributos que el WSDL.
   Los elementos hijos **van calificados** con el namespace del servicio: lo logra `dto/package-info.java` (por eso el DTO debe estar en ese paquete):
   ```java
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "RespuestaDetalle", namespace = ConfiguracionServicio.NAMESPACE)   // name = el del complexType del WSDL
   public class RespuestaDetalleDto extends RespuestaBaseDto {
       protected String valor;
       public String getValor() { return valor; }
   }
   ```
   y registrarla en `@XmlSeeAlso({...})` de `RespuestaBaseDto`.
3. **Servicio** — `servicio/ServicioItems extends ServicioBase`:
   ```java
   public List<String> items() {
       return List.copyOf(verificar(llamar(() -> ws.obtenerItems(token()))).getElementos());   // token() lanza AUTH-002 si no hay sesión
   }
   ```
   Se crea en `Main` (`new ServicioItems(contrato, sesion)`; la `Sesion` es **la misma** que usa `ServicioAutenticacion`: obtenerla de ahí o crearla una vez en `Main` y pasarla a todos).
   Aquí van las reglas **del cliente** (campo vacío, formato); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
4. **Vista** — clase `XxxVista extends JPanel` **pasiva**, construida solo con `EstilosUi` y `Tema` (ver `InicioVista` y `LoginVista`): getters de lo escrito,
   métodos `alPulsarXxx(...)`, `mostrarError/mostrarResultado`, `setOcupado(boolean)`. Sin lógica, sin llamadas al servidor.
5. **Controlador** — molde: `LoginControlador`. Registra los eventos de la vista, llama al servicio **con `EjecutorAsincrono.ejecutar(tarea, alTerminar, alFallar)`** (nunca en el hilo de eventos),
   activa `setOcupado(true/false)`, muestra `error.getMessage()`, y si `error.exigeLogin()` avisa con un callback `Consumer<String> alSesionExpirada` para volver al login. La navegación se recibe como `Runnable`/`Consumer`.
6. **Pantalla** — agregar una constante a `vista/Pantalla`, y en `Main`: crear la vista, `ventana.agregar(Pantalla.X, vista)`, crear el controlador y conectar la navegación.
7. **Pruebas** — un doble del contrato (`ContratoWS` es una interfaz: sirve una lambda o `java.lang.reflect.Proxy`, ver `ServicioAutenticacionTest` y `LoginControladorTest`),
   pruebas del servicio y del controlador (con `SwingUtilities.invokeAndWait` y un `esperar(...)` como en `LoginControladorTest`).
   Por cada operación nueva, repetir el patrón de `ws/ConvencionClienteTest` (agregarla al servidor falso y comprobar parámetros calificados, SOAPAction y campos de la respuesta).

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED` (`ServicioBase.llamar`).
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Nunca red en el hilo de eventos.** Los fallos de red no cierran la aplicación.
- `EstilosUi.instalarLookAndFeel()` **antes** de crear cualquier componente.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Las vistas **solo usan la fábrica** `EstilosUi` y las constantes de `Tema`; no colorean ni dimensionan componentes sueltos. Componentes disponibles:

| Fábrica / clase | Para qué |
|---|---|
| `EstilosUi.botonPrimario` | Acción principal de la pantalla (relleno `primario`, texto blanco) |
| `EstilosUi.botonSecundario` | Salida o regreso: «Cerrar sesión», «Volver» (sólido `secundario`) |
| `EstilosUi.encabezadoAplicacion` / `encabezadoPantalla` | Título de la aplicación (acceso, centrado) y de pantalla |
| `EstilosUi.etiqueta`, `campoTexto`, `campoContrasenia`, `selector`, `marcarError` | Etiqueta de campo, campos, lista desplegable, borde de error |
| `PanelMensaje` (`mostrarError`, `mostrarExito`, `mostrarResultado`) | Mensaje y resultado; oculto hasta que hay texto |
| `IndicadorProgreso` (`setActivo`) | Indicador de carga; deshabilitar los controles mientras dura |

- **Disposición:** ventana dividida: pantalla a la izquierda (columna de 420, centrada) e imagen a la derecha (completa y centrada). Tamaño inicial 1100 × 720, mínimo 900 × 640 (`Tema`).
  La imagen es de la **ventana**, no de cada pantalla. Para no usarla, quitar el `PanelImagen` de `VentanaPrincipal`.
- **Presentación clara:** cambiar los valores de `Tema` por los de `docs/CLIENT_STYLES_JAVA.md` §2.3 y usar `FlatLightLaf`. No hay selector de tema.
- **Las imágenes** van en `src/main/resources` (Maven las copia al jar sin configuración adicional).
- FlatLaf no admite algunas propiedades de estilo en ciertos componentes (por ejemplo `minimumHeight` en campos de texto): una propiedad no admitida no falla al compilar: puede fallar al construir la pantalla; probar cada pantalla nueva y revisar la salida de consola.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionServicio.URL_POR_DEFECTO` (URL completa). Sin recompilar: `-Dws.url=http://host:8080/Contexto/Servicio` o la variable de entorno `WS_URL`.
- Si el servidor está apagado, `Main` muestra un diálogo de error y termina (no deja una ventana inservible). El WSDL se descarga **fuera** del hilo de eventos.
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`).
- **Consola:** `mvn compile exec:java`; con otro servidor: `mvn compile exec:java -Dws.url=http://192.168.1.10:8080/Contexto/Servicio`.
- **Jar ejecutable:** no incluido. Para entregar un jar único agregar `maven-shade-plugin` con `ManifestResourceTransformer` (clase principal `ec.edu.monster.Main`) y `ServicesResourceTransformer`
  (JAX-WS descubre sus implementaciones por `META-INF/services`); **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor ni pantalla)
mvn compile exec:java        # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, renders sin mostrar la ventana, contra qué servidor) y que **no vio la ventana real**.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `ServicioAutenticacion`:

Es la convención común de la familia (`CONVENCION_SOAP.md`); lo que **el código ya cumple** y lo que **debe cumplir el servidor**:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): en el cliente lo garantizan
  `dto/package-info.java` y los `targetNamespace` del contrato. Si faltan, los campos llegan `null` sin error (R3).
- **SOAPAction = `NAMESPACE + Servicio + "/" + operación`** (atributo `action` de cada `@WebMethod`; ayuda: `ConfiguracionServicio.accion(...)`).
  Un servidor .NET rechaza la acción vacía con HTTP 500 *ActionNotSupported* (R4).
- **El puerto se obtiene por la interfaz** (`getPort(ContratoWS.class)`, hecho en `ClienteWS`): el cliente no depende del nombre del puerto ni del binding, que varían entre Java y .NET (R10).
  `NOMBRE_SERVICIO` es a la vez el `<wsdl:service name>` y el portType del contrato.
- **La URL completa del servicio** se recibe por configuración; el cliente no asume puerto ni «base + servicio» (R9). `NAMESPACE` = `targetNamespace` del WSDL.
- Cada respuesta es un elemento `respuesta` (`@WebResult(name = "respuesta")`) con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación.
- **Lo que el código no puede imponer** (acuerdo con el servidor): todos los datos viajan como texto (R6); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R7);
  los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5/R8). Si el servidor cambia su contrato, actualizar contrato y DTO del cliente.
- Si el WSDL viene de .NET con campos `nillable`, `wsimport` genera `JAXBElement<String>`; esta plantilla no usa `wsimport`, por lo que no le afecta.
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (manejadores JAX-WS).
- Para un servicio **REST** reutilizar de esta plantilla todo salvo `ws/` y `dto/` (la capa `servicio` solo depende de `ContratoWS`).
