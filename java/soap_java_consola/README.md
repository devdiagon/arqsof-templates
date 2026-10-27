# Plantilla: cliente de consola Java para servicios SOAP

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, los tipos comunes, la verificación de la envoltura
y la entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1). Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java 21. **Interfaz:** texto por la consola (`Scanner` / `System.out`), sin librerías de terceros. **Arquitectura:** capas, sin frameworks.
- **Transporte:** JAX-WS (`jaxws-rt` 4.0.3) con **proxy generado**: `wsimport` (`jaxws-maven-plugin`) genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto, y `ws/ConfiguracionServicio` solo fija la URL completa y los timeouts. El WSDL **no se descarga en ejecución**.
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (copia en `docs/`, con el TNS fijo `http://ws.monster.edu.ec/` de R2): así entiende a un servidor Java o .NET que la cumpla.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su propio login y su propia sesión (en `modelo`); la plantilla no trae ninguno (§8).
- **Verificada:** `mvn test` → BUILD SUCCESS, 22 pruebas JUnit en verde: 7 de configuración de la URL (3 de ellas para varios servicios), 5 de **conformidad** con la convención (contra un servidor falso local que la cumple y un
  cliente que es un proxy generado desde un WSDL de ejemplo: parámetros y campos de la respuesta calificados en el XML, SOAPAction con URI, respuesta entendida, URL completa),
  5 de la verificación de la envoltura (`ServicioBase`) y 5 de la traducción de fallos de red (`TraductorRed`).
  **No se ha ejecutado:** el cliente contra el servidor real del dominio, ni la secuencia de pantallas (la plantilla no trae ninguna).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId`; opcionalmente agregar `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**: es la raíz y cada capa es un paquete directo bajo ella (`ec.edu.monster.servicios`, `ec.edu.monster.ws`…), sin paquete intermedio de módulo.
4. **Configurar el servidor:** en `ws/ConfiguracionServicio.java`, `URL_POR_DEFECTO` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor).
   Con el servidor **ya actualizado y desplegado**, copiar su WSDL y su `.xsd` a `src/main/resources/wsdl/` y generar el proxy (§4, paso 1). El namespace y el nombre del servicio salen del WSDL; no hay nombre de puerto ni «base + servicio»: ver §8.
   **Si el servidor publica varios servicios** (cada uno con su WSDL y su **URL completa**; R9 prohíbe asumir «base + nombre del servicio»):
   - Copiar cada WSDL con su `.xsd` a `src/main/resources/wsdl/` y agregar **una ejecución de `wsimport` por servicio, cada una con su propio `packageName`**
     (p. ej. `ec.edu.monster.ws.generado.consulta`). Los WSDL de un mismo servidor suelen definir los mismos tipos (como `RespuestaBase`) en el mismo namespace:
     en un paquete común las clases generadas chocan.
   - En `ConfiguracionServicio`, una constante con la URL completa de cada servicio y un método que la lea con `urlConfigurada(servicio, urlPorDefecto)`:
     ```java
     public static final String URL_CONSULTA = "http://localhost:8080/Contexto/Consulta";
     public static String urlConsulta() { return urlConfigurada("consulta", URL_CONSULTA); }
     ```
   - Cada servicio se sobrescribe sin recompilar con `-Dws.url.consulta=...` o la variable `WS_URL_CONSULTA` (el nombre del servicio, en mayúsculas). Un servicio .NET con
     ruta libre se configura igual que uno Java: es solo otra URL completa.
   - Cada `XClienteWS.crear()` pide su puerto a su propia clase generada y lo configura con la URL de su servicio:
     `ConfiguracionServicio.configurar(new Consulta_Service().getConsultaPort(), ConfiguracionServicio.urlConsulta())`.
   - Los nombres de los servicios y de sus URLs se registran en el `docs/PROJECT.md` del proyecto.
5. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
6. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar: `mvn compile exec:java`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración del proxy** | `ws/ConfiguracionServicio` (`configurar`: URL completa y timeouts, sin nada del dominio; `urlConfigurada(servicio, urlPorDefecto)` para varios servicios) | `URL_POR_DEFECTO` siempre (o una constante por servicio, §1 paso 4); el resto no |
| **Traducción de fallos de red** | `ws/TraductorRed` (`WebServiceException` → `ExcepcionServicio` con código `RED`) | No |
| **Proxy generado** | `src/main/resources/wsdl/` (copia del WSDL y del `.xsd` del servidor) → `wsimport` en `pom.xml` → `target/generated-sources` | **Se copia** por proyecto y se descomenta la ejecución del plugin (§4, paso 1) |
| **Interfaz y envoltorio** | — (`ws/XCliente.java` y `ws/XClienteWS.java`) | **Se crean** por proyecto, una firma por operación del servidor (§4, paso 2) |
| **DTO base** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`, `esOk()`) | No; se agregan DTO del dominio al lado |
| **Verificación de la envoltura** | `servicios/ServicioBase` (`verificar`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (código + mensaje; códigos propios `RED` y `SRV-000`) | No |
| **Entrada/salida** | `vista/VistaBase` (`pedir`, `elegir`, `pausar`, `mostrarTitulo`, `mostrarPortada`, `mostrar`, `mostrarError`) y `vista/Textos` | `VistaBase` no; las vistas del dominio la extienden y `Textos` crece con los textos del dominio |
| **Composición** | `Main` (UTF-8 en la salida, manejo de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas, sesión** | `servicios/`, `controlador/`, `vista/`, `modelo/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (cliente SOAP, servicios, vistas, controladores) y bucle principal
├── modelo/                     datos propios del cliente que no son de una pantalla (p. ej. la sesión, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio                       código + mensaje; servidorNoDisponible(), respuestaIlegible()
├── dto/           RespuestaBaseDto (+ los del dominio)    POJOs sin anotaciones, todos los campos como texto: lo que ve el resto del cliente
├── servicios/     ServicioBase (+ los del dominio)        verifican la envoltura; única capa que conoce dto y la INTERFAZ del cliente
├── ws/            ConfiguracionServicio, TraductorRed (+ XCliente y XClienteWS del dominio)   el proxy generado vive en target/ (ec.edu.monster.ws.generado)
├── controlador/                                           coordinan vista y servicio; devuelven un Resultado
└── vista/         VistaBase, Textos (+ las del dominio)   única capa que lee el teclado y escribe en la consola
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor externo), misma estructura; ws/ConvencionClienteTest es un ejemplo ejecutable de prueba de conformidad
src/test/resources/wsdl/               WSEjemplo.wsdl + .xsd: WSDL de ejemplo del que wsimport-test genera el proxy de las pruebas (ec.edu.monster.ws.ejemplo)
docs/                                  documentos de referencia: CONVENCION_SOAP.md (copia vigente) y PROJECT.md (esqueleto del proyecto)
README.md                              esta guía: el único documento que queda en la raíz
```

Dependencias permitidas: `vista → controlador → servicios → ws`, y `modelo`, `dto` y `excepciones` son transversales (no dependen de nadie).
**La vista nunca importa `ws` ni `dto`**: recibe y devuelve texto y tipos simples; solo conoce a su controlador y a `Textos`.
**`servicios` solo conoce la interfaz del cliente (`XCliente`), no su implementación**; solo la implementación de `ws` conoce el protocolo, los tipos generados y las excepciones de red.
Si el WSDL cambia solo se tocan `ws`, `dto` y `servicios`. **Solo la vista usa `System.in`/`System.out`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación SOAP y su pantalla de consola

Ejemplo: operación `obtenerItems()` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba
(modelo ejecutable en `src/test`: `WSEjemplo.wsdl` y `ConvencionClienteTest`):

1. **WSDL y proxy** — copiar el WSDL del servidor (`<URL>?wsdl`) y su `.xsd` a `src/main/resources/wsdl/`, dejar en el WSDL el `schemaLocation` que apunta al `.xsd` local
   y **descomentar** en `pom.xml` la ejecución `generar-proxy` (`wsdlFile` = el nombre del WSDL, `wsdlLocation` = `/wsdl/<nombre>.wsdl`). `mvn generate-sources` crea en
   `target/generated-sources/wsimport` el paquete `ec.edu.monster.ws.generado`: la interfaz del puerto, la clase de servicio (`X_Service`) y un tipo por cada respuesta. **El código generado no se edita ni se versiona.**
   Cuando el servidor cambia su contrato: desplegarlo, copiar el WSDL nuevo y repetir `mvn generate-sources`.
2. **Interfaz y envoltorio** — `ws/XCliente.java` (interfaz propia con las operaciones que usa el cliente, tipos de retorno = DTO propios) y `ws/XClienteWS.java`, que implementa la interfaz
   sobre el puerto generado, copia cada respuesta generada a un DTO propio y **traduce los fallos de red con `TraductorRed`**. Lo único que ven el servicio y las vistas es la interfaz:
   ```java
   public final class XClienteWS implements XCliente {
       private final WSServicio puerto;                       // interfaz del puerto generada por wsimport
       public static XCliente crear() {
           return new XClienteWS(ConfiguracionServicio.configurar(new WSServicio_Service().getWSServicioPort()));   // URL completa y timeouts
       }
       @Override public RespuestaListaDto obtenerItems() {
           return TraductorRed.llamar(() -> {
               RespuestaLista r = puerto.obtenerItems();
               if (r == null) { return null; }                 // ServicioBase la trata como SRV-000
               RespuestaListaDto dto = new RespuestaListaDto();
               dto.setEstado(r.getEstado()); dto.setCodigo(r.getCodigo()); dto.setMensaje(r.getMensaje());
               if (r.getElementos() != null) { dto.getElementos().addAll(r.getElementos().getElemento()); }
               return dto;
           });
       }
   }
   ```
   y en `Main`: `XCliente cliente = XClienteWS.crear();`. Los nombres exactos (`WSServicio`, `WSServicio_Service`, `getWSServicioPort`) los decide `wsimport` a partir del WSDL y solo se usan en este archivo.
3. **DTO propio** — clase `…Dto` en el paquete `dto` que extienda `RespuestaBaseDto`, como **POJO sin anotaciones**, con los mismos atributos que el tipo generado (todos texto):
   ```java
   public class RespuestaListaDto extends RespuestaBaseDto {
       protected List<String> elementos;
       public List<String> getElementos() { if (elementos == null) { elementos = new ArrayList<>(); } return elementos; }   // nunca nulo
   }
   ```
4. **Servicio** — `servicios/XServicio extends ServicioBase`:
   ```java
   public List<String> items() {
       return List.copyOf(verificar(cliente.obtenerItems()).getElementos());
   }
   ```
   - `verificar(...)` devuelve la respuesta si `esOk()`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor; una respuesta nula es `SRV-000`.
   - **No captura excepciones de red**: ya llegan traducidas a `RED` desde `ws`.
   - Aquí van las reglas **del cliente**: validar lo trivial (campo vacío), convertir los datos de pantalla al formato del contrato (texto, decimales con **punto**, fechas ISO-8601, cultura invariante). Las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
5. **Vista** — `vista/XVista extends VistaBase`: pide datos con `pedir(...)` / `elegir(...)`, muestra con `mostrar(...)` y `mostrarError(...)`, con los textos de `Textos`. Sin lógica de negocio ni llamadas al servidor.
6. **Controlador** — `controlador/XControlador`: valida campos vacíos **antes** de llamar (si faltan, no se llama al servicio), pide a la vista, llama al servicio, muestra el resultado o `e.getMessage()` (solo el mensaje, nunca el código). Devuelve un `enum Resultado` que indica el **siguiente menú** (el controlador no conoce las pantallas por nombre).
   Si `e.servidorNoDisponible()` se muestra el mensaje y no se cierra el programa. Cualquier otra excepción inesperada se muestra con el texto genérico de `Textos`.
7. **Composición** — en `Main`: crear el cliente (`XClienteWS`), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Todo `RuntimeException` no previsto se captura en el bucle (mensaje por `System.err`), nunca termina con una traza.
8. **Pruebas** — un doble de la interfaz propia (`XCliente` es una interfaz: sirve una lambda o `java.lang.reflect.Proxy`) para el servicio (OK, error de negocio, red, respuesta nula, coma enviada como punto);
   los controladores se prueban con una vista falsa (subclase de `VistaBase` que devuelve respuestas fijas), sin tocar la consola. Para el envoltorio, repetir el patrón de `ConvencionClienteTest` con la operación real.
   Mockito no está en el `pom.xml`: la arquitectura lo prevé, pero se agrega solo cuando una prueba lo necesite.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se vuelve al menú.

## 5. Convenciones de consola

- **Codificación:** `Main` fuerza UTF-8 en `System.out`/`System.err` y `VistaBase` lee el teclado como UTF-8. Las tildes y la «ñ» salen bien solo si la terminal también está en UTF-8 (`chcp 65001` en cmd; PowerShell y Windows Terminal suelen estarlo).
- **Menús:** listas numeradas con `elegir(titulo, opciones)` (o `elegir(titulo, opciones, textoCero)` para rotular el 0, p. ej. «Salir»): devuelve el índice (base 0) o `-1` si se elige `0`. «0» es siempre cancelar / volver / salir. `pausar()` espera Enter; `mostrarTitulo(...)` imprime un título subrayado.
- **Portada:** `Textos.PORTADA` guarda el arte ASCII de arranque (vacío por defecto); `Main` llama una vez a `mostrarPortada()` de cualquier vista antes del bucle.
- **Errores:** se muestran con `mostrarError(...)` (prefijo `[!] `) y solo con el mensaje. Una entrada inválida se vuelve a pedir; no se lanza excepción.
- **Datos pedidos:** `pedir(etiqueta)` devuelve el texto ya recortado (`trim`); validar lo trivial (vacío, número) en el servicio o el controlador antes de llamar al servidor.
- **Claves:** `pedir(...)` hace eco del texto. `System.console().readPassword()` las oculta, pero solo existe en una terminal real (no en el IDE); si un proyecto decide mostrarlas en texto plano, lo registra en su `docs/PROJECT.md`. Nunca imprimir ni registrar la clave fuera de lo que la pantalla muestra al digitar.
- **Entrada cerrada:** `pedir` y `pausar` lanzan `NoSuchElementException` al terminar la entrada (Ctrl+Z / Ctrl+D o `stdin` agotado), así un bucle de menú no gira sin fin. `Main` la captura y termina el programa en silencio.
- **Esperas:** la llamada al servidor bloquea la consola (hasta el timeout de 10 s); mostrar «Consultando…» antes de llamar si la operación puede tardar.
- **Salida:** presentar el resultado en pocas líneas, con el mismo formato y los mismos textos que los demás clientes del proyecto (escritorio, web, móvil), tomados de `Textos`.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionServicio.URL_POR_DEFECTO` (URL completa). Sin recompilar: `-Dws.url=http://host:8080/Contexto/Servicio` o la variable de entorno `WS_URL`. Con varios servicios, uno por servicio: `-Dws.url.<servicio>=...` o `WS_URL_<SERVICIO>` (§1, paso 4).
- **El WSDL va empaquetado** (`wsdlLocation` en el classpath): crear el proxy no abre ninguna conexión y funciona con el servidor apagado. El fallo llega al **llamar** una operación (`WebServiceException`), que `TraductorRed` traduce a `RED`: el programa muestra «Servidor no disponible, intente más tarde» en vez de la traza.
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`). La consola de NetBeans no es una terminal: no hay `System.console()`.
- **Terminal:** `mvn compile exec:java` (`exec:java` no compila); con otro servidor: `mvn compile exec:java -Dws.url=http://192.168.1.10:8080/Contexto/Servicio`. El `exec-maven-plugin` se **mantiene declarado** en el `pom.xml` de los clientes: es la forma sencilla de ejecutarlos fuera del IDE.
- **Jar ejecutable:** no incluido. Para entregar un jar único agregar `maven-shade-plugin` con `ManifestResourceTransformer` (clase principal `ec.edu.monster.Main`) y `ServicesResourceTransformer`
  (JAX-WS descubre sus implementaciones por `META-INF/services`); **probarlo** antes de darlo por bueno.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor)
mvn compile exec:java        # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con el programa en marcha (las vistas y los controladores se prueban a mano o con vistas falsas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor y se vuelve al menú.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y el programa sigue respondiendo.
4. Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión.
5. Tildes y «ñ» en entrada y salida; entrada vacía y opción inválida en los menús.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_SOAP.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `servicios`.
Lo que **el código ya cumple** y lo que **debe cumplir el servidor**:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): lo declara el XSD del servidor (`elementFormDefault="qualified"`) y el proxy generado lo respeta.
  Si el WSDL copiado no lo declara, los campos llegan `null` sin error (R3).
- **Namespace (TNS) fijo `http://ws.monster.edu.ec/`** en todos los servicios del equipo (R2).
- **SOAPAction = `TNS + Servicio + "/" + operación`**: viene en el `soapAction` de cada operación del WSDL y el proxy la envía. Un servidor .NET rechaza la acción vacía con HTTP 500 *ActionNotSupported* (R4).
- **El puerto se pide a la clase de servicio generada** (`new X_Service().getXPort()`): el cliente no depende del nombre del puerto ni del binding, que varían entre Java y .NET (R10).
- **La URL completa del servicio** se recibe por configuración (`ConfiguracionServicio.configurar`) y sustituye a la dirección del WSDL; el cliente no asume puerto ni «base + servicio» (R9). El namespace y el nombre del servicio salen del WSDL.
- Cada respuesta es un elemento `respuesta` (`@WebResult(name = "respuesta")`) con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación. Los campos propios de la respuesta nunca se llaman `estado`, `codigo` ni `mensaje`.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R7); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R8);
  los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5). Si el servidor cambia su contrato hay que actualizar el contrato y los DTO del cliente.
- Si el WSDL viene de .NET con campos `nillable`, `wsimport` genera `JAXBElement<String>` en lugar de `String`: agregar un archivo de binding con `generateElementProperty="false"` (`<bindingFiles>` del plugin; docs/CONVENCION_SOAP.md §4).
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (token, credenciales en cada operación, nada) lo define cada servidor y su cliente lo implementa en `servicios`, con la sesión en `modelo`.
- Para un servicio **REST** usar la plantilla `rest_java_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `WebServiceException` al llamar una operación | Servidor apagado o `URL_POR_DEFECTO` distinta de la real. Abrir `<URL>?wsdl` en el navegador. Con `TraductorRed` el usuario ve «Servidor no disponible» |
| Error al ejecutar `generate-sources` | El WSDL no está en `src/main/resources/wsdl`, el `.xsd` no está junto a él o su `schemaLocation` no apunta al archivo local |
| Cambió el servidor y el cliente no ve la operación nueva | Copiar de nuevo el WSDL y el `.xsd`, `mvn generate-sources` y agregar la operación a la interfaz y al envoltorio (§4) |
| Respuesta con campos vacíos | Se copió un WSDL antiguo, o el envoltorio no copia un campo de la respuesta generada al DTO propio |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | El WSDL copiado no trae el `soapAction` de la operación (se envía `SOAPAction ""`): copiar el WSDL del servidor actualizado y regenerar |
| El servidor no recibe los parámetros (llegan vacíos) | El WSDL/XSD copiado no califica los elementos (`elementFormDefault="qualified"`): copiar el WSDL del servidor actualizado |
| `ClassNotFoundException` / «Provider … not found» de JAX-WS | Falta `jaxws-rt` en el `pom.xml` (el JDK 21 ya no incluye JAX-WS); si se empaqueta un jar, falta `ServicesResourceTransformer` |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó el `PrintStream` UTF-8 de `Main` |
| El menú repite «Opción inválida» sin parar | Ya no debería ocurrir: al cerrarse la entrada el programa termina (ver §5, «Entrada cerrada») |
| `System.console()` es `null` | Se ejecuta desde el IDE o con la entrada redirigida: usar `pedir(...)` como alternativa |
| Errores de compilación por clases duplicadas (`RespuestaBase`…) o proxies que se pisan | Varios WSDL generados en el mismo paquete: darle a cada ejecución de `wsimport` su propio `packageName` (§1, paso 4) |
