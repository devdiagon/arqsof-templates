
# Plantilla: cliente web Java (Jakarta EE) para APIs REST

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga una API **REST/JSON** (JAX-RS en Jakarta EE)
con el esquema de respuesta de esta familia de proyectos (`estado`, `codigo`, `mensaje` + campos de la operación; token Bearer). Contiene lo que **todo**
cliente necesita y deja claramente marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java 21. **Plataforma:** Jakarta EE 11 sobre Payara 7 (WAR). **Interfaz:** servlets como controladores + **JSP** como vistas, escritas a código, sin frameworks de UI (ni JSF ni JS pesado). **Arquitectura:** MVC por capas.
- **Transporte:** `java.net.http.HttpClient` (JDK, sin dependencia). **JSON:** Gson 2.11.0 (compile: entra en el WAR).
- **El navegador no habla con la API:** quien llama al servidor es el servlet (el servidor del cliente web), nunca JavaScript. Por eso la API REST no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida). Se aplica con **componentes JSP reutilizables** (`WEB-INF/tags`) y un único `estilos.css`.
- **Verificada:** `mvn package` → BUILD SUCCESS, **10 pruebas JUnit en verde** (servicio de autenticación y reglas heredadas de `ServicioBase`, transporte), sin servidor; el WAR solo empaqueta Gson.
  **No se ha ejecutado:** el WAR desplegado en Payara 7, la comunicación contra un servidor real, la presentación en un navegador (oscura, clara, columna única < 900 px) ni la ejecución desde NetBeans.
  Las pantallas, los componentes JSP, los filtros y el estilo vienen sin cambios de la plantilla web SOAP, donde sí se verificaron.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21**, Maven y **Payara 7** registrado como servidor; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`) y poner el mismo nombre en el **context root** de `src/main/webapp/WEB-INF/glassfish-web.xml`. El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor** en un único archivo: `src/main/java/ec/edu/monster/api/ConfiguracionApi.java`
   (`BASE_POR_DEFECTO` = contexto + prefijo de la API, `RUTA_LOGIN`). Ver §8.
5. **Poner el nombre de la aplicación** en `WEB-INF/web.xml` (`context-param` `nombreAplicacion`: título de la pestaña y de la pantalla de acceso) y, si se quiere, **sustituir la imagen**
   `src/main/webapp/recursos/img/fondo_app.jpeg` (es una imagen de ejemplo; ver `docs/CLIENT_STYLES_JAVA.md` §6 para los requisitos de la imagen).
6. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
7. **Probar el servidor** (Postman/curl contra `POST <BASE>/auth/login`), ejecutar el proyecto en Payara (*Run* en NetBeans) y abrir `http://localhost:8080/<context-root>/login`. El login debe funcionar tal cual.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Login (compartido)** | `controlador/LoginControlador`, `vista/login.jsp`, `servicio/ServicioAutenticacion`, `dto/RespuestaLoginDto` | Solo la pantalla de destino (`ContextoWeb.PANTALLA_INICIAL`) y, si el servidor difiere, `RUTA_LOGIN` y `SolicitudLoginDto` |
| **Núcleo de servicios** | `servicio/ServicioBase` (sesión, `token()`, `get`, `post`, `postPublico`, `codificar`, interpretación del código HTTP) | No; los servicios del dominio la extienden |
| **Sesión y errores** | `modelo/Sesion`, `excepciones/ExcepcionServicio` | No |
| **Transporte HTTP** | `api/ClienteApi` (interfaz), `api/ClienteApiHttp`, `api/RespuestaHttp`, `api/ConfiguracionApi` | `ConfiguracionApi` siempre; el resto no |
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaLoginDto`, `RespuestaListaDto`, `SolicitudLoginDto` | Se agregan DTO del dominio al lado |
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
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin(), servidorNoDisponible()
├── dto/           RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto, SolicitudLoginDto    POJOs para Gson
├── api/           ConfiguracionApi, ClienteApi, ClienteApiHttp, RespuestaHttp
├── servicio/      ServicioBase, ServicioAutenticacion     única capa que conoce api y dto
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

Dependencias permitidas: `vista (JSP) ← controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `api` ni `dto`** y recibe datos solo como atributos de la petición (`request.setAttribute`). Si el contrato HTTP cambia solo se tocan `api`, `dto` y `servicio`.
Los JSP viven en `WEB-INF/vistas`: el navegador no puede pedirlos directamente; solo los muestran los controladores (`ContextoWeb.mostrar`).

**Una petición, de punta a punta:** navegador → `FiltroCodificacion` → `FiltroAcceso` (si la ruta es protegida) → servlet (`*Controlador`) → servicio (por petición, con la `Sesion` del usuario) → `ClienteApi` (HTTP/JSON) → el servlet deja datos en la petición → JSP.
El estado del usuario (el token) vive en la **sesión HTTP** (`ContextoWeb.sesion`), nunca en un bean `@ApplicationScoped`: lo compartirían todos los usuarios. El transporte, en cambio, **sí** es uno solo para toda la aplicación (`ContextoWeb.api()`).

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET /items` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **DTO** (si la respuesta no es login ni lista) — POJO que extienda `RespuestaBaseDto`; los nombres de campo son los del JSON del servidor.
   Para cuerpos de petición, un `SolicitudXxxDto` con los campos del JSON. Todo dato viaja como **texto** (`"valor": "1.5"`).
2. **Servicio** — `servicio/ServicioItems extends ServicioBase`:
   ```java
   public List<String> items() {
       return List.copyOf(get("/items", RespuestaListaDto.class).getElementos());   // get() lanza AUTH-002 si no hay sesión
   }
   ```
   Para segmentos de ruta usar `codificar(texto)`; para `POST` protegido, `post(ruta, dto, Clase.class)`.
   Aquí van las reglas **del cliente** (campo vacío, formato, normalizar la coma decimal); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
   En `ContextoWeb` agregar `static ServicioItems items(HttpServletRequest p) { return new ServicioItems(api(), sesion(p)); }`: se arma **por petición** con la `Sesion` del usuario (los servicios no guardan estado propio).
3. **Controlador** — molde: `InicioControlador` (y el bloque de su Javadoc). Un servlet `@WebServlet("/items")`: llama al servicio, deja el resultado con `request.setAttribute(...)` y muestra el JSP con `ContextoWeb.mostrar(peticion, respuesta, "items")`.
   Atrapa `ExcepcionServicio`: si `e.exigeLogin()` → `ContextoWeb.volverAlLogin(peticion, respuesta, e.getMessage())`; si no, `setAttribute("error", e.getMessage())` (solo el mensaje).
   Para un formulario: `doPost` valida lo trivial, llama al servicio y vuelve a mostrar la misma pantalla con el resultado o el error (no pierde lo escrito: devolver los valores como atributos).
4. **Ruta protegida** — agregar `"/items"` a `urlPatterns` de `filtro/FiltroAcceso`. (Sin esto la pantalla abriría sin sesión y fallaría con `AUTH-002` al llamar al servidor.)
5. **Vista** — `WEB-INF/vistas/items.jsp`, **solo con componentes `ui:`** (ver `inicio.jsp`, `login.jsp` y la tabla de `docs/CLIENT_STYLES_JAVA.md` §7.3). Sin `class`, sin `style`, sin lógica; muestra lo que dejó el controlador (`${items}`, `${error}`).
   Los enlaces a otras pantallas son `<ui:boton href="/otra">` (relativos al contexto) o `<ui:lista-acciones>` con datos del servidor.
6. **Pruebas** — `ClienteApi` es una interfaz de un método: el doble es una lambda que devuelve `RespuestaHttp(codigo, json)` (ver `ServicioAutenticacionTest`):
   casos mínimos del servicio (OK, error de negocio, servidor no disponible, sesión vencida, valor con coma enviado con punto). Los controladores y los JSP se verifican **ejecutando la aplicación** (§7).

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP** (`ServicioBase.interpretar`): 2xx → DTO; otro → `ExcepcionServicio` con `codigo` y `mensaje` del cuerpo (o «Error del servidor (HTTP n)» si no es JSON).
  Un `401` en una operación protegida descarta el token. Sin respuesta (servidor caído, timeout) → `RED` / «Servidor no disponible, intente más tarde».
- **El token** vive en `Sesion` (en la sesión HTTP), va en `Authorization: Bearer`, no se guarda en disco, ni en cookies propias, ni en logs, ni en la página. Ante `AUTH-002`/`AUTH-003` o un `401` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
  `FiltroAcceso` solo comprueba que *haya* token; si el servidor ya lo venció, lo detecta el servicio al llamar.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio). El campo numérico de la vista es `type="text"` con `inputmode="decimal"`, no `type="number"`.
- Mostrar solo el **mensaje** del servidor, nunca el código. Textos propios del cliente **con tildes**.
- **Escapar todo texto dinámico** en los JSP (`<c:out>`; los componentes ya lo hacen). La contraseña nunca se devuelve a la página.
- Tras autenticar se renueva el identificador de sesión (`changeSessionId`: evita la fijación de sesión). La cookie es `HttpOnly` y no se admite el id en la URL (`web.xml`).
- Un fallo de red no tumba la aplicación: se muestra el mensaje y la pantalla sigue usable. El WAR arranca aunque el servidor esté apagado (no hay conexión previa; cada llamada lo intenta de nuevo).
- Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
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

- **Servidor por defecto:** `ConfiguracionApi.BASE_POR_DEFECTO`. Sin recompilar: propiedad de la JVM de Payara `-Dapi.base.url=http://host:8080/Contexto/api` (en la consola de administración → *Configurations → server-config → JVM Settings → JVM Options*; requiere reiniciar el dominio) o la variable de entorno `API_BASE_URL` del proceso de Payara.
- Si el servidor está apagado o la URL es incorrecta, cada operación muestra «Servidor no disponible, intente más tarde»; no hay que reiniciar la aplicación.
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
Probar el servidor sin la aplicación: `curl -X POST <BASE>/auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"contrasenia\":\"...\"}"`.

Verificación con la aplicación desplegada (los controladores y los JSP **no** se cubren con JUnit):
1. Sin sesión, abrir una ruta protegida (p. ej. `/inicio`): debe redirigir a `/login`.
2. Login con credenciales incorrectas: muestra el mensaje del servidor; correcto: entra a la pantalla inicial; «Cerrar sesión» vuelve al login y «Atrás» no muestra la pantalla protegida.
3. Con el servidor apagado: el mensaje de error aparece y la aplicación sigue respondiendo.
4. Ventana ancha (≥ 900 px: imagen a la derecha) y estrecha (columna única, sin imagen, sin scroll horizontal).
5. Revisar `server.log` de Payara tras recorrer las pantallas.

Un agente que no pueda abrir el navegador debe decir explícitamente qué verificó (pruebas, peticiones HTTP con `curl`, capturas renderizadas sin ventana, contra qué servidor) y que **no vio la aplicación en un navegador real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `ServicioAutenticacion`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- Login: `POST` a `ConfiguracionApi.RUTA_LOGIN` con `{"usuario","contrasenia"}` → `200` con `{estado, codigo, mensaje, token}`.
- Las operaciones protegidas reciben `Authorization: Bearer <token>`.
- Toda respuesta (también las de error) trae `estado`, `codigo` y `mensaje`; los errores usan códigos HTTP 4xx/5xx.
- `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`); además, cualquier `401` en una operación protegida.
- Si el servidor usa otra forma de autenticación (cookies, API key, OAuth), ampliar `ClienteApiHttp` y `ServicioBase`.
- La plantilla **no está verificada** contra ningún servidor concreto: comprobar el contrato real (rutas, campos, códigos) antes de construir el dominio.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| 404 al abrir la aplicación | El context root no es el supuesto: ver `glassfish-web.xml` y *Applications* en la consola de Payara |
| 500 al abrir una pantalla | Error en un JSP o *tag file* (atributo mal escrito, etiqueta sin cerrar): revisar `server.log` |
| «Servidor no disponible, intente más tarde» | Servidor apagado, API no desplegada o `BASE_POR_DEFECTO` distinta de la real (contexto + prefijo). Probar la URL con `curl` desde la máquina de Payara |
| «Error del servidor (HTTP 404)» | Ruta mal escrita o prefijo de la API ausente en `BASE_POR_DEFECTO` |
| Respuesta con campos vacíos | El nombre de un campo del DTO no coincide con el del JSON (Gson no avisa: lo deja `null`); los nombres son el contrato con el servidor |
| Tildes o «ñ» corruptas en los datos enviados | Se quitó `FiltroCodificacion` o un JSP sin `pageEncoding="UTF-8"` |
| Una pantalla protegida abre sin sesión | Su ruta no está en `urlPatterns` de `FiltroAcceso` |
| El usuario ve la sesión de otro | El token se guardó en un bean `@ApplicationScoped` o en un campo estático; debe vivir solo en la `HttpSession` |
| Pantalla «congelada» con el indicador girando | El navegador restauró la página en estado ocupado con «Atrás»: `app.js` lo limpia en `pageshow`; si se cambió el script, conservarlo |
