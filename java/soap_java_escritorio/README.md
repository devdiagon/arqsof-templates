# Plantilla: cliente de escritorio Java (Swing) para servicios SOAP

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita y deja
claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java 21. **Interfaz:** Swing + FlatLaf, vistas **escritas a código** (sin editor visual). **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** JAX-WS (`jaxws-rt` 4.0.3): el contrato es una interfaz Java y el proxy se genera en ejecución a partir del WSDL. No se generan clases con `wsimport`.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida).
- **Verificada** al crearla:
  - `mvn test` → BUILD SUCCESS, **14 pruebas JUnit en verde** (servicio, login, inicio, ejecutor asíncrono), sin servidor ni pantalla.
  - Contra un **servidor real** (el del Conversor de Unidades, con una copia temporal que solo cambió las constantes de `ConfiguracionServicio`): login con credenciales
    incorrectas muestra el mensaje del servidor, login correcto navega a la pantalla de inicio y «Cerrar sesión» vuelve al login. Las pantallas se comprobaron **renderizándolas
    sin mostrar la ventana**.
  - **No se ha ejecutado:** la ventana real abierta en pantalla, una operación de dominio distinta del login (la receta de §4 está probada en su forma original en el cliente de escritorio del
    Conversor, no con un dominio nuevo) ni el empaquetado en un jar ejecutable (no incluido en la plantilla).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor** en un único archivo: `src/main/java/ec/edu/monster/ws/ConfiguracionServicio.java`
   (`BASE_POR_DEFECTO`, `NOMBRE_SERVICIO`, `NOMBRE_PUERTO`, `NAMESPACE`). Los cuatro valores salen del WSDL (`<URL>?wsdl`): ver §8.
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
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaListaDto` | Se agregan DTO del dominio al lado |
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
├── dto/           RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto    POJOs con anotaciones JAXB
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

1. **Contrato** — `ws/ContratoWS.java` (los nombres son los del servidor y van **en su orden**):
   ```java
   @WebMethod(operationName = "obtenerItems")
   @WebResult(name = "respuesta")
   RespuestaListaDto obtenerItems(@WebParam(name = "token") String token);
   ```
2. **DTO** (si la respuesta no es login ni lista) — clase `…Dto` que extienda `RespuestaBaseDto`, con los mismos atributos que el WSDL (los elementos hijos **no llevan namespace**):
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

- **Servidor por defecto:** `ConfiguracionServicio.BASE_POR_DEFECTO`. Sin recompilar: `-Dws.base.url=http://host:8080/Contexto` o la variable de entorno `WS_BASE_URL`.
- Si el servidor está apagado, `Main` muestra un diálogo de error y termina (no deja una ventana inservible). El WSDL se descarga **fuera** del hilo de eventos.
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`).
- **Consola:** `mvn compile exec:java`; con otro servidor: `mvn compile exec:java -Dws.base.url=http://192.168.1.10:8080/Contexto`.
- **Jar ejecutable:** no incluido. Para entregar un jar único agregar `maven-shade-plugin` con `ManifestResourceTransformer` (clase principal `ec.edu.monster.Main`) y `ServicesResourceTransformer`
  (JAX-WS descubre sus implementaciones por `META-INF/services`); **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor ni pantalla)
mvn compile exec:java        # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<BASE>/<NOMBRE_SERVICIO>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, renders sin mostrar la ventana, contra qué servidor) y que **no vio la ventana real**.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `ServicioAutenticacion`:

- SOAP **1.1** (JAX-WS), estilo *document/literal wrapped*, `SOAPAction ""`.
- **Valores de `ConfiguracionServicio` en el WSDL:** `NOMBRE_SERVICIO` = `<wsdl:service name="…">`; `NOMBRE_PUERTO` = `<wsdl:port name="…">`; `NAMESPACE` = `targetNamespace` del WSDL.
  Con JAX-WS por defecto, el nombre del servicio es el de la clase, el puerto es ese nombre + `Port` y el último tramo de la URL del endpoint coincide con el servicio (verificar: si la URL termina distinto, ajustar `ClienteWS.crear`).
- Los tipos de respuesta (DTO) están en el **mismo namespace** que el servicio (se comprueba en el `<xsd:schema targetNamespace>` del WSDL; un namespace distinto hace que los campos lleguen `null` sin error).
- Cada respuesta es un elemento `respuesta` (`@WebResult(name = "respuesta")`) con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación.
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (manejadores JAX-WS).
- Para un servicio **REST** reutilizar de esta plantilla todo salvo `ws/` y `dto/` (la capa `servicio` solo depende de `ContratoWS`).
