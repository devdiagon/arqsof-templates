# Plantilla: cliente de escritorio Java (Swing) para servicios SOAP

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Trae el transporte, los tipos comunes, la verificación de la envoltura,
la ventana principal con sus estilos y la ejecución asíncrona, y deja marcado lo que cambia con el **dominio**.
No incluye inicio de sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1) y `ESPECIFICACION_CLIENTES.md`. Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java 21. **Interfaz:** Swing + FlatLaf, vistas **escritas a código** (sin editor visual). **Arquitectura:** capas, sin frameworks.
- **Transporte:** JAX-WS (`jaxws-rt` 4.0.3) con **proxy generado**: `wsimport` (`jaxws-maven-plugin`) genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto;
  `ws/ConfiguracionServicio` fija la URL completa y los timeouts, y `ws/TraductorRed` traduce los fallos de red a `RED`. El WSDL **no se descarga en ejecución**.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida).
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (TNS fijo `http://ws.monster.edu.ec/`, R2). Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su login y su sesión (en `modelo`). La plantilla deja el punto de enganche: `InicioVista` (pantalla inicial)
  y `LoginVista`, una pantalla de acceso **pasiva** (sin controlador) lista para usar.
- **Verificada:** `mvn test` → BUILD SUCCESS, 27 pruebas JUnit en verde (ejecutor asíncrono, molde de controlador, configuración de la URL, verificación de la envoltura, traducción de red y
  5 de conformidad de ejemplo contra un servidor falso local). Las pantallas de la plantilla y los componentes nuevos (tarjeta, tabla, zona de estado) se vieron **renderizados fuera de pantalla**. **No se ha ejecutado:** la ventana real abierta en pantalla, el cliente contra un servidor real del dominio ni el empaquetado en un jar.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias.
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**; cada capa es un paquete directo bajo él.
4. **Configurar el servidor:** en `ws/ConfiguracionServicio.java`, `URL_POR_DEFECTO` = **URL completa del servicio** (sin `?wsdl`, sin «/» final).
   Con el servidor **ya desplegado**, copiar su WSDL y su `.xsd` a `src/main/resources/wsdl/` y generar el proxy (§4, paso 1).
   Si el servidor publica **varios servicios** (cada uno con su WSDL y su URL completa; R9): una ejecución de `wsimport` por servicio con su propio `packageName`
   (p. ej. `ec.edu.monster.ws.generado.consulta`) y, en `ConfiguracionServicio`, una constante y un método `urlConfigurada("consulta", URL_CONSULTA)` por servicio;
   se sobrescriben con `-Dws.url.consulta=...` o `WS_URL_CONSULTA`.
5. **Poner el nombre de la aplicación** en `vista/Textos.java` y, si se quiere, **sustituir la imagen** `src/main/resources/ec/edu/monster/estaticos/fondo_app.png`
   (requisitos en `docs/CLIENT_STYLES_JAVA.md` §6). La imagen es un **PNG** (admite transparencia); si se cambia por otra, con otro dibujo o proporciones, **se conserva como `fondo_app.png`** para no tocar el código ni los estilos. El tamaño con que se dibuja (60 % de su contenedor) se ajusta en `Tema.ESCALA_IMAGEN`.
6. **Completar `docs/PROJECT.md`** y mantenerlo al día. Retirar los ejemplos de conformidad (`ConvencionClienteTest`, `src/test/resources/wsdl/WSEjemplo.*` y la ejecución `generar-proxy-ejemplo` del `pom.xml`).
7. Ejecutar: `mvn compile exec:java`.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Núcleo de servicios** | `servicios/ServicioBase` (verifica la envoltura: no OK → `ExcepcionServicio`; nula → `SRV-000`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (códigos propios `RED` y `SRV-000`) | No |
| **Proxy generado** | `src/main/resources/wsdl/` → `wsimport` (`pom.xml`) → `target/generated-sources` (`ec.edu.monster.ws.generado`) | Se **agrega** el WSDL del servidor (§4, paso 1) |
| **Transporte SOAP** | `ws/ConfiguracionServicio`, `ws/TraductorRed` | `ConfiguracionServicio` siempre (URL); el resto no |
| **DTO base** | `dto/RespuestaBaseDto` (POJO sin anotaciones) | Se agregan DTO del dominio al lado |
| **Asincronía y molde de controlador** | `controlador/EjecutorAsincrono`, `controlador/ControladorBase` (`ejecutar`: segundo plano, estado «ocupado», segunda acción ignorada, error como mensaje) | No; los controladores del dominio la extienden |
| **Composición y navegación** | `Main`, `vista/Pantalla`, `vista/VentanaPrincipal` | Se agregan pantallas, servicios y controladores del dominio |
| **Estilos** | `vista/estilo/*` (`Tema`, `EstilosUi`, `PanelMensaje`, `ZonaEstado`, `PanelRedondeado`, `PanelImagen`, `IndicadorProgreso`, `TarjetaResultado`, `TablaDatos`, `FlechaDireccion`), `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se repiten valores en las vistas |
| **Textos** | `vista/Textos` | `NOMBRE_APLICACION` y los textos del dominio |
| **Base de las vistas** | `vista/VistaBase` (zona de estado, «ocupado» que deshabilita los controles, `Cancelar` con Esc, auxiliares de disposición) | No; las vistas del dominio la extienden |
| **Punto de enganche** | `vista/InicioVista` (pantalla inicial de ejemplo), `vista/LoginVista` (pasiva, sin controlador) | Se reemplazan/usan según el proyecto |
| **Dominio (vacío)** | `modelo/`, `controlador/` | Nuevos `dto/`, `servicios/`, `controlador/`, `vista/`, interfaz `XCliente` y envoltorio `XClienteWS` en `ws/` |

Las zonas a adaptar están marcadas con **`DOMINIO`** o **`TODO`** en el código (`grep -rn "DOMINIO\|TODO" src`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (cliente SOAP, servicios, vistas, controladores) y navegación
├── modelo/                     datos propios del cliente (p. ej. la sesión, si el proyecto tiene login)
├── excepciones/   ExcepcionServicio
├── dto/           RespuestaBaseDto                 POJOs sin anotaciones
├── servicios/     ServicioBase                     única capa que conoce la interfaz del cliente y los dto
├── ws/            ConfiguracionServicio, TraductorRed        + XCliente / XClienteWS por proyecto (envuelve el puerto generado)
├── controlador/   EjecutorAsincrono, ControladorBase        + un controlador por pantalla
└── vista/         Pantalla, Textos, VentanaPrincipal (dos modos), VistaBase, InicioVista, LoginVista
    └── estilo/    Tema, EstilosUi, PanelRedondeado, PanelMensaje, ZonaEstado, PanelImagen, IndicadorProgreso, TarjetaResultado, TablaDatos, FlechaDireccion
src/main/resources/wsdl/                copia del WSDL del servidor (la crea el proyecto)
src/main/resources/ec/edu/monster/estaticos/   fondo_app.png (imagen de ejemplo)
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor ni pantalla), misma estructura
docs/                                   CLIENT_STYLES_JAVA, CONVENCION_SOAP, CONVENCION_PRUEBAS, PROJECT
```

Dependencias: `vista → controlador → servicios → ws | dto`; `modelo`, `dto` y `excepciones` son transversales.
**La vista nunca importa `ws`.** Las clases generadas solo las conoce la implementación `XClienteWS`.

## 4. Receta: agregar una operación SOAP y su pantalla

Siempre en este orden, de abajo hacia arriba:

1. **WSDL y proxy** — con el servidor ya desplegado, copiar su WSDL (`<URL>?wsdl`) y su `.xsd` a `src/main/resources/wsdl/`, con el `schemaLocation` apuntando al `.xsd` local.
   En el `pom.xml`, **descomentar** la ejecución `generar-proxy` (ajustar nombres del WSDL) y ejecutar `mvn generate-sources`.
   **El código generado no se edita ni se versiona.** Si el contrato cambia: copiar el WSDL nuevo y regenerar.
2. **Interfaz y envoltorio** — `ws/XCliente.java` (una operación por cada una que use el cliente, con DTO propios) y `ws/XClienteWS.java`, que delega en el puerto generado,
   copia la respuesta al DTO y envuelve cada llamada con `TraductorRed.llamar(...)`:
   ```java
   @Override public RespuestaItemsDto obtenerItems(String codigo) {
       return TraductorRed.llamar(() -> convertir(puerto.obtenerItems(codigo)));
   }
   ```
   Se crea en `Main`: `new XClienteWS(ConfiguracionServicio.configurar(new WSX_Service().getWSXPort()))`.
3. **DTO propio** — `…Dto` en `dto` que extienda `RespuestaBaseDto`; POJO con los mismos atributos que el tipo generado. **Todos los campos son texto.**
4. **Servicio** — `servicios/XServicio extends ServicioBase`: valida lo trivial (campos vacíos), convierte los datos al formato del contrato (coma decimal → punto) y llama:
   `return verificar(cliente.obtenerItems(codigo));`. Las reglas del dominio las decide el servidor; el cliente muestra su `mensaje`.
5. **Vista** — `XxxVista extends VistaBase` **pasiva**, construida solo con `EstilosUi` y `Tema` (ver `InicioVista` y `LoginVista`): getters de lo escrito, métodos `alXxx(...)`, `marcarVacio(campo)` y, de `VistaBase`, la zona de estado, `setOcupado` y `alCancelar`. Los controles que se deshabilitan mientras espera se registran con `registrar(...)`.
6. **Controlador** — `extends ControladorBase`: registra los eventos de la vista, valida los campos vacíos en el orden de la pantalla y llama al servicio **con `ejecutar(textoOcupado, tarea, alExito)`**, que usa `EjecutorAsincrono` (nunca en el hilo de eventos), marca la pantalla «ocupada», ignora una segunda acción y muestra el error como mensaje. Un proyecto con login sobrescribe `manejar` para tratar el código de sesión no válida de su servidor.
   Invariantes (`ARQUITECTURA_CLIENTE.md` §4.6): validar campos vacíos antes de llamar; mostrar `error.getMessage()`, nunca el código; conservar los datos ante un error salvo los sensibles (claves);
   estado «ocupado» que deshabilita los controles e **ignora una nueva acción mientras dura**; no conocer la pantalla siguiente (la navegación llega como `Runnable`/`Consumer`).
7. **Pantalla** — constante en `vista/Pantalla` y, en `Main`: crear vista y controlador, `ventana.agregar(Pantalla.X, vista)` y conectar la navegación.
8. **Pruebas** — unitarias, sin servidor ni pantalla (`docs/CONVENCION_PRUEBAS.md`): servicio con un doble de `XCliente`, controlador con un doble del servicio
   (`SwingUtilities.invokeAndWait` y un `esperar(...)`), y la traducción a `RED`/`SRV-000` en `XClienteWS`.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED` (`TraductorRed`).
- Si el proyecto tiene login: la sesión vive solo en memoria (`modelo`), nunca en disco ni en logs; las credenciales se envían según el contrato del servidor.
- Todo dato viaja como **texto**; los decimales con **punto**.
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
- Antes de construir las pantallas de un proyecto conviene especificarlas en su `docs/PANTALLAS.md` (orden de los elementos, anchos, estados y decisiones); la del cliente de escritorio de EurekaBank sirve de modelo.

## 6. Servidor y ejecución

- **URL por defecto:** `ConfiguracionServicio.URL_POR_DEFECTO`. Sin recompilar: `-Dws.url=http://host:8080/Contexto/Servicio` o la variable `WS_URL`.
- El proxy usa el WSDL empaquetado: crear el cliente no abre ninguna conexión, así que con el servidor apagado la ventana abre y el fallo llega al **llamar** una operación,
  donde `TraductorRed` lo traduce a `RED` («Servidor no disponible, intente más tarde»).
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (`exec.mainClass`). **Consola:** `mvn compile exec:java`.
- **Jar ejecutable:** no incluido. Para entregarlo, `maven-shade-plugin` con `ManifestResourceTransformer` y `ServicesResourceTransformer` (JAX-WS descubre implementaciones por `META-INF/services`); **probarlo**.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor ni pantalla)
mvn compile exec:java        # ejecutar
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador. Un agente que no pueda abrir la ventana debe decir qué verificó y que **no vio la ventana real**.

## 8. Qué asume la plantilla del servidor

Si el servidor no cumple alguno de estos puntos, el cambio se limita a `ws` y `dto` (`docs/CONVENCION_SOAP.md`):

- SOAP 1.1, *document/literal wrapped*; elementos calificados con el TNS (R3: si el WSDL no lo declara, los campos llegan `null` sin error).
- **SOAPAction = `TNS + Servicio + "/" + operación`** (R4), que el proxy toma del WSDL.
- El puerto se pide a la clase de servicio generada (R10) y la URL completa de la configuración sustituye a la del WSDL (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación.
- Todo dato viaja como texto (R7); los errores de negocio van en el cuerpo con HTTP 200 (R8).
- Si el WSDL viene de .NET con `nillable`, agregar un binding con `generateElementProperty="false"` (`<bindingFiles>` del plugin).
- Para un servicio **REST** se reutiliza todo salvo `ws/` y `dto/`.
