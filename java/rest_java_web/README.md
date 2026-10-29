# Plantilla: cliente web Java (Jakarta EE) para APIs REST

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga una API **REST/JSON**
con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, el armado y la lectura de las peticiones, los tipos comunes, la verificación de la envoltura,
los componentes JSP con sus estilos y el contexto web, y deja marcado lo que cambia con el **dominio**. No incluye inicio de sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1) y `ESPECIFICACION_CLIENTES.md`. Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java 21. **Plataforma:** Jakarta EE 11 sobre Payara 7 (WAR). **Interfaz:** servlets como controladores + **JSP** como vistas, escritas a código, sin frameworks de UI (ni JSF ni JS pesado). **Arquitectura:** capas.
- **Transporte:** `java.net.http.HttpClient` (JDK, sin dependencia), con un solo cliente compartido y tiempo límite de 10 s. **JSON:** Gson 2.11.0, que **sí se empaqueta en el WAR** (`compile`): Payara aporta JSON-B y JSON-P, pero no Gson.
  `api/ConfiguracionApi` fija la URL base, `api/TransporteHttp` es la única clase que abre conexiones y `api/TraductorApi` arma las peticiones y lee las respuestas (sin red), traduciendo los fallos a `RED` y `SRV-000`.
- **El navegador no habla con la API:** quien llama al servidor es el servlet (el servidor del cliente web), nunca JavaScript. Por eso el servidor REST no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida). Se aplica con **componentes JSP reutilizables** (`WEB-INF/tags`) y un único `estilos.css`.
- **Convención REST:** sigue `docs/CONVENCION_REST.md` (copia en `docs/`): URL base completa con prefijo y «/» final (R1), todo dato como texto (R9), rutas con variables codificadas (R10). El contrato concreto del servidor es su tabla de endpoints (`ENDPOINTS_REST.md`), que el proyecto copia en `docs/`. Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su login y su sesión (§8). La plantilla deja el punto de enganche: `/inicio` (pantalla inicial) y `login.jsp`, una pantalla de acceso **pasiva** (sin controlador) lista para usar.
- **Verificada:** `mvn test` → BUILD SUCCESS, 20 pruebas JUnit en verde, sin servidor (verificación de la envoltura, cliente perezoso, configuración de la URL y armado y lectura de las peticiones). `mvn package` genera el WAR, que lleva solo Gson (y su dependencia `error_prone_annotations`) en `WEB-INF/lib`.
  **No se ha ejecutado:** la plantilla desplegada en Payara ni vista en un navegador con esta capa REST, ninguna petición HTTP real (`TransporteHttp`), ninguna operación de dominio, el login y el filtro de acceso (no forman parte de la plantilla), ni el comportamiento con el servidor REST apagado (solo lo cubre una prueba unitaria del reintento del cliente perezoso).
  Los JSP, los tag files y los estilos son los de la plantilla web SOAP, ya verificados en Payara 7 y un navegador.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21**, Maven y **Payara 7** registrado como servidor; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`) y poner el mismo nombre en el **context root** de `src/main/webapp/WEB-INF/glassfish-web.xml`. El paquete **`ec.edu.monster`** es la convención y **no se cambia**: cada capa es un paquete directo bajo ella.
4. **Configurar el servidor:** en `api/ConfiguracionApi.java`, `URL_BASE_POR_DEFECTO` = **URL base completa de la API** (host, puerto, contexto y prefijo, **con «/» final**), tal como la entrega la tabla de endpoints del servidor.
   Guardar una copia de esa tabla en `docs/` como contrato.
5. **Poner el nombre de la aplicación** en `WEB-INF/web.xml` (`context-param` `nombreAplicacion`: título de la pestaña y de la pantalla de acceso) y, si se quiere, **sustituir la imagen**
   `src/main/webapp/recursos/img/fondo_app.png` (es una imagen de ejemplo; ver `docs/CLIENT_STYLES_JAVA.md` §6 para los requisitos). La imagen es un **PNG** (admite transparencia); si se cambia por otra, con otro dibujo o proporciones, **se conserva como `fondo_app.png`** para no tocar el código ni los estilos. El tamaño con que se dibuja (60 % de su contenedor) se ajusta en `--margen-imagen` de `estilos.css`.
6. **Completar `docs/PROJECT.md`** (viene como esqueleto en `docs/`) y mantenerlo al día.
7. Ejecutar el proyecto en Payara (*Run* en NetBeans) y abrir `http://localhost:8080/<context-root>/`.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Núcleo de servicios** | `servicios/ServicioBase` (verifica la envoltura: no OK → `ExcepcionServicio`; nula → `SRV-000`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (códigos propios `RED` y `SRV-000`) | No |
| **Configuración de la URL** | `api/ConfiguracionApi` (`urlBase()`: propiedad `api.base.url`, variable `API_BASE_URL` o la constante) | `URL_BASE_POR_DEFECTO` siempre; el resto no |
| **Transporte** | `api/Transporte` (interfaz de un método), `api/TransporteHttp` (única clase que abre conexiones), `api/RespuestaHttp` (código y cuerpo) | No |
| **Armado y lectura** | `api/TraductorApi`: ruta codificada, cuerpo JSON, cabecera `Basic`, lectura de la envoltura; traduce los fallos a `RED` y `SRV-000`. No toca la red | No |
| **Cliente compartido** | `api/ClientePerezoso` (un solo cliente por servicio para toda la aplicación, creado en su primer uso y reintentado si falla) | No |
| **Interfaz e implementación del dominio** | — (`api/XCliente.java` y `api/XClienteApi.java`) | **Se crean** por proyecto, una firma por fila de la tabla de endpoints (§4, pasos 2 y 3) |
| **DTO base** | `dto/RespuestaBaseDto` (POJO sin anotaciones) | Se agregan DTO del dominio al lado |
| **Contexto web** | `controlador/ContextoWeb` (mostrar vistas, redirigir, resultado y aviso de una sola lectura, cerrar sesión; punto de composición de los clientes) | Se agrega un cliente y un método por servicio |
| **Codificación y textos** | `filtro/FiltroCodificacion` (UTF-8); `filtro/ImportaTextos` (hace visible `Textos` en las expresiones EL: `${Textos.CODIGO_CUENTA}`, `${Textos.saludo(nombre, paterno)}`) | No |
| **Estilos y componentes** | `webapp/recursos/css/estilos.css`, `recursos/js/app.js`, `WEB-INF/tags/*.tag`, `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se escriben clases ni estilos en los JSP |
| **Textos** | `vista/Textos` (los que necesitan los controladores) y `web.xml` (`nombreAplicacion`, `tema`) | Se agregan los textos del dominio |
| **Punto de enganche** | `controlador/InicioControlador` + `vista/inicio.jsp`; `vista/login.jsp` (pasiva) | Se reemplazan/usan según el proyecto |
| **Dominio (vacío)** | `modelo/` | Nuevos `dto/`, `servicios/`, `controlador/`, `vista/`, e interfaz `XCliente` e implementación `XClienteApi` en `api/` |

Las zonas a adaptar están marcadas con el comentario **`DOMINIO`** en el código (`grep -rn "DOMINIO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── modelo/                     datos propios del cliente (p. ej. la sesión del empleado, si el proyecto tiene login); vacía en la plantilla
├── excepciones/   ExcepcionServicio
├── dto/           RespuestaBaseDto                       POJOs sin anotaciones para Gson, todos los campos como texto
├── api/           ConfiguracionApi, Transporte, TransporteHttp, RespuestaHttp, TraductorApi, ClientePerezoso     + XCliente / XClienteApi por proyecto
├── servicios/     ServicioBase                           única capa que conoce la interfaz del cliente y los dto
├── controlador/   ContextoWeb, InicioControlador         servlets
├── vista/         Textos                                 textos que usan los controladores (las páginas son JSP, en WEB-INF)
└── filtro/        FiltroCodificacion, ImportaTextos      transversal a las pantallas
src/main/webapp/
├── recursos/      css/estilos.css, js/app.js, img/fondo_app.png            lo único público (se sirve tal cual)
└── WEB-INF/       web.xml, beans.xml, glassfish-web.xml                     descriptores
    ├── tags/      pantalla, formulario, progreso, encabezado, campo, campo-con-acciones, fila-campos, selector, mensaje, boton, fila-botones, lista-acciones, cuadricula-acciones, tarjeta, tarjetas, tabla, cerrar-sesion   componentes (.tag)
    └── vistas/    login.jsp, inicio.jsp                                     pantallas; no accesibles por URL directa
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor), misma estructura
docs/                                   CLIENT_STYLES_JAVA, CONVENCION_REST, CONVENCION_PRUEBAS y PROJECT (esqueleto del proyecto)
README.md                               esta guía: el único documento que queda en la raíz
```

Dependencias: `vista (JSP) ← controlador → servicios → api | dto`; `modelo`, `dto` y `excepciones` son transversales.
**La vista nunca importa `api`** y recibe datos solo como atributos de la petición (`request.setAttribute`). Si el contrato cambia solo se tocan `api`, `dto` y `servicios`.
Los JSP viven en `WEB-INF/vistas`: el navegador no puede pedirlos directamente; solo los muestran los controladores (`ContextoWeb.mostrar`).
Solo la implementación `XClienteApi` conoce el protocolo, HTTP, JSON y las excepciones de red.

**Una petición, de punta a punta:** navegador → `FiltroCodificacion` → servlet (`*Controlador`) → servicio → interfaz del cliente (`XClienteApi`, que llama a la API REST) → el servlet deja datos en la petición → JSP.
**Estado:** los clientes de la API son **uno solo por servicio para toda la aplicación** (`ClientePerezoso`); en cambio el estado de cada usuario (su sesión, si hay login) vive **solo en su sesión HTTP**,
nunca en un bean `@ApplicationScoped` ni en un campo estático: lo compartirían todos los usuarios. Si el proyecto tiene login, se crea un contexto por sesión HTTP (la `Sesion` del empleado y los servicios que la reciben) y se guarda en ella.

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET items` que devuelve una lista y `POST items/{codigo}/pedidos` con un cuerpo. Siempre en este orden, de abajo hacia arriba:

1. **DTO propio** — clase `…Dto` en el paquete `dto` que extienda `RespuestaBaseDto`, como **POJO sin anotaciones**: los nombres de campo son los del JSON del servidor (Gson los mapea por nombre) y todos son texto.
   ```java
   public class RespuestaListaDto extends RespuestaBaseDto {
       protected List<String> elementos;
       public List<String> getElementos() { if (elementos == null) { elementos = new ArrayList<>(); } return elementos; }   // nunca nulo
   }
   ```
2. **Interfaz del cliente** — `api/XCliente.java`: **un método por fila de la tabla de endpoints, con el nombre de la operación SOAP equivalente** si existe y tipos de retorno = DTO propios. Las credenciales, si el servidor las pide en cada operación, son **parámetros** del método (como en SOAP): así `servicios` no cambia entre protocolos.
   ```java
   public interface XCliente {
       RespuestaListaDto obtenerItems(String usuario, String clave);
       RespuestaPedidoDto realizarPedido(String codigo, String cantidad, String usuario, String clave);
   }
   ```
3. **Implementación** — `api/XClienteApi.java`, con el `Transporte` por constructor y una fábrica `crear()` que usa el real. Cada método es una llamada a `TraductorApi.llamar`; **aquí, y solo aquí**, se decide verbo, ruta, cuerpo y cabecera:
   ```java
   public final class XClienteApi implements XCliente {
       private final Transporte transporte;
       public XClienteApi(Transporte transporte) { this.transporte = transporte; }
       public static XCliente crear() { return new XClienteApi(TransporteHttp.crear()); }

       @Override public RespuestaListaDto obtenerItems(String usuario, String clave) {
           return TraductorApi.llamar(transporte, "GET", "items", null,
                   TraductorApi.autorizacionBasic(usuario, clave), RespuestaListaDto.class);
       }
       @Override public RespuestaPedidoDto realizarPedido(String codigo, String cantidad, String usuario, String clave) {
           return TraductorApi.llamar(transporte, "POST", "items/" + TraductorApi.codificar(codigo) + "/pedidos",
                   TraductorApi.cuerpo("cantidad", cantidad), TraductorApi.autorizacionBasic(usuario, clave),
                   RespuestaPedidoDto.class);
       }
   }
   ```
   El ejemplo usa `Authorization: Basic` porque es lo que hace `TraductorApi.autorizacionBasic`; si el servidor usa otro esquema, el valor de la cabecera se arma aquí. Las credenciales **nunca** van en la ruta ni se registran.
   `TraductorApi.llamar` lee la envoltura **también con HTTP de error** (nunca descarta el cuerpo por el código), `IOException` → `RED`, cuerpo vacío o que no es JSON o sin `estado` → `SRV-000`.
   El cliente se crea **una vez** en `ContextoWeb`: `ClientePerezoso.de(XCliente.class, XClienteApi::crear)` (se crea en el primer uso y se reintenta si falla).
4. **Servicio** — `servicios/XServicio extends ServicioBase`: valida lo trivial (campos vacíos), convierte los datos al formato del contrato (coma decimal → punto) y llama:
   `return verificar(cliente.obtenerItems(usuario, clave));`. Las reglas del dominio las decide el servidor; el cliente muestra su `mensaje`. No captura excepciones de red: ya llegan traducidas a `RED`.
   En `ContextoWeb`, un método `static XServicio x(HttpServletRequest p)` que lo arma con el cliente compartido (los servicios no guardan estado propio).
5. **Controlador** — un servlet `@WebServlet("/items")` (molde: `InicioControlador`): valida campos vacíos, llama al servicio, deja el resultado con `request.setAttribute(...)` y muestra el JSP con `ContextoWeb.mostrar(peticion, respuesta, "items")`.
   Atrapa `ExcepcionServicio` y deja `setAttribute("error", e.getMessage())` (solo el mensaje, nunca el código). Cualquier otra excepción: `Textos.ERROR_INESPERADO`, sin traza.
   Para un formulario que **consulta** (no cambia datos): `doPost` valida, llama al servicio y vuelve a mostrar la misma pantalla con el resultado o el error, **conservando lo escrito** (devolver los valores como atributos) salvo los datos sensibles (claves).
   Para uno que **cambia datos** (una operación, un registro): Post/Redirect/Get con resultado de una sola lectura. El `POST` valida, llama al servicio, guarda el resultado, o el error y lo escrito, con `ContextoWeb.guardarFlash(...)` y redirige al `GET` de la misma pantalla, que lo recupera con `ContextoWeb.cargarFlash(...)`.
   Así recargar la página (F5) no repite la operación. Si el proyecto lo requiere, descartar además un `POST` repetido de la misma sesión mientras el anterior sigue en curso (un indicador por sesión HTTP).
   Lo que se guarda en la sesión debe ser serializable.
6. **Vista** — `WEB-INF/vistas/items.jsp`, **solo con componentes `ui:`** y textos de `Textos` (`${Textos.X}`, no escritos a mano) (ver `inicio.jsp`, `login.jsp` y la tabla de `docs/CLIENT_STYLES_JAVA.md` §7.2). Sin `class`, sin `style`, sin lógica; muestra lo que dejó el controlador (`${items}`, `${error}`).
   Los enlaces a otras pantallas son `<ui:boton href="/otra">` (relativos al contexto) o `<ui:lista-acciones>` con datos del servidor.
7. **Pruebas** — unitarias, sin servidor (`docs/CONVENCION_PRUEBAS.md`): `XCliente` es una interfaz, sirve un doble para el servicio (OK, error de negocio, red, respuesta nula, coma enviada como punto);
   para `XClienteApi`, un `Transporte` falso (una lambda) que registra verbo, ruta, cuerpo y cabecera y devuelve una `RespuestaHttp` (ver `TraductorApiTest`: no hace falta red ni servidor).
   Los controladores y los JSP se verifican **ejecutando la aplicación** (§7); el contrato se verifica a mano con `docs/CONVENCION_PRUEBAS.md` §6.3.

Reglas que se repiten en todo dominio:
- **La envoltura decide, no el código HTTP:** el cliente lee `estado`/`codigo`/`mensaje` del cuerpo también cuando el HTTP es 4xx o 5xx; el HTTP de error solo clasifica. Solo la falta de respuesta es `RED`, y una respuesta que no se puede interpretar es `SRV-000`.
- **Datos sensibles** (credenciales, si hay login): solo en la sesión HTTP del servidor del cliente; nunca en disco, cookies propias, logs ni en la página, y la clave nunca se devuelve al formulario.
- Si hay login: renovar el identificador de sesión al autenticar (`changeSessionId`: evita la fijación de sesión), invalidar la sesión al cerrarla y no guardar en caché las pantallas protegidas (`Cache-Control: no-store`) para que «Atrás» no las muestre. La cookie es `HttpOnly` y no se admite el id en la URL (`web.xml`).
- Todo dato viaja como **texto**; los decimales con **punto**. Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos. El campo numérico de la vista es `type="text"` con `inputmode="decimal"`, no `type="number"`.
- Mostrar solo el **mensaje**, nunca el código. Los textos salen de `Textos` (los mismos en todos los clientes); en los JSP se leen con `${Textos.X}` (no se escriben a mano en las páginas).
- **Escapar todo texto dinámico** en los JSP (`<c:out>`; los componentes ya lo hacen).
- **Lo que se muestra en un JSP es una clase con getters, no un `record`**: las expresiones EL de Payara 7 no ven los accesores de un `record` y la página falla con HTTP 500 (`PropertyNotFoundException`).
- Un fallo de red no tumba la aplicación: se muestra el mensaje y la pantalla sigue usable. El WAR arranca aunque el servidor REST esté apagado.

## 5. Estilos (resumen; detalle en `docs/CLIENT_STYLES_JAVA.md`)

Los JSP **solo usan los componentes** de `WEB-INF/tags`; no escriben clases, estilos ni colores. Componentes disponibles:

| Etiqueta | Para qué |
|---|---|
| `ui:pantalla` | Estructura de toda página. `modo="acceso"` (por defecto): disposición dividida, columna de 420 e imagen a la derecha. `modo="aplicacion"`: ancho completo con la imagen como fondo (`ancho`: `formulario` 480, `amplio` 640, `completo`) |
| `ui:formulario` | Formulario POST (`ocupado`: texto de la espera); al enviarlo deshabilita los controles, oculta el mensaje y los resultados anteriores y muestra `ui:progreso` (`app.js`) |
| `ui:encabezado` | Título de pantalla (`aplicacion="true"`: título de la aplicación, centrado; `plano="true"`: texto grande en negrita sin panel, para un saludo) |
| `ui:campo`, `ui:selector` | Etiqueta + campo de texto/contraseña, etiqueta + lista desplegable (`invalido="true"` marca el borde de error; `centrado="true"` reduce el ancho) |
| `ui:fila-campos`, `ui:campo-con-acciones` | Dos campos en horizontal con una flecha entre ambos; un campo con sus botones a la derecha (fragmentos `jsp:attribute`) |
| `ui:mensaje` | Mensaje o resultado (`tipo`: `error`, `exito`, `resultado`); no se dibuja si no hay texto |
| `ui:boton`, `ui:fila-botones` | Botón primario (acción principal) o `tipo="secundario"` (salida o regreso); con `href` es un enlace y `cancelar="true"` lo marca como Cancelar (la tecla Esc lo activa). `fila-botones` los alinea en una fila |
| `ui:lista-acciones`, `ui:cuadricula-acciones` | Botones primarios generados con datos del servidor: en una columna, o en una cuadrícula de dos columnas (una bajo 600 px) |
| `ui:tarjeta`, `ui:tarjetas` | Tarjeta de resultado (título centrado, monto con signo, variación, pie) y la fila que las agrupa; reciben los textos ya formateados |
| `ui:tabla` | Tabla con encabezado fijo; se desplaza dentro de su contenedor (la página nunca se desplaza en horizontal) |
| `ui:cerrar-sesion` | Botón secundario «Cerrar sesión» (POST `/logout`), para los proyectos con login |

- **Disposición:** el acceso va dividido (columna de 420 e imagen a la derecha); el resto de la aplicación usa el ancho completo y la imagen es una **capa fija de fondo**, completa, centrada y con opacidad. Bajo 900 px no hay imagen (columna única). La imagen es de la **página** (`ui:pantalla`), no de cada JSP.
- **Presentación clara:** cambiar el `context-param` `tema` de `web.xml` a `tema-claro` (ver `docs/CLIENT_STYLES_JAVA.md` §2.3 y §6: la clara no usa imagen).
- **Las imágenes** van en `src/main/webapp/recursos/img` y se referencian desde el CSS con ruta relativa (`../img/...`).
- **Componente nuevo:** crear `WEB-INF/tags/<nombre>.tag` (escapando el texto con `<c:out>`) y su CSS en `estilos.css`, con tokens (`var(--…)`), nunca con colores sueltos. Probarlo en un navegador a 1100 px y a menos de 900 px.
- JSP y tag files son texto: un error de sintaxis **no falla al compilar**, aparece al abrir la pantalla (HTTP 500). Probar cada pantalla nueva y revisar `glassfish\domains\domain1\logs\server.log`.

## 6. Servidor y ejecución

- **Servidor por defecto:** `ConfiguracionApi.URL_BASE_POR_DEFECTO` (URL base completa, con «/» final). Sin recompilar: propiedad de la JVM de Payara `-Dapi.base.url=http://host:8080/Contexto/api` (consola de administración → *Configurations → server-config → JVM Settings → JVM Options*; requiere reiniciar el dominio) o la variable de entorno `API_BASE_URL` del proceso de Payara (se normaliza: sin espacios y con una sola «/» final).
- Si el servidor REST está apagado o la URL es incorrecta, la **primera operación** muestra «Servidor no disponible, intente más tarde» y las siguientes lo vuelven a intentar; no hay que reiniciar la aplicación.
  Crear el cliente no abre ninguna conexión, así que el WAR arranca sin el servidor REST.
- **NetBeans:** *Run* despliega el WAR en Payara 7 y abre el navegador en `/<context-root>/`; el archivo de bienvenida es `/inicio`.
- **Consola de Payara / línea de comandos:** `mvn clean package` genera `target/<artifactId>-1.0-SNAPSHOT.war`; se despliega desde la consola web (`http://localhost:4848`, *Applications → Deploy*) o con `asadmin deploy`. El context root sale de `glassfish-web.xml`.
- **Sesión HTTP:** 30 minutos de inactividad (`web.xml`).

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor)
mvn clean package            # compila, prueba y genera el WAR
```
Probar el servidor sin la aplicación: `curl -i -X POST <BASE>auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"clave\":\"...\"}"` (la ruta y el cuerpo son los de la tabla de endpoints del proyecto).

Verificación con la aplicación desplegada (los controladores y los JSP **no** se cubren con JUnit):
1. Abrir la pantalla inicial y cada pantalla nueva: sin errores 500 y sin errores en `server.log`.
2. Con el servidor REST apagado: el mensaje de error aparece y la aplicación sigue respondiendo.
3. Ventana ancha (≥ 900 px: imagen a la derecha) y estrecha (columna única, sin imagen, sin scroll horizontal).
4. Con login: sin sesión, una ruta protegida redirige al acceso; tras cerrar sesión, «Atrás» no muestra la pantalla protegida; con dos empleados a la vez, cada uno opera con sus credenciales.

Un agente que no pueda abrir el navegador debe decir explícitamente qué verificó (pruebas, peticiones HTTP con `curl`, capturas renderizadas sin ventana, contra qué servidor) y que **no vio la aplicación en un navegador real** si es el caso.

## 8. Qué asume la plantilla del servidor

Es la convención común de la familia (`docs/CONVENCION_REST.md`). Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicios`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- **URL base completa** con prefijo común y «/» final (R1); las rutas de las operaciones son relativas y sin barra inicial; los segmentos variables van codificados (R10).
- Toda respuesta (también las de error) trae `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación; los errores usan códigos HTTP 4xx/5xx por categoría (R5 a R7). En un error los campos propios se omiten y las listas llegan vacías.
- **Lo que el código no puede imponer** (queda como acuerdo con el servidor): todos los datos viajan como texto (R9), los nombres de campos son fijos y en `camelCase` (R4) y los errores de plataforma (ruta, verbo, `Content-Type`, JSON mal formado, error interno) también traen la envoltura (R6).
- **Autenticación:** no la asume la plantilla. Cómo se identifica quien opera (cabecera `Basic` por petición, token, credenciales en el cuerpo, nada) lo define cada servidor y su cliente lo implementa en `api` (la cabecera) y `servicios`/`modelo` (la sesión); se documenta en el `docs/PROJECT.md` del proyecto. `TraductorApi.autorizacionBasic` solo arma el valor `Basic`.
- Para un servicio **SOAP** usar la plantilla `soap_java_web`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `ClassNotFoundException: com.google.gson…` en Payara | Gson no quedó en el WAR (alcance `provided` por error): debe ser `compile` |
| «Servidor no disponible, intente más tarde» | Servidor apagado, API no desplegada o `URL_BASE_POR_DEFECTO` distinta de la real (contexto + prefijo). Probar la URL con `curl` |
| «No se pudo interpretar la respuesta del servidor (HTTP 404)» | Ruta mal escrita o prefijo de la API ausente en la URL base: el servidor respondió con una página que no es JSON. Probar la ruta con `curl` |
| Respuesta con campos vacíos | El nombre de un campo del DTO no coincide con el del JSON (Gson no avisa: lo deja `null`); los nombres son el contrato con el servidor |
| `IllegalArgumentException` al armar la URI / «URL no válida» | Segmento de ruta sin codificar (espacios, tildes): usar `TraductorApi.codificar`; o `URL_BASE_POR_DEFECTO` mal escrita |
| El servidor rechaza la petición por `Content-Type` o por JSON mal formado | Cuerpo armado a mano: usar `TraductorApi.cuerpo` (genera un objeto JSON con campos de texto) |
| Conflictos de clases en Payara (`ClassCastException`) | Se empaquetó una API que Payara ya aporta (JAX-RS, JSON-B, etc.) dentro del WAR. Dejar solo Gson como dependencia `compile` |
| 404 al abrir la aplicación | El context root no es el supuesto: ver `glassfish-web.xml` y *Applications* en la consola de Payara |
| 500 al abrir una pantalla | Error en un JSP o *tag file* (atributo mal escrito, etiqueta sin cerrar): revisar `server.log` |
| Tildes o «ñ» corruptas en los datos enviados | Se quitó `FiltroCodificacion` o un JSP sin `pageEncoding="UTF-8"` |
| El usuario ve la sesión de otro | El estado del empleado se guardó en un bean `@ApplicationScoped` o en un campo estático; debe vivir solo en la `HttpSession` |
| Pantalla «congelada» con el indicador girando | El navegador restauró la página en estado ocupado con «Atrás»: `app.js` lo limpia en `pageshow`; si se cambió el script, conservarlo |
