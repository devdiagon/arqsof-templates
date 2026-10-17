# Plantilla: cliente de consola Java para servicios SOAP

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, los tipos comunes y la
entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

- **Lenguaje:** Java 21. **Interfaz:** texto por la consola (`Scanner` / `System.out`), sin librerías de terceros. **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** JAX-WS (`jaxws-rt` 4.0.3): el contrato es una interfaz Java y el proxy se genera en ejecución a partir del WSDL. No se generan clases con `wsimport`.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla.
- **Verificada:** `mvn test` → BUILD SUCCESS, 8 pruebas JUnit en verde: 3 de configuración de la URL y 5 de **conformidad** con la convención
  (contra un servidor falso local que la cumple: parámetros y campos de la respuesta calificados en el XML, SOAPAction con URI, respuesta entendida, URL completa; sin `dto/package-info.java` una falla).
  **No se ha ejecutado:** el cliente contra el servidor real del dominio, ni la secuencia de pantallas (la plantilla no trae ninguna).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId`; opcionalmente agregar `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor:** en `ws/ClienteWS.java`, `URL_POR_DEFECTO` = **URL completa del servicio** (sin `?wsdl`, sin «/» final; incluye host, puerto y ruta, tal como la entrega el servidor).
   En `dto/EspacioNombres.java`, `SERVICIO` = `targetNamespace` del WSDL (`<URL>?wsdl`). El nombre del servicio se pasa a `ClienteWS.crear(...)` desde `Main`. No hay nombre de puerto ni «base + servicio»: ver §8.
5. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
6. **Probar el servidor** (el WSDL abre en el navegador) y ejecutar: `mvn compile exec:java`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Transporte SOAP** | `ws/ClienteWS` (fábrica genérica del proxy: WSDL, endpoint, timeouts) | `URL_POR_DEFECTO` siempre; el resto no |
| **Contrato** | — (`ws/<Contrato>.java`, interfaz con `@WebService`) | **Se crea** por proyecto, una firma por operación del servidor |
| **Namespace** | `dto/EspacioNombres` (`SERVICIO`), `dto/package-info.java` (elementos calificados) | Solo `SERVICIO`; **`package-info.java` no se borra** |
| **DTO base** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`, `esOk()`) | No; se agregan DTO del dominio al lado |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`exigeLogin()`, `servidorNoDisponible()`) | No |
| **Entrada/salida** | `vista/VistaBase` (`pedir`, `elegir`, `mostrar`, `mostrarError`) | No; las vistas del dominio la extienden |
| **Composición** | `Main` (UTF-8 en la salida, manejo de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas** | `servicio/`, `controlador/` (carpetas vacías, `.gitkeep`), `vista/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (Sesion, contrato, servicio, vistas, controladores) y bucle principal
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin(), servidorNoDisponible()
├── dto/           EspacioNombres, package-info, RespuestaBaseDto (+ los del dominio)   POJOs con anotaciones JAXB
├── ws/            ClienteWS (+ el contrato del dominio)
├── servicio/      (vacía)                                 única capa que conoce ws y dto
├── controlador/   (vacía)                                 coordinan vista y servicio; devuelven un Resultado
└── vista/         VistaBase (+ las del dominio)           única capa que lee el teclado y escribe en la consola
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor externo), misma estructura; ws/ConvencionClienteTest + ContratoEjemplo + dto/RespuestaEcoDto
                                        son un ejemplo ejecutable de contrato, DTO y prueba de conformidad: copiarlos como modelo
```

Dependencias permitidas: `vista ← controlador → servicio → ws/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `ws` ni `dto`**: recibe y devuelve texto y tipos simples. Si el WSDL cambia solo se tocan `ws`, `dto` y `servicio`.
**Solo la vista usa `System.in`/`System.out`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación SOAP y su pantalla de consola

Ejemplo: operación `obtenerItems(token)` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba
(modelo ejecutable en `src/test`: `ContratoEjemplo`, `RespuestaEcoDto`, `ConvencionClienteTest`):

1. **Contrato** — `ws/ContratoXxx.java`, interfaz con las operaciones que usa el cliente (los nombres son los del servidor y van **en su orden**).
   En **cada** operación van las tres declaraciones de la convención: `action` (SOAPAction), `targetNamespace` en `@WebResult` y en cada `@WebParam`:
   ```java
   @WebService(name = "WSServicio", targetNamespace = EspacioNombres.SERVICIO)   // name = nombre del portType del WSDL
   @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
   public interface ContratoXxx {
       @WebMethod(operationName = "obtenerItems", action = EspacioNombres.SERVICIO + "WSServicio/obtenerItems")
       @WebResult(name = "respuesta", targetNamespace = EspacioNombres.SERVICIO)
       RespuestaListaDto obtenerItems(@WebParam(name = "token", targetNamespace = EspacioNombres.SERVICIO) String token);
   }
   ```
   y en `Main`: `ContratoXxx contrato = ClienteWS.crear(ContratoXxx.class, EspacioNombres.SERVICIO, "WSServicio");`
2. **DTO** — clase `…Dto` en el paquete `dto` que extienda `RespuestaBaseDto`, con los mismos atributos que el WSDL. Los elementos hijos **van calificados**
   con el namespace del servicio: lo logra `dto/package-info.java` (por eso los DTO deben estar en ese paquete):
   ```java
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "RespuestaLista", namespace = EspacioNombres.SERVICIO)   // name = el del complexType del WSDL
   public class RespuestaListaDto extends RespuestaBaseDto {
       @XmlElementWrapper(name = "elementos")
       @XmlElement(name = "elemento")
       protected List<String> elementos;
       public List<String> getElementos() { return elementos == null ? new ArrayList<>() : elementos; }   // nunca nulo
   }
   ```
   y registrarla en `@XmlSeeAlso({...})` de `RespuestaBaseDto`.
3. **Servicio** — `servicio/ServicioXxx` (si hay varios, una clase común con `token()`, `llamar(...)` y `verificar(...)` que ellos extiendan):
   ```java
   public List<String> items() {
       return List.copyOf(verificar(llamar(() -> contrato.obtenerItems(token()))).getElementos());
   }
   ```
   - `token()` lanza `AUTH-002` sin llamar al servidor si la `Sesion` no tiene token.
   - `llamar(...)` convierte `WebServiceException` (red, timeout, `Fault`) en `ExcepcionServicio(ExcepcionServicio.RED, "No fue posible comunicarse con el servidor", e)`.
   - `verificar(...)` devuelve la respuesta si `esOk()`; si no, lanza `ExcepcionServicio(codigo, mensaje)` del servidor, y descarta el token si `exigeLogin()`.
   - Aquí van las reglas **del cliente** (campo vacío, formato, coma decimal → punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
4. **Vista** — `vista/XxxVista extends VistaBase`: pide datos con `pedir(...)` / `elegir(...)`, muestra con `mostrar(...)` y `mostrarError(...)`. Sin lógica de negocio ni llamadas al servidor.
5. **Controlador** — `controlador/XxxControlador`: pide a la vista, llama al servicio, muestra el resultado o `e.getMessage()` (solo el mensaje). Devuelve un `enum Resultado` (p. ej. `SEGUIR`, `IR_AL_LOGIN`, `SALIR`).
   Si `e.exigeLogin()` → `IR_AL_LOGIN`. Si `e.servidorNoDisponible()` se muestra el mensaje y se conservan los datos ya capturados (no se cierra el programa).
6. **Composición** — en `Main`: crear la `Sesion` (**una sola** para toda la ejecución), el contrato, los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Todo `RuntimeException` no previsto se captura en el bucle (mensaje por `System.err`), nunca termina con una traza.
7. **Pruebas** — un doble del contrato (es una interfaz: sirve una lambda o `java.lang.reflect.Proxy`) para el servicio (OK, error de negocio, red, sesión vencida, coma enviada como punto);
   los controladores se prueban con una vista falsa (subclase de `VistaBase` que devuelve respuestas fijas), sin tocar la consola.

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
- **El WSDL se descarga al crear el proxy** (`ClienteWS.crear`), no al arrancar la JVM: con el servidor apagado falla ahí con `WebServiceException`. Crear el contrato dentro del `try` de `Main` (o la primera vez que se use) y mostrar el mensaje en vez de la traza.
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

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): en el cliente lo garantizan `package-info.java` y los `targetNamespace` del contrato.
  Si falta, los campos llegan `null` sin error (R3).
- **SOAPAction = `TNS + Servicio + "/" + operación`** (atributo `action` de cada `@WebMethod`). Un servidor .NET rechaza la acción vacía con HTTP 500 *ActionNotSupported* (R4).
- **El puerto se obtiene por la interfaz** (`getPort(Contrato.class)`, hecho en `ClienteWS`): el cliente no depende del nombre del puerto ni del binding, que varían entre Java y .NET (R10).
  `name` del `@WebService` del contrato = nombre del portType del WSDL.
- **La URL completa del servicio** se recibe por configuración; el cliente no asume puerto ni «base + servicio» (R9). `namespace` = `targetNamespace` del WSDL; nombre del servicio = `<wsdl:service name="…">`.
- Cada respuesta es un elemento `respuesta` (`@WebResult(name = "respuesta")`) con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R6); los errores de negocio van en el cuerpo con HTTP 200, no como Fault (R7);
  los nombres de operaciones, parámetros y campos son fijos e iguales en Java y .NET (R5/R8). Si el servidor cambia su contrato hay que actualizar el contrato y los DTO del cliente.
- Si el WSDL viene de .NET con campos `nillable`, `wsimport` genera `JAXBElement<String>`; esta plantilla no usa `wsimport`, por lo que no le afecta.
- Existe `login(usuario, contrasenia)` que devuelve `token`, y las operaciones protegidas reciben el `token` como parámetro (no como cabecera).
- Los códigos `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`).
- Si el servidor usa **cabecera SOAP** para el token, WS-Security u otro estilo, ampliar `ClienteWS` (manejadores JAX-WS).
- Para un servicio **REST** usar la plantilla `rest_java_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `WebServiceException` al crear el proxy | Servidor apagado, `URL_POR_DEFECTO` distinta de la real, o nombre de servicio incorrecto. Abrir `<URL>?wsdl` en el navegador |
| «Tipo de puerto no definido» | El `name` del `@WebService` del contrato no coincide con el portType del WSDL, o `EspacioNombres.SERVICIO` no es el `targetNamespace` |
| Respuesta con campos vacíos | Falta o se borró `dto/package-info.java`, el DTO no está en el paquete `dto`, o se renombró un `name` XML (son el contrato con el servidor, no el nombre Java) |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | Falta el `action` en el `@WebMethod` (se envía `SOAPAction ""`) |
| El servidor no recibe los parámetros (llegan vacíos) | Falta `targetNamespace` en algún `@WebParam` (viajan sin calificar) |
| `ClassNotFoundException` / «Provider … not found» de JAX-WS | Falta `jaxws-rt` en el `pom.xml` (el JDK 21 ya no incluye JAX-WS); si se empaqueta un jar, falta `ServicesResourceTransformer` |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó el `PrintStream` UTF-8 de `Main` |
| El menú repite «Opción inválida» sin parar | La entrada estándar se cerró (ver §5, «Entrada cerrada») |
| `System.console()` es `null` | Se ejecuta desde el IDE o con la entrada redirigida: usar `pedir(...)` como alternativa |
