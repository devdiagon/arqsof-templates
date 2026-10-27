# Plantilla: cliente web Java (Jakarta EE) para servicios SOAP

Base reutilizable para construir el cliente web de **cualquier sistema** cuyo servidor exponga servicios **SOAP**
(JAX-WS) con el esquema de respuesta de esta familia de proyectos. Es la plantilla **mínima**: trae el transporte, los tipos comunes, la verificación de la envoltura,
los componentes JSP con sus estilos y el contexto web, y deja marcado lo que cambia con el **dominio**. No incluye inicio de sesión ni pantallas de dominio: se construyen con la receta de §4.

Sigue `ARQUITECTURA_CLIENTE.md` (v1) y `ESPECIFICACION_CLIENTES.md`. Si este README y esa arquitectura discrepan, **prevalece la arquitectura**.

- **Lenguaje:** Java 21. **Plataforma:** Jakarta EE 11 sobre Payara 7 (WAR). **Interfaz:** servlets como controladores + **JSP** como vistas, escritas a código, sin frameworks de UI (ni JSF ni JS pesado). **Arquitectura:** capas.
- **Transporte:** JAX-WS con **proxy generado**: `wsimport` (`jaxws-maven-plugin`) genera las clases a partir de una copia del WSDL del servidor que va dentro del proyecto;
  `ws/ConfiguracionServicio` fija la URL completa y los timeouts, y `ws/TraductorRed` traduce los fallos de red a `RED`. El WSDL **no se descarga en ejecución**. No se usa el asistente *Web Service Client* de NetBeans.
  La implementación de JAX-WS la aporta Payara (`webservices-rt` va en `provided`): **no** se empaqueta `jaxws-rt` como en escritorio.
- **El navegador no habla SOAP:** quien llama al servidor es el servlet (el servidor del cliente web), nunca JavaScript. Por eso el servidor SOAP no necesita CORS.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md` (paleta oscura/clara, tipografía, medidas, componentes, disposición dividida). Se aplica con **componentes JSP reutilizables** (`WEB-INF/tags`) y un único `estilos.css`.
- **Convención SOAP:** sigue `docs/CONVENCION_SOAP.md` (TNS fijo `http://ws.monster.edu.ec/`, R2). Pruebas: `docs/CONVENCION_PRUEBAS.md`.
- **Autenticación:** **fuera de la plantilla base.** Todo proyecto define su login y su sesión. La plantilla deja el punto de enganche: `/inicio` (pantalla inicial) y `login.jsp`, una pantalla de acceso **pasiva** (sin controlador) lista para usar.
- **Verificada:** `mvn test` → BUILD SUCCESS, 25 pruebas JUnit en verde (5 de la verificación de la envoltura, 3 del cliente perezoso, 7 de configuración de la URL, 5 de traducción de red y
  5 de conformidad de ejemplo contra un servidor falso local, ejemplo heredado y fuera de alcance), sin servidor externo. `mvn package` genera el WAR.
  Los JSP y los tag files de **todos** los componentes se desplegaron en Payara 7 (en una copia temporal con una página que los usa) y se vieron renderizados en un navegador sin ventana; no queda ninguna expresión EL sin resolver.
  **No se ha ejecutado:** ninguna operación de dominio, el login y el filtro de acceso (no forman parte de la plantilla), ni el comportamiento con el servidor SOAP apagado (solo lo cubre una prueba unitaria del reintento del cliente perezoso).

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `docs/PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21**, Maven y **Payara 7** registrado como servidor; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`) y poner el mismo nombre en el **context root** de `src/main/webapp/WEB-INF/glassfish-web.xml`. El paquete **`ec.edu.monster`** es la convención y **no se cambia**: cada capa es un paquete directo bajo ella.
4. **Configurar el servidor SOAP:** en `ws/ConfiguracionServicio.java`, `URL_POR_DEFECTO` = **URL completa del servicio** (sin `?wsdl`, sin «/» final).
   Con el servidor **ya desplegado**, copiar su WSDL y su `.xsd` a `src/main/resources/wsdl/` y generar el proxy (§4, paso 1). El namespace y el nombre del servicio salen del WSDL; no hay nombre de puerto ni «base + servicio»: ver §8.
   **Si el servidor publica varios servicios** (cada uno con su WSDL y su **URL completa**; R9): una ejecución de `wsimport` por servicio, **cada una con su propio `packageName`**
   (p. ej. `ec.edu.monster.ws.generado.consulta`; los WSDL de un mismo servidor suelen definir los mismos tipos y en un paquete común chocan), y en `ConfiguracionServicio` una constante y un método
   `urlConfigurada("consulta", URL_CONSULTA)` por servicio; cada uno se sobrescribe con `-Dws.url.consulta=...` o `WS_URL_CONSULTA`.
5. **Poner el nombre de la aplicación** en `WEB-INF/web.xml` (`context-param` `nombreAplicacion`: título de la pestaña y de la pantalla de acceso) y, si se quiere, **sustituir la imagen**
   `src/main/webapp/recursos/img/fondo_app.jpeg` (es una imagen de ejemplo; ver `docs/CLIENT_STYLES_JAVA.md` §6 para los requisitos).
6. **Completar `docs/PROJECT.md`** y mantenerlo al día. Retirar los ejemplos de conformidad (`ConvencionClienteTest`, `src/test/resources/wsdl/WSEjemplo.*` y la ejecución `generar-proxy-ejemplo` del `pom.xml`).
7. Ejecutar el proyecto en Payara (*Run* en NetBeans) y abrir `http://localhost:8080/<context-root>/`.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Núcleo de servicios** | `servicios/ServicioBase` (verifica la envoltura: no OK → `ExcepcionServicio`; nula → `SRV-000`) | No; los servicios del dominio la extienden |
| **Errores** | `excepciones/ExcepcionServicio` (códigos propios `RED` y `SRV-000`) | No |
| **Proxy generado** | `src/main/resources/wsdl/` → `wsimport` (`pom.xml`) → `target/generated-sources` (`ec.edu.monster.ws.generado`) | Se **agrega** el WSDL del servidor (§4, paso 1) |
| **Transporte SOAP** | `ws/ConfiguracionServicio`, `ws/TraductorRed`, `ws/ClientePerezoso` | `ConfiguracionServicio` siempre (URL); el resto no |
| **DTO base** | `dto/RespuestaBaseDto` (POJO sin anotaciones) | Se agregan DTO del dominio al lado |
| **Contexto web** | `controlador/ContextoWeb` (mostrar vistas, redirigir, resultado y aviso de una sola lectura, cerrar sesión; punto de composición de los clientes) | Se agrega un cliente y un método por servicio |
| **Codificación y textos** | `filtro/FiltroCodificacion` (UTF-8); `filtro/ImportaTextos` (hace visible `Textos` en las expresiones EL: `${Textos.CODIGO_CUENTA}`, `${Textos.saludo(nombre, paterno)}`) | No |
| **Estilos y componentes** | `webapp/recursos/css/estilos.css`, `recursos/js/app.js`, `WEB-INF/tags/*.tag`, `docs/CLIENT_STYLES_JAVA.md` | Colores solo si se cambia de presentación; **no** se escriben clases ni estilos en los JSP |
| **Textos** | `vista/Textos` (los que necesitan los controladores) y `web.xml` (`nombreAplicacion`, `tema`) | Se agregan los textos del dominio |
| **Punto de enganche** | `controlador/InicioControlador` + `vista/inicio.jsp`; `vista/login.jsp` (pasiva) | Se reemplazan/usan según el proyecto |
| **Dominio (vacío)** | `modelo/` | Nuevos `dto/`, `servicios/`, `controlador/`, `vista/`, interfaz `XCliente` y envoltorio `XClienteWS` en `ws/` |

Las zonas a adaptar están marcadas con el comentario **`DOMINIO`** en el código (`grep -rn "DOMINIO" src pom.xml`).

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── modelo/                     datos propios del cliente (p. ej. la sesión del empleado, si el proyecto tiene login)
├── excepciones/   ExcepcionServicio
├── dto/           RespuestaBaseDto                       POJOs sin anotaciones
├── ws/            ConfiguracionServicio, TraductorRed, ClientePerezoso     + XCliente / XClienteWS por proyecto (envuelve el puerto generado)
├── servicios/     ServicioBase                           única capa que conoce la interfaz del cliente y los dto
├── controlador/   ContextoWeb, InicioControlador         servlets
├── vista/         Textos                                 textos que usan los controladores (las páginas son JSP, en WEB-INF)
└── filtro/        FiltroCodificacion, ImportaTextos      transversal a las pantallas
src/main/resources/wsdl/             copia del WSDL del servidor (la crea el proyecto)
src/main/webapp/
├── recursos/      css/estilos.css, js/app.js, img/fondo_app.jpeg            lo único público (se sirve tal cual)
└── WEB-INF/       web.xml, beans.xml, glassfish-web.xml                     descriptores
    ├── tags/      pantalla, formulario, progreso, encabezado, campo, campo-con-acciones, fila-campos, selector, mensaje, boton, fila-botones, lista-acciones, cuadricula-acciones, tarjeta, tarjetas, tabla, cerrar-sesion   componentes (.tag)
    └── vistas/    login.jsp, inicio.jsp                                     pantallas; no accesibles por URL directa
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor), misma estructura
docs/                                   CLIENT_STYLES_JAVA, CONVENCION_SOAP, CONVENCION_PRUEBAS, PROJECT
```

Dependencias: `vista (JSP) ← controlador → servicios → ws | dto`; `modelo`, `dto` y `excepciones` son transversales.
**La vista nunca importa `ws`** y recibe datos solo como atributos de la petición (`request.setAttribute`). Si el WSDL cambia solo se tocan `ws`, `dto` y `servicios`.
Los JSP viven en `WEB-INF/vistas`: el navegador no puede pedirlos directamente; solo los muestran los controladores (`ContextoWeb.mostrar`).
Las clases generadas por wsimport solo las conoce la implementación `XClienteWS`.

**Una petición, de punta a punta:** navegador → `FiltroCodificacion` → servlet (`*Controlador`) → servicio → interfaz del cliente (proxy SOAP) → el servlet deja datos en la petición → JSP.
**Estado:** los clientes SOAP (puertos generados) son **uno solo por servicio para toda la aplicación** (`ClientePerezoso`); en cambio el estado de cada usuario (su sesión, si hay login) vive **solo en su sesión HTTP**,
nunca en un bean `@ApplicationScoped` ni en un campo estático: lo compartirían todos los usuarios. Si el proyecto tiene login, se crea un contexto por sesión HTTP (la `Sesion` del empleado y los servicios que la reciben) y se guarda en ella.

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
   El cliente se crea **una vez** en `ContextoWeb`: `ClientePerezoso.de(XCliente.class, () -> new XClienteWS(ConfiguracionServicio.configurar(new WSX_Service().getWSXPort())))` (se crea en el primer uso y se reintenta si falla).
3. **DTO propio** — `…Dto` en `dto` que extienda `RespuestaBaseDto`; POJO con los mismos atributos que el tipo generado. **Todos los campos son texto.**
4. **Servicio** — `servicios/XServicio extends ServicioBase`: valida lo trivial (campos vacíos), convierte los datos al formato del contrato (coma decimal → punto) y llama:
   `return verificar(cliente.obtenerItems(codigo));`. Las reglas del dominio las decide el servidor; el cliente muestra su `mensaje`.
   En `ContextoWeb`, un método `static XServicio x(HttpServletRequest p)` que lo arma con el cliente compartido (los servicios no guardan estado propio).
5. **Controlador** — un servlet `@WebServlet("/items")` (molde: `InicioControlador`): valida campos vacíos, llama al servicio, deja el resultado con `request.setAttribute(...)` y muestra el JSP con `ContextoWeb.mostrar(peticion, respuesta, "items")`.
   Atrapa `ExcepcionServicio` y deja `setAttribute("error", e.getMessage())` (solo el mensaje, nunca el código). Cualquier otra excepción: `Textos.ERROR_INESPERADO`, sin traza.
   Para un formulario que **consulta** (no cambia datos): `doPost` valida, llama al servicio y vuelve a mostrar la misma pantalla con el resultado o el error, **conservando lo escrito** (devolver los valores como atributos) salvo los datos sensibles (claves).
   Para uno que **cambia datos** (una operación, un registro): Post/Redirect/Get con resultado de una sola lectura. El `POST` valida, llama al servicio, guarda el resultado, o el error y lo escrito, con `ContextoWeb.guardarFlash(...)` y redirige al `GET` de la misma pantalla, que lo recupera con `ContextoWeb.cargarFlash(...)`.
   Así recargar la página (F5) no repite la operación. Si el proyecto lo requiere, descartar además un `POST` repetido de la misma sesión mientras el anterior sigue en curso (un indicador por sesión HTTP).
   Lo que se guarda en la sesión debe ser serializable.
6. **Vista** — `WEB-INF/vistas/items.jsp`, **solo con componentes `ui:`** y textos de `Textos` (`${Textos.X}`, no escritos a mano) (ver `inicio.jsp`, `login.jsp` y la tabla de `docs/CLIENT_STYLES_JAVA.md` §7.2). Sin `class`, sin `style`, sin lógica; muestra lo que dejó el controlador (`${items}`, `${error}`).
   Los enlaces a otras pantallas son `<ui:boton href="/otra">` (relativos al contexto) o `<ui:lista-acciones>` con datos del servidor.
7. **Pruebas** — unitarias, sin servidor (`docs/CONVENCION_PRUEBAS.md`): servicio con un doble de `XCliente` (es una interfaz) y la traducción a `RED`/`SRV-000` en `XClienteWS`.
   Los controladores y los JSP se verifican **ejecutando la aplicación** (§7).

Reglas que se repiten en todo dominio:
- **Los errores de negocio no son SOAP Fault**: HTTP 200 con `estado = ERROR`; solo red, timeout y `Fault` son `RED` (`TraductorRed`).
- **Datos sensibles** (credenciales, si hay login): solo en la sesión HTTP del servidor del cliente; nunca en disco, cookies propias, logs ni en la página, y la clave nunca se devuelve al formulario.
- Si hay login: renovar el identificador de sesión al autenticar (`changeSessionId`: evita la fijación de sesión), invalidar la sesión al cerrarla y no guardar en caché las pantallas protegidas (`Cache-Control: no-store`) para que «Atrás» no las muestre. La cookie es `HttpOnly` y no se admite el id en la URL (`web.xml`).
- Todo dato viaja como **texto**; los decimales con **punto**. El campo numérico de la vista es `type="text"` con `inputmode="decimal"`, no `type="number"`.
- Mostrar solo el **mensaje**, nunca el código. Los textos salen de `Textos` (los mismos en todos los clientes); en los JSP se leen con `${Textos.X}` (no se escriben a mano en las páginas).
- **Escapar todo texto dinámico** en los JSP (`<c:out>`; los componentes ya lo hacen).
- **Lo que se muestra en un JSP es una clase con getters, no un `record`**: las expresiones EL de Payara 7 no ven los accesores de un `record` y la página falla con HTTP 500 (`PropertyNotFoundException`).
- Un fallo de red no tumba la aplicación: se muestra el mensaje y la pantalla sigue usable. El WAR arranca aunque el servidor SOAP esté apagado.

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

- **Servidor SOAP por defecto:** `ConfiguracionServicio.URL_POR_DEFECTO` (URL completa). Sin recompilar: propiedad de la JVM de Payara `-Dws.url=http://host:8080/Contexto/Servicio` (consola de administración → *Configurations → server-config → JVM Settings → JVM Options*; requiere reiniciar el dominio) o la variable de entorno `WS_URL` del proceso de Payara.
- Si el servidor SOAP está apagado o la URL es incorrecta, la **primera operación** muestra «Servidor no disponible, intente más tarde» y las siguientes lo vuelven a intentar; no hay que reiniciar la aplicación.
  El proxy usa el WSDL empaquetado (no lo descarga), así que el WAR y el proxy se crean sin el servidor SOAP.
- **NetBeans:** *Run* despliega el WAR en Payara 7 y abre el navegador en `/<context-root>/`; el archivo de bienvenida es `/inicio`.
- **Consola de Payara / línea de comandos:** `mvn clean package` genera `target/<artifactId>-1.0-SNAPSHOT.war`; se despliega desde la consola web (`http://localhost:4848`, *Applications → Deploy*) o con `asadmin deploy`. El context root sale de `glassfish-web.xml`.
- **Sesión HTTP:** 30 minutos de inactividad (`web.xml`).

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor)
mvn clean package            # compila, prueba y genera el WAR
```
Probar el servidor SOAP sin la aplicación: abrir `<URL del servicio>?wsdl` en el navegador; si carga el XML, el cliente conectará.

Verificación con la aplicación desplegada (los controladores y los JSP **no** se cubren con JUnit):
1. Abrir la pantalla inicial y cada pantalla nueva: sin errores 500 y sin errores en `server.log`.
2. Con el servidor SOAP apagado: el mensaje de error aparece y la aplicación sigue respondiendo.
3. Ventana ancha (≥ 900 px: imagen a la derecha) y estrecha (columna única, sin imagen, sin scroll horizontal).
4. Con login: sin sesión, una ruta protegida redirige al acceso; tras cerrar sesión, «Atrás» no muestra la pantalla protegida; con dos empleados a la vez, cada uno opera con sus credenciales.

Un agente que no pueda abrir el navegador debe decir explícitamente qué verificó (pruebas, peticiones HTTP con `curl`, capturas renderizadas sin ventana, contra qué servidor) y que **no vio la aplicación en un navegador real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el servidor no cumple alguno de estos puntos, el cambio se limita a `ws` y `dto` (`docs/CONVENCION_SOAP.md`):

- SOAP 1.1, *document/literal wrapped*; elementos calificados con el TNS (R3: si el WSDL no lo declara, los campos llegan `null` sin error).
- **SOAPAction = `TNS + Servicio + "/" + operación`** (R4), que el proxy toma del WSDL.
- El puerto se pide a la clase de servicio generada (R10) y la URL completa de la configuración sustituye a la del WSDL (R9).
- Cada respuesta es un elemento `respuesta` con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los datos de la operación.
- Todo dato viaja como texto (R7); los errores de negocio van en el cuerpo con HTTP 200 (R8).
- Si el WSDL viene de .NET con `nillable`, agregar un binding con `generateElementProperty="false"` (`<bindingFiles>` del plugin).
- Para un servicio **REST** se reutiliza todo salvo `ws/` y `dto/`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `jakarta.jws.*` / `jakarta.xml.ws.*` no reconocido tras tocar el `pom.xml` | Falta `webservices-rt:4.0.6` (`provided`): el API paraguas de EE 11 no incluye JAX-WS. Restaurarlo, *Reload POM* y *Clean and Build* |
| Error al ejecutar `generate-sources` | El WSDL no está en `src/main/resources/wsdl`, el `.xsd` no está junto a él o su `schemaLocation` no apunta al archivo local |
| Cambió el servidor y el cliente no ve la operación nueva | Copiar de nuevo el WSDL y el `.xsd`, `mvn generate-sources` y agregar la operación a la interfaz del cliente y a su envoltorio (§4) |
| Respuesta con campos vacíos | Se copió un WSDL antiguo, o el envoltorio no copia un campo de la respuesta generada al DTO propio |
| Servidor .NET responde HTTP 500 *ActionNotSupported* | El WSDL copiado no trae el `soapAction` de la operación: copiar el WSDL del servidor actualizado y regenerar |
| El servidor no recibe los parámetros (llegan vacíos) | El WSDL/XSD copiado no califica los elementos (`elementFormDefault="qualified"`) |
| Conflictos de `jakarta.xml.ws` / `ClassCastException` en Payara | JAX-WS (`jaxws-rt`, `jakarta.xml.ws-api`, `jakarta.activation-api`) dentro del WAR. Dejar solo `webservices-rt` como `provided` |
| 404 al abrir la aplicación | El context root no es el supuesto: ver `glassfish-web.xml` y *Applications* en la consola de Payara |
| 500 al abrir una pantalla | Error en un JSP o *tag file* (atributo mal escrito, etiqueta sin cerrar): revisar `server.log` |
| «Servidor no disponible, intente más tarde» | Servidor SOAP apagado o URL distinta de la del servidor real. Abrir `…?wsdl` en el navegador |
| Tildes o «ñ» corruptas en los datos enviados | Se quitó `FiltroCodificacion` o un JSP sin `pageEncoding="UTF-8"` |
| El usuario ve la sesión de otro | El estado del empleado se guardó en un bean `@ApplicationScoped` o en un campo estático; debe vivir solo en la `HttpSession` |
| Pantalla «congelada» con el indicador girando | El navegador restauró la página en estado ocupado con «Atrás»: `app.js` lo limpia en `pageshow`; si se cambió el script, conservarlo |
