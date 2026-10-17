
# Plantilla: cliente web Java (Jakarta EE) para servicios SOAP

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga un servicio **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Contiene lo que **todo** cliente necesita y deja
claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java 21. **Plataforma:** Jakarta EE 11 sobre Payara 7 (WAR). **Interfaz:** servlets como controladores + **JSP** como vistas, escritas a código, sin frameworks de UI (ni JSF ni JS pesado). **Arquitectura:** MVC por capas.
- **Transporte:** JAX-WS: el contrato es una interfaz Java y el proxy se genera en ejecución a partir del WSDL. No se generan clases con `wsimport` ni se usa el asistente *Web Service Client* de NetBeans.
  La implementación de JAX-WS la aporta Payara (`webservices-rt` va en `provided`): **no** se empaqueta `jaxws-rt` como en escritorio.
- **El navegador no habla SOAP:** quien llama al servidor es el servlet (el servidor del cliente web), nunca JavaScript. Por eso el servidor SOAP no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida). Se aplica con **componentes JSP reutilizables** (`WEB-INF/tags`) y un único `estilos.css`.
- **Convención SOAP:** sigue `CONVENCION_SOAP.md` (copia en esta carpeta): así entiende a un servidor Java o .NET que la cumpla.
- **Verificada:**
  - `mvn test` → BUILD SUCCESS, **16 pruebas JUnit en verde** (servicio de autenticación y reglas heredadas de `ServicioBase`, puerto perezoso, URL y 5 de **conformidad** con la convención), sin servidor externo.
    Las de conformidad usan un servidor falso local y miden el XML real (parámetros y campos de la respuesta calificados, SOAPAction con URI, respuesta entendida, URL completa); sin `dto/package-info.java` una falla.
    `mvn package` también pasa y el WAR no empaqueta ningún JAR.
  - **Antes de adaptarla a la convención** se probó contra un **servidor real** (con una copia temporal que solo cambió las constantes de `ConfiguracionServicio`, desplegada en Payara 7); **no se ha repetido** desde entonces: acceso a `/inicio` sin sesión
    redirige al login; login con credenciales incorrectas muestra el mensaje del servidor y conserva el usuario; login correcto navega a `/inicio`; con sesión, `/login` redirige a `/inicio`; «Cerrar sesión» vuelve al login
    y una pantalla protegida ya no abre. Se comprobó también, con una página temporal, el HTML de **todos** los componentes (selector, lista de acciones, mensajes, campo con error) y que el texto dinámico sale **escapado**.
    La pantalla de acceso y esa página se vieron **renderizadas** en un navegador (Edge sin ventana, 1100 px).
  - **No se ha ejecutado:** una operación de dominio distinta del login, la plantilla adaptada desplegada en Payara, la presentación clara, el diseño de columna única
    (< 900 px) en un navegador, el redireccionamiento al login por token vencido (`AUTH-003`) de extremo a extremo, el comportamiento con el servidor SOAP apagado (solo lo cubre una prueba unitaria del reintento del proxy), ni la ejecución desde NetBeans.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21**, Maven y **Payara 7** registrado como servidor; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`) y poner el mismo nombre en el **context root** de `src/main/webapp/WEB-INF/glassfish-web.xml`. El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor SOAP** en un único archivo: `src/main/java/ec/edu/monster/ws/ConfiguracionServicio.java`
   (`URL_POR_DEFECTO`, `NOMBRE_SERVICIO`, `NAMESPACE`). `URL_POR_DEFECTO` es la **URL completa del servicio** (host, puerto y ruta, sin `?wsdl` ni «/» final);
   los otros dos salen del WSDL (`<URL>?wsdl`). No hay nombre de puerto ni «base + servicio»: ver §8.
5. **Poner el nombre de la aplicación** en `WEB-INF/web.xml` (`context-param` `nombreAplicacion`: título de la pestaña y de la pantalla de acceso) y, si se quiere, **sustituir la imagen**
   `src/main/webapp/recursos/img/fondo_app.jpeg` (es una imagen de ejemplo; ver `docs/CLIENT_STYLES_JAVA.md` §6 para los requisitos de la imagen).
6. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
7. **Probar el servidor** (el WSDL abre en el navegador), ejecutar el proyecto en Payara (*Run* en NetBeans) y abrir `http://localhost:8080/<context-root>/login`. El login debe funcionar tal cual.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Login (compartido)** | `controlador/LoginControlador`, `vista/login.jsp`, `servicio/ServicioAutenticacion`, `dto/RespuestaLoginDto` | Solo la pantalla de destino (`ContextoWeb.PANTALLA_INICIAL`) y, si el servidor difiere, la operación `login` |
| **Núcleo de servicios** | `servicio/ServicioBase` (sesión, `token()`, `llamar(...)`, `verificar(...)`) | No; los servicios del dominio la extienden |
| **Sesión y errores** | `modelo/Sesion`, `excepciones/ExcepcionServicio` | No |
| **Transporte SOAP** | `ws/ClienteWS`, `ws/ConfiguracionServicio`, `ws/ContratoWS` | `ConfiguracionServicio` siempre; `ContratoWS` para **agregar operaciones** |
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaListaDto`, `dto/package-info.java` (elementos calificados) | Se agregan DTO del dominio al lado; **`package-info.java` no se borra** |
| **Contexto web** | `controlador/ContextoWeb` (sesión HTTP, servicios por petición, navegación) | Se agrega un método por servicio nuevo |
| **Seguridad de acceso** | `filtro/FiltroAcceso` (solo con token), `filtro/FiltroCodificacion` (UTF-8) | Se agregan las rutas protegidas a `FiltroAcceso` |
| **Estilos y componentes** | `webapp/recursos/css/estilos.css`, `recursos/js/app.js`, `WEB-INF/tags/*.tag`, `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se escriben clases ni estilos en los JSP |
| **Textos comunes** | `web.xml` (`nombreAplicacion`, `tema`) | `nombreAplicacion` |
| **Pantalla de ejemplo (dominio)** | `controlador/InicioControlador`, `vista/inicio.jsp` | **Se reemplaza** por las pantallas del dominio |
| **Dominio (vacío)** | — | Nuevos `dto/`, `servicio/`, `controlador/`, `vista/` |

Las zonas a adaptar están marcadas con el comentario **`DOMINIO`** en el código (`grep -rn "DOMINIO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin()
├── dto/           package-info, RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto    POJOs con anotaciones JAXB
├── ws/            ConfiguracionServicio, ContratoWS, ClienteWS
├── servicio/      ServicioBase, ServicioAutenticacion     única capa que conoce ws y dto
├── controlador/   ContextoWeb, LoginControlador, InicioControlador          servlets
└── filtro/        FiltroAcceso, FiltroCodificacion        transversal a las pantallas
src/main/webapp/
├── recursos/      css/estilos.css, js/app.js, img/fondo_app.jpeg            lo único público (se sirve tal cual)
└── WEB-INF/       web.xml, beans.xml, glassfish-web.xml                     descriptores
    ├── tags/      pantalla, formulario, encabezado, campo, selector, mensaje, boton, lista-acciones, cerrar-sesion   componentes (.tag)
    └── vistas/    login.jsp, inicio.jsp                                     pantallas; no accesibles por URL directa
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor), misma estructura
docs/CLIENT_STYLES_JAVA.md              guía de estilos
```

Dependencias permitidas: `vista (JSP) ← controlador → servicio → ws/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `ws` ni `dto`** y recibe datos solo como atributos de la petición (`request.setAttribute`). Si el WSDL cambia solo se tocan `ws`, `dto` y `servicio`.
Los JSP viven en `WEB-INF/vistas`: el navegador no puede pedirlos directamente; solo los muestran los controladores (`ContextoWeb.mostrar`).
Excepción conocida: los DTO leen `ConfiguracionServicio.NAMESPACE` (para que el espacio de nombres se cambie en un solo lugar).

**Una petición, de punta a punta:** navegador → `FiltroCodificacion` → `FiltroAcceso` (si la ruta es protegida) → servlet (`*Controlador`) → servicio (por petición, con la `Sesion` del usuario) → `ContratoWS` (SOAP) → el servlet deja datos en la petición → JSP.
El estado del usuario (el token) vive en la **sesión HTTP** (`ContextoWeb.sesion`), nunca en un bean `@ApplicationScoped`: lo compartirían todos los usuarios. El proxy SOAP, en cambio, **sí** es uno solo para toda la aplicación (`ClienteWS.compartido()`).

## 4. Receta: agregar una operación SOAP y su pantalla

Ejemplo: operación `obtenerItems(token)` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **Contrato** — `ws/ContratoWS.java` (los nombres son los del servidor y van **en su orden**):
   En **cada** operación van las tres declaraciones de la convención: `action` (SOAPAction) y `targetNamespace` en `@WebResult` y en cada `@WebParam` (el `login` existente es el modelo):
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
   Aquí van las reglas **del cliente** (campo vacío, formato, normalizar la coma decimal); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
   En `ContextoWeb` agregar `static ServicioItems items(HttpServletRequest p) { return new ServicioItems(ClienteWS.compartido(), sesion(p)); }`: se arma **por petición** con la `Sesion` del usuario (los servicios no guardan estado propio).
4. **Controlador** — molde: `InicioControlador` (y el bloque de su Javadoc). Un servlet `@WebServlet("/items")`: llama al servicio, deja el resultado con `request.setAttribute(...)` y muestra el JSP con `ContextoWeb.mostrar(peticion, respuesta, "items")`.
   Atrapa `ExcepcionServicio`: si `e.exigeLogin()` → `ContextoWeb.volverAlLogin(peticion, respuesta, e.getMessage())`; si no, `setAttribute("error", e.getMessage())` (solo el mensaje).
   Para un formulario: `doPost` valida lo trivial, llama al servicio y vuelve a mostrar la misma pantalla con el resultado o el error (no pierde lo escrito: devolver los valores como atributos).
5. **Ruta protegida** — agregar `"/items"` a `urlPatterns` de `filtro/FiltroAcceso`. (Sin esto la pantalla abriría sin sesión y fallaría con `AUTH-002` al llamar al servidor.)
6. **Vista** — `WEB-INF/vistas/items.jsp`, **solo con componentes `ui:`** (ver `inicio.jsp`, `login.jsp` y la tabla de `docs/CLIENT_STYLES_JAVA.md` §7.3). Sin `class`, sin `style`, sin lógica; muestra lo que dejó el controlador (`${items}`, `${error}`).
   Los enlaces a otras pantallas son `<ui:boton href="/otra">` (relativos al contexto) o `<ui:lista-acciones>` con datos del servidor.
7. **Pruebas** — un doble del contrato (`ContratoWS` es una interfaz: sirve una lambda o `java.lang.reflect.Proxy`, ver `ServicioAutenticacionTest`) y pruebas del servicio:
   casos mínimos del servicio (OK, error de negocio, red, sesión vencida, valor con coma enviado con punto). Por cada operación nueva, repetir el patrón de `ws/ConvencionClienteTest`
   (agregarla al servidor falso y comprobar parámetros calificados, SOAPAction y campos de la respuesta). Los controladores y los JSP se verifican **ejecutando la aplicación** (§7).

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: el servidor responde HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED` (`ServicioBase.llamar`).
- **El token** vive en `Sesion` (en la sesión HTTP), no se guarda en disco, ni en cookies propias, ni en logs, ni en la página. Ante `AUTH-002`/`AUTH-003` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
  `FiltroAcceso` solo comprueba que *haya* token; si el servidor ya lo venció, lo detecta el servicio al llamar.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio). El campo numérico de la vista es `type="text"` con `inputmode="decimal"`, no `type="number"`.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Escapar todo texto dinámico** en los JSP (`<c:out>`; los componentes ya lo hacen). La contraseña nunca se devuelve a la página.
- Tras autenticar se renueva el identificador de sesión (`changeSessionId`: evita la fijación de sesión). La cookie es `HttpOnly` y no se admite el id en la URL (`web.xml`).
- Un fallo de red no tumba la aplicación: se muestra el mensaje y la pantalla sigue usable. El WAR arranca aunque el servidor SOAP esté apagado (el proxy se crea al primer uso y se reintenta).
- Cada `POST` que cambie algo debería terminar en redirección (patrón Post/Redirect/Get) si el resultado no es una página de respuesta del mismo formulario; el login y el logout ya lo hacen.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Los JSP **solo usan los componentes** de `WEB-INF/tags`; no escriben clases, estilos ni colores. Componentes disponibles:

| Etiqueta | Para qué |
|---|---|
| `ui:pantalla` | Estructura de toda página: cabecera HTML, columna de contenido (420, centrada) e imagen a la derecha |
| `ui:formulario` | Formulario POST; al enviarlo deshabilita los controles y muestra el progreso (`app.js`) |
| `ui:encabezado` | Título de pantalla (`aplicacion="true"` para el título de la aplicación en el acceso) |
| `ui:campo`, `ui:selector` | Etiqueta + campo de texto/contraseña, etiqueta + lista desplegable (`invalido="true"` marca el borde de error) |
| `ui:mensaje` | Mensaje o resultado (`tipo`: `error`, `exito`, `resultado`); no se dibuja si no hay texto |
| `ui:boton` | Botón primario (acción principal) o `tipo="secundario"` (salida o regreso); con `href` es un enlace |
| `ui:lista-acciones` | Una columna de botones primarios generados con datos del servidor |
| `ui:cerrar-sesion` | Botón secundario «Cerrar sesión» |

- **Disposición:** pantalla a la izquierda (columna de 420, centrada) e imagen a la derecha (completa y centrada); bajo 900 px, columna única sin imagen. La imagen es de la **página** (`ui:pantalla`), no de cada JSP.
- **Presentación clara:** cambiar el `context-param` `tema` de `web.xml` a `tema-claro` (los valores ya están en `estilos.css`; ver `docs/CLIENT_STYLES_JAVA.md` §2.3 y §6: la clara no usa imagen).
- **Las imágenes** van en `src/main/webapp/recursos/img` y se referencian desde el CSS con ruta relativa (`../img/...`).
- **Componente nuevo:** crear `WEB-INF/tags/<nombre>.tag` (escapando el texto con `<c:out>`) y su CSS en `estilos.css`, con tokens (`var(--…)`), nunca con colores sueltos. Probarlo en un navegador a 1100 px y a menos de 900 px.
- JSP y tag files son texto: un error de sintaxis **no falla al compilar**, aparece al abrir la pantalla (HTTP 500). Probar cada pantalla nueva y revisar `glassfish\domains\domain1\logs\server.log`.

## 6. Servidor y ejecución

- **Servidor SOAP por defecto:** `ConfiguracionServicio.URL_POR_DEFECTO` (URL completa). Sin recompilar: propiedad de la JVM de Payara `-Dws.url=http://host:8080/Contexto/Servicio` (en la consola de administración → *Configurations → server-config → JVM Settings → JVM Options*; requiere reiniciar el dominio) o la variable de entorno `WS_URL` del proceso de Payara.
- Si el servidor SOAP está apagado o la URL es incorrecta, la **primera operación** muestra «No fue posible comunicarse con el servidor» y la siguiente reintenta crear el proxy; no hay que reiniciar la aplicación.
- **NetBeans:** *Run* despliega el WAR en Payara 7 y abre el navegador en `/<context-root>/`; el archivo de bienvenida es `/login`.
- **Consola de Payara / línea de comandos:** `mvn clean package` genera `target/<artifactId>-1.0-SNAPSHOT.war`; se despliega desde la consola web (`http://localhost:4848`, *Applications → Deploy*) o con `asadmin deploy`.
  El context root sale de `glassfish-web.xml`.
- **URL:** `http://localhost:8080/<context-root>/login`.
- **Sesión HTTP:** 30 minutos de inactividad (`web.xml`); es independiente del token del servidor (que vence a su ritmo y se reconoce por el código `AUTH-003`).

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor)
mvn clean package            # compila, prueba y genera el WAR
```
Probar el servidor sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con la aplicación desplegada (los controladores y los JSP **no** se cubren con JUnit):
1. Sin sesión, abrir una ruta protegida (p. ej. `/inicio`): debe redirigir a `/login`.
2. Login con credenciales incorrectas: muestra el mensaje del servidor; correcto: entra a la pantalla inicial; «Cerrar sesión» vuelve al login y «Atrás» no muestra la pantalla protegida.
3. Con el servidor SOAP apagado: el mensaje de error aparece y la aplicación sigue respondiendo.
4. Ventana ancha (≥ 900 px: imagen a la derecha) y estrecha (columna única, sin imagen, sin scroll horizontal).
5. Revisar `server.log` de Payara tras recorrer las pantallas.

Un agente que no pueda abrir el navegador debe decir explícitamente qué verificó (pruebas, peticiones HTTP con `curl`, capturas renderizadas sin ventana, contra qué servidor) y que **no vio la aplicación en un navegador real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `ws`, `dto` y `ServicioAutenticacion`:

Es la convención común de la familia (`CONVENCION_SOAP.md`); lo que **el código ya cumple** y lo que **debe cumplir el servidor**:

- SOAP **1.1**, *document/literal wrapped*. **Elementos calificados** con el namespace del servicio (parámetros, `respuesta` y sus campos): en el cliente lo garantizan
  `dto/package-info.java` y los `targetNamespace` del contrato. Si faltan, los campos llegan `null` sin error (R3).
- **SOAPAction = `NAMESPACE + Servicio + "/" + operación`** (atributo `action` de cada `@WebMethod`; ayuda: `ConfiguracionServicio.accion(...)`).
  Un servidor .NET rechaza la acción vacía con HTTP 500 *ActionNotSupported* (R4).
- **El puerto se obtiene por la interfaz** (`getPort(ContratoWS.class)`, hecho en `ClienteWS`): no depende del nombre del puerto ni del binding, que varían entre Java y .NET (R10).
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

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `jakarta.jws.*` / `jakarta.xml.ws.*` no reconocido tras tocar el `pom.xml` | Falta `webservices-rt:4.0.6` (`provided`): el API paraguas de EE 11 no incluye JAX-WS. Restaurarlo, *Reload POM* y *Clean and Build* |
| «Tipo de puerto no definido» | El `name` del `@WebService` del contrato (`NOMBRE_SERVICIO`) no coincide con el portType del WSDL, o `NAMESPACE` no es el `targetNamespace` |
| Respuesta con campos vacíos | Falta o se borró `dto/package-info.java`, el DTO no está en el paquete `dto`, o se renombró un `name` XML |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | Falta el `action` en el `@WebMethod` (se envía `SOAPAction ""`) |
| El servidor no recibe los parámetros (llegan vacíos) | Falta `targetNamespace` en algún `@WebParam` (viajan sin calificar) |
| Conflictos de `jakarta.xml.ws` / `ClassCastException` en Payara | JAX-WS (`jaxws-rt`, `jakarta.xml.ws-api`, `jakarta.activation-api`) dentro del WAR. Dejar solo `webservices-rt` como `provided` |
| 404 al abrir la aplicación | El context root no es el supuesto: ver `glassfish-web.xml` y *Applications* en la consola de Payara |
| 500 al abrir una pantalla | Error en un JSP o *tag file* (atributo mal escrito, etiqueta sin cerrar): revisar `server.log` |
| «No fue posible comunicarse con el servidor» | Servidor SOAP apagado, URL base distinta del servidor real o `NOMBRE_SERVICIO`/`NAMESPACE` incorrectos. Abrir `…?wsdl` en el navegador |
| Respuesta con campos vacíos | El namespace de un `@XmlType` no coincide con el del WSDL, o se renombró un `name` XML (son el contrato con el servidor, no el nombre Java) |
| Tildes o «ñ» corruptas en los datos enviados | Se quitó `FiltroCodificacion` o un JSP sin `pageEncoding="UTF-8"` |
| Una pantalla protegida abre sin sesión | Su ruta no está en `urlPatterns` de `FiltroAcceso` |
| El usuario ve la sesión de otro | El token se guardó en un bean `@ApplicationScoped` o en un campo estático; debe vivir solo en la `HttpSession` |
| Pantalla «congelada» con el indicador girando | El navegador restauró la página en estado ocupado con «Atrás»: `app.js` lo limpia en `pageshow`; si se cambió el script, conservarlo |
