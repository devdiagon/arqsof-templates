# Plantilla: cliente de consola Java para servicios SOAP

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, los tipos comunes y la
entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

- **Lenguaje:** Java 21. **Interfaz:** texto por la consola (`Scanner` / `System.out`), sin librerías de terceros. **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** JAX-WS (`jaxws-rt` 4.0.3) con **proxy generado**: `wsimport` (`jaxws-maven-plugin`) genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto, y `ws/ClienteWS` solo fija la URL completa y los timeouts. El WSDL **no se descarga en ejecución**.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla.
- **Verificada:** `mvn test` → BUILD SUCCESS, 9 pruebas JUnit en verde: 4 de configuración de la URL y 5 de **conformidad** con la convención
  (contra un servidor falso local que la cumple y un cliente que es un proxy generado desde un WSDL de ejemplo: parámetros y campos de la respuesta calificados en el XML, SOAPAction con URI, respuesta entendida, URL completa).
  **No se ha ejecutado:** el cliente contra el servidor real del dominio, ni la secuencia de pantallas (la plantilla no trae ninguna).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId`; opcionalmente agregar `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor:** en `ws/ClienteWS.java`, `URL_POR_DEFECTO` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor).
   Con el servidor **ya actualizado y desplegado**, copiar su WSDL y su `.xsd` a `src/main/resources/wsdl/` y generar el proxy (§4, paso 1). El namespace y el nombre del servicio salen del WSDL; no hay nombre de puerto ni «base + servicio»: ver §8.
5. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
6. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar: `mvn compile exec:java`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Configuración del proxy** | `ws/ClienteWS` (`configurar`: URL completa y timeouts, sin nada del dominio) | `URL_POR_DEFECTO` siempre; el resto no |
| **Proxy generado** | `src/main/resources/wsdl/` (copia del WSDL y del `.xsd` del servidor) → `wsimport` en `pom.xml` → `target/generated-sources` | **Se copia** por proyecto y se descomenta la ejecución del plugin (§4, paso 1) |
| **Interfaz y envoltorio** | — (`ws/ClienteXxx.java` y `ws/ClienteWSXxx.java`) | **Se crean** por proyecto, una firma por operación del servidor (§4, paso 2) |
| **DTO base** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`, `esOk()`) | No; se agregan DTO del dominio al lado |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`exigeLogin()`, `servidorNoDisponible()`) | No |
| **Entrada/salida** | `vista/VistaBase` (`pedir`, `elegir`, `mostrar`, `mostrarError`) | No; las vistas del dominio la extienden |
| **Composición** | `Main` (UTF-8 en la salida, manejo de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas** | `servicio/`, `controlador/` (carpetas vacías, `.gitkeep`), `vista/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (Sesion, cliente SOAP, servicio, vistas, controladores) y bucle principal
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin(), servidorNoDisponible()
├── dto/           RespuestaBaseDto (+ los del dominio)    POJOs sin anotaciones: lo que ve el resto del cliente
├── ws/            ClienteWS (+ ClienteXxx y ClienteWSXxx del dominio)   configuración del proxy; el proxy generado vive en target/ (ec.edu.monster.ws.generado)
├── servicio/      (vacía)                                 única capa que conoce ws y dto
├── controlador/   (vacía)                                 coordinan vista y servicio; devuelven un Resultado
└── vista/         VistaBase (+ las del dominio)           única capa que lee el teclado y escribe en la consola
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor externo), misma estructura; ws/ConvencionClienteTest es un ejemplo ejecutable de prueba de conformidad
src/test/resources/wsdl/               WSEjemplo.wsdl + .xsd: WSDL de ejemplo del que wsimport-test genera el proxy de las pruebas (ec.edu.monster.ws.ejemplo)
```

Dependencias permitidas: `vista ← controlador → servicio → ws/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `ws` ni `dto`**: recibe y devuelve texto y tipos simples. Si el WSDL cambia solo se tocan `ws`, `dto` y `servicio`.
**Solo la vista usa `System.in`/`System.out`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación SOAP y su pantalla de consola

Ejemplo: operación `obtenerItems(token)` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba
(modelo ejecutable en `src/test`: `WSEjemplo.wsdl` y `ConvencionClienteTest`):

1. **WSDL y proxy** — copiar el WSDL del servidor (`<URL>?wsdl`) y su `.xsd` a `src/main/resources/wsdl/`, dejar en el WSDL el `schemaLocation` que apunta al `.xsd` local
   y **descomentar** en `pom.xml` la ejecución `generar-proxy` (`wsdlFile` = el nombre del WSDL, `wsdlLocation` = `/wsdl/<nombre>.wsdl`). `mvn generate-sources` crea en
   `target/generated-sources/wsimport` el paquete `ec.edu.monster.ws.generado`: la interfaz del puerto, la clase de servicio (`X_Service`) y un tipo por cada respuesta. **El código generado no se edita ni se versiona.**
   Cuando el servidor cambia su contrato: desplegarlo, copiar el WSDL nuevo y repetir `mvn generate-sources`.
2. **Interfaz y envoltorio** — `ws/ClienteXxx.java` (interfaz propia con las operaciones que usa el cliente, tipos de retorno = DTO propios) y `ws/ClienteWSXxx.java`, que implementa la interfaz
   sobre el puerto generado y copia cada respuesta generada a un DTO propio. Lo único que ven el servicio y las vistas es la interfaz:
   ```java
   public final class ClienteWSXxx implements ClienteXxx {
       private final WSServicio puerto;                       // interfaz del puerto generada por wsimport
       public static ClienteXxx crear() {
           return new ClienteWSXxx(ClienteWS.configurar(new WSServicio_Service().getWSServicioPort()));   // URL completa y timeouts
       }
       @Override public RespuestaListaDto obtenerItems(String token) {
           RespuestaLista r = puerto.obtenerItems(token);
           if (r == null) { return null; }
           RespuestaListaDto dto = new RespuestaListaDto();
           dto.setEstado(r.getEstado()); dto.setCodigo(r.getCodigo()); dto.setMensaje(r.getMensaje());
           if (r.getElementos() != null) { dto.getElementos().addAll(r.getElementos().getElemento()); }
           return dto;
       }
   }
   ```
   y en `Main`: `ClienteXxx cliente = ClienteWSXxx.crear();`. Los nombres exactos (`WSServicio`, `WSServicio_Service`, `getWSServicioPort`) los decide `wsimport` a partir del WSDL y solo se usan en este archivo.
3. **DTO propio** — clase `…Dto` en el paquete `dto` que extienda `RespuestaBaseDto`, como **POJO sin anotaciones**, con los mismos atributos que el tipo generado:
   ```java
   public class RespuestaListaDto extends RespuestaBaseDto {
       protected List<String> elementos;
       public List<String> getElementos() { if (elementos == null) { elementos = new ArrayList<>(); } return elementos; }   // nunca nulo
   }
   ```
   (Alternativa: usar directamente los tipos generados en el servicio; el cliente queda más corto, pero cada cambio del WSDL se propaga a las capas superiores.)
4. **Servicio** — `servicio/ServicioXxx` (si hay varios, una clase común con `token()`, `llamar(...)` y `verificar(...)` que ellos extiendan):
   ```java
   public List<String> items() {
       return List.copyOf(verificar(llamar(() -> cliente.obtenerItems(token()))).getElementos());
   }
   ```
   - `token()` lanza `AUTH-002` sin llamar al servidor si la `Sesion` no tiene token.
   - `llamar(...)` convierte `WebServiceException` (red, timeout, `Fault`) en `ExcepcionServicio(ExcepcionServicio.RED, "No fue posible comunicarse con el servidor", e)`.
   - `verificar(...)` devuelve la respuesta si `esOk()`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor, y descarta el token si `exigeLogin()`.
   - Aquí van las reglas **del cliente** (campo vacío, formato, coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
5. **Vista** — `vista/XxxVista extends VistaBase`: pide datos con `pedir(...)` / `elegir(...)`, muestra con `mostrar(...)` y `mostrarError(...)`. Sin lógica de negocio ni llamadas al servidor.
6. **Controlador** — `controlador/XxxControlador`: pide a la vista, llama al servicio, muestra el resultado o `e.getMessage()` (solo el mensaje). Devuelve un `enum Resultado` (p. ej. `SEGUIR`, `IR_AL_LOGIN`, `SALIR`).
   Si `e.exigeLogin()` → `IR_AL_LOGIN`. Si `e.servidorNoDisponible()` se muestra el mensaje y se conservan los datos ya capturados (no se cierra el programa).
7. **Composición** — en `Main`: crear la `Sesion` (**una sola** para toda la ejecución), el cliente (`ClienteWSXxx`), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Todo `RuntimeException` no previsto se captura en el bucle (mensaje por `System.err`), nunca termina con una traza.
8. **Pruebas** — un doble de la interfaz propia (`ClienteXxx` es una interfaz: sirve una lambda o `java.lang.reflect.Proxy`) para el servicio (OK, error de negocio, red, sesión vencida, coma enviada como punto);
   los controladores se prueban con una vista falsa (subclase de `VistaBase` que devuelve respuestas fijas), sin tocar la consola. Para el envoltorio, repetir el patrón de `ConvencionClienteTest` con la operación real.

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED`.
- **El token** vive en `Sesion` (memoria), no se guarda en disco ni en logs ni se imprime. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- Un fallo de red no cierra el programa: se muestra el mensaje y se vuelve al menú.

## 5. Convenciones de consola

- **Codificación:** `Main` fuerza UTF-8 en `System.out`/`System.err` y `VistaBase` lee el teclado como UTF-8. Las tildes y la «ñ» salen bien solo si la terminal también está en UTF-8 (`chcp 65001` en cmd; PowerShell y Windows Terminal suelen estarlo).
- **Menús:** listas numeradas con `elegir(titulo, opciones)`: devuelve el índice (base 0) o `-1` si se elige `0. Cancelar`. «0» es siempre cancelar / volver / salir.
- **Errores:** se muestran con `mostrarError(...)` (prefijo `[!] `) y solo con el mensaje. Una entrada inválida se vuelve a pedir; no se lanza excepción.
- **Datos pedidos:** `pedir(etiqueta)` devuelve el texto ya recortado (`trim`); validar lo trivial (vacío, número) en el servicio o la vista antes de llamar al servidor.
- **Contraseñas:** `pedir(...)` hace eco del texto. Para el login usar `System.console().readPassword()` cuando exista consola, con `pedir(...)` como alternativa (el IDE no proporciona consola); nunca imprimir ni registrar la contraseña.
- **Entrada cerrada:** `pedir` devuelve `""` al terminar la entrada (Ctrl+Z / Ctrl+D o `stdin` agotado). Como `elegir` repite hasta recibir una opción válida, **un bucle de menú se queda girando** en ese caso: si se necesita ejecutar con `stdin` redirigido,
  distinguir ese caso (p. ej. contar entradas vacías consecutivas y salir).
- **Esperas:** la llamada al servidor bloquea la consola (hasta el timeout de 10 s); mostrar «Consultando…» antes de llamar si la operación puede tardar.
- **Salida:** presentar el resultado en pocas líneas, con el mismo formato y los mismos textos que los demás clientes del proyecto (escritorio, web, móvil).

## 6. Servidor y ejecución

- **Servidor por defecto:** `ClienteWS.URL_POR_DEFECTO` (URL completa). Sin recompilar: `-Dws.url=http://host:8080/Contexto/Servicio` o la variable de entorno `WS_URL`.
- **El WSDL va empaquetado** (`wsdlLocation` en el classpath): crear el proxy no abre ninguna conexión y funciona con el servidor apagado. El fallo llega al **llamar** una operación (`WebServiceException`), que el servicio traduce a `RED`: el programa muestra el mensaje en vez de la traza.
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`). La consola de NetBeans no es una terminal: no hay `System.console()`.
- **Terminal:** `mvn compile exec:java` (`exec:java` no compila); con otro servidor: `mvn compile exec:java -Dws.url=http://192.168.1.10:8080/Contexto/Servicio`.
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
3. Con el servidor apagado: aparece «No fue posible comunicarse con el servidor» y el programa sigue respondiendo.
4. Token vencido: vuelve al login con el mensaje.
5. Tildes y «ñ» en entrada y salida; entrada vacía y opción inválida en los menús.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `servicio`:

Es la convención común de la familia (`CONVENCION_SOAP.md`); lo que **el código ya cumple** y lo que **debe cumplir el servidor**:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): lo declara el XSD del servidor (`elementFormDefault="qualified"`) y el proxy generado lo respeta.
  Si el WSDL copiado no lo declara, los campos llegan `null` sin error (R3).
- **SOAPAction = `TNS + Servicio + "/" + operación`**: viene en el `soapAction` de cada operación del WSDL y el proxy la envía. Un servidor .NET rechaza la acción vacía con HTTP 500 *ActionNotSupported* (R4).
- **El puerto se pide a la clase de servicio generada** (`new X_Service().getXPort()`): el cliente no depende del nombre del puerto ni del binding, que varían entre Java y .NET (R10).
- **La URL completa del servicio** se recibe por configuración (`ClienteWS.configurar`) y sustituye a la dirección del WSDL; el cliente no asume puerto ni «base + servicio» (R9). El namespace y el nombre del servicio salen del WSDL.
- Cada respuesta es un elemento `respuesta` (`@WebResult(name = "respuesta")`) con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R6); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R7);
  los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5/R8). Si el servidor cambia su contrato hay que actualizar el contrato y los DTO del cliente.
- Si el WSDL viene de .NET con campos `nillable`, `wsimport` genera `JAXBElement<String>` en lugar de `String`: agregar un archivo de binding con `generateElementProperty="false"` (`<bindingFiles>` del plugin; CONVENCION_SOAP.md §4).
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (manejadores JAX-WS) y regenerar el proxy.
- Para un servicio **REST** usar la plantilla `rest_java_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `WebServiceException` al llamar una operación | Servidor apagado o `URL_POR_DEFECTO` distinta de la real. Abrir `<URL>?wsdl` en el navegador |
| Error al ejecutar `generate-sources` | El WSDL no está en `src/main/resources/wsdl`, el `.xsd` no está junto a él o su `schemaLocation` no apunta al archivo local |
| Cambió el servidor y el cliente no ve la operación nueva | Copiar de nuevo el WSDL y el `.xsd`, `mvn generate-sources` y agregar la operación a la interfaz y al envoltorio (§4) |
| Respuesta con campos vacíos | Se copió un WSDL antiguo, o el envoltorio no copia un campo de la respuesta generada al DTO propio |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | El WSDL copiado no trae el `soapAction` de la operación (se envía `SOAPAction ""`): copiar el WSDL del servidor actualizado y regenerar |
| El servidor no recibe los parámetros (llegan vacíos) | El WSDL/XSD copiado no califica los elementos (`elementFormDefault="qualified"`): copiar el WSDL del servidor actualizado |
| `ClassNotFoundException` / «Provider … not found» de JAX-WS | Falta `jaxws-rt` en el `pom.xml` (el JDK 21 ya no incluye JAX-WS); si se empaqueta un jar, falta `ServicesResourceTransformer` |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó el `PrintStream` UTF-8 de `Main` |
| El menú repite «Opción inválida» sin parar | La entrada estándar se cerró (ver §5, «Entrada cerrada») |
| `System.console()` es `null` | Se ejecuta desde el IDE o con la entrada redirigida: usar `pedir(...)` como alternativa |
