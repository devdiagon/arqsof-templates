# Plantilla: cliente de escritorio Java (Swing) para APIs REST

Base reutilizable para construir el cliente de escritorio de **cualquier sistema** cuyo servidor exponga una API **REST/JSON** (JAX-RS en Jakarta EE)
con el esquema de respuesta de esta familia de proyectos (`estado`, `codigo`, `mensaje` + campos de la operación; token Bearer). Contiene lo que **todo**
cliente necesita y deja marcado lo que cambia con el **dominio**.

- **Lenguaje:** Java 21. **Interfaz:** Swing + FlatLaf, vistas **escritas a código**. **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** `java.net.http.HttpClient` (JDK, sin dependencia). **JSON:** Gson 2.11.0.
- **Estilo:** `docs/CLIENT_STYLES_JAVA.md`.
- **Verificada:** `mvn test` → 15 pruebas JUnit en verde (sin servidor ni pantalla) y `mvn package` genera el jar normal y el `-shaded.jar`.
  **No se ha ejecutado:** la ventana real, la comunicación contra un servidor real ni el jar `-shaded` con `java -jar`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** y usa este README solo como guía.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId` y `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor** en un único archivo: `src/main/java/ec/edu/monster/api/ConfiguracionApi.java`
   (`BASE_POR_DEFECTO` = contexto + prefijo de la API, `RUTA_LOGIN`). Ver §8.
5. **Poner el nombre de la aplicación** en `vista/Textos.java` (título de la ventana y de la pantalla de acceso) y, si se quiere, **sustituir la imagen**
   `src/main/resources/ec/edu/monster/estaticos/fondo_app.jpeg` (es una imagen de ejemplo; ver `docs/CLIENT_STYLES_JAVA.md` §6 para los requisitos de la imagen).
6. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
7. **Probar el servidor** (Postman/curl contra `POST <BASE>/auth/login`) y ejecutar: `mvn compile exec:java`. El login debe funcionar tal cual.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Login (compartido)** | `vista/LoginVista`, `controlador/LoginControlador`, `servicio/ServicioAutenticacion`, `dto/RespuestaLoginDto` | Solo la pantalla de destino (en `Main`) y, si el servidor difiere, `RUTA_LOGIN` y `SolicitudLoginDto` |
| **Núcleo de servicios** | `servicio/ServicioBase` (sesión, `token()`, `get`, `post`, `postPublico`, `codificar`, interpretación del código HTTP) | No; los servicios del dominio la extienden |
| **Sesión y errores** | `modelo/Sesion`, `excepciones/ExcepcionServicio` | No |
| **Transporte HTTP** | `api/ClienteApi` (interfaz), `api/ClienteApiHttp`, `api/RespuestaHttp`, `api/ConfiguracionApi` | `ConfiguracionApi` siempre; el resto no |
| **DTO base** | `dto/RespuestaBaseDto`, `RespuestaLoginDto`, `RespuestaListaDto`, `SolicitudLoginDto` | Se agregan DTO del dominio al lado |
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
├── Main.java                   punto de composición (Sesion, ClienteApi, servicios, vistas, controladores) y navegación
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin(), servidorNoDisponible()
├── dto/           RespuestaBaseDto, RespuestaLoginDto, RespuestaListaDto, SolicitudLoginDto    POJOs para Gson
├── api/           ConfiguracionApi, ClienteApi, ClienteApiHttp, RespuestaHttp
├── servicio/      ServicioBase, ServicioAutenticacion     única capa que conoce api y dto
├── controlador/   EjecutorAsincrono, LoginControlador, InicioControlador
└── vista/         Pantalla, Textos, VentanaPrincipal, LoginVista, InicioVista
    └── estilo/    Tema, EstilosUi, PanelRedondeado, PanelMensaje, PanelImagen, IndicadorProgreso
src/main/resources/ec/edu/monster/estaticos/   fondo_app.jpeg (imagen de ejemplo)
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor ni pantalla), misma estructura
docs/CLIENT_STYLES_JAVA.md              guía de estilos
```

Dependencias permitidas: `vista → controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `api` ni `dto`.** Si el contrato HTTP cambia solo se tocan `api`, `dto` y `servicio`.

## 4. Receta: agregar una operación REST y su pantalla

Ejemplo: `GET /items` que devuelve una lista. Siempre el mismo orden, de abajo hacia arriba:

1. **DTO** (si la respuesta no es login ni lista) — POJO que extienda `RespuestaBaseDto`; los nombres de campo son los del JSON del servidor.
   Para cuerpos de petición, un `SolicitudXxxDto` con los campos del JSON. Todo dato viaja como **texto** (`"valor": "1.5"`).
2. **Servicio** — `servicio/ServicioItems extends ServicioBase`:
   ```java
   public List<String> items() {
       return List.copyOf(get("/items", RespuestaListaDto.class).getElementos());   // get() lanza AUTH-002 si no hay sesión
   }
   ```
   Para segmentos de ruta usar `codificar(texto)`; para `POST` protegido, `post(ruta, dto, Clase.class)`. Se crea en `Main` con **el mismo** `ClienteApi` y la **misma** `Sesion`.
   Aquí van las reglas **del cliente** (campo vacío, coma→punto); las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
3. **Vista** — `XxxVista extends JPanel` **pasiva**, construida solo con `EstilosUi` y `Tema` (ver `InicioVista`, `LoginVista`): getters, `alPulsarXxx(...)`, `mostrarError/mostrarResultado`, `setOcupado(boolean)`.
4. **Controlador** — molde: `LoginControlador`. Llama al servicio **con `EjecutorAsincrono.ejecutar(tarea, alTerminar, alFallar)`** (nunca en el hilo de eventos),
   activa `setOcupado`, muestra `error.getMessage()`; si `error.exigeLogin()` avisa con un callback para volver al login.
   Con `error.servidorNoDisponible()` se conservan los datos del formulario.
5. **Pantalla** — constante en `vista/Pantalla` y, en `Main`: vista, `ventana.agregar(...)`, controlador y navegación.
6. **Pruebas** — `ClienteApi` es una interfaz de un método: el doble es una lambda que devuelve `RespuestaHttp(codigo, json)` (ver `ServicioAutenticacionTest`, `LoginControladorTest`).

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP** (`ServicioBase.interpretar`): 2xx → DTO; otro → `ExcepcionServicio` con `codigo` y `mensaje` del cuerpo (o «Error del servidor (HTTP n)» si no es JSON).
  Un `401` en una operación protegida descarta el token. Sin respuesta (servidor caído, timeout) → `RED` / «Servidor no disponible, intente más tarde».
- **El token** vive en `Sesion` (memoria), va en `Authorization: Bearer`, no se guarda en disco ni en logs. No se cuenta el tiempo en el cliente: se reacciona al `401`.
- Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
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

- **Servidor por defecto:** `ConfiguracionApi.BASE_POR_DEFECTO`. Sin recompilar: `-Dapi.base.url=http://host:8080/Contexto/api` o la variable de entorno `API_BASE_URL`.
- `Main` no comprueba la conexión al arrancar: con el servidor apagado la ventana abre y el login muestra «Servidor no disponible, intente más tarde».
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`).
- **Consola:** `mvn compile exec:java` (`exec:java` no compila); con otro servidor: `mvn compile exec:java -Dapi.base.url=http://192.168.1.10:8080/Contexto/api`.
- **Jar ejecutable:** `mvn package` genera `target/<artifactId>-<version>-shaded.jar` (`maven-shade-plugin`, incluye Gson y FlatLaf): `java -jar target/…-shaded.jar`.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor ni pantalla)
mvn compile exec:java        # ejecutar contra el servidor configurado
```
Probar el servidor sin la aplicación: `curl -X POST <BASE>/auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"contrasenia\":\"...\"}"`.

Un agente que no pueda abrir la ventana debe decir explícitamente qué verificó (pruebas, renders sin mostrar la ventana, contra qué servidor) y que **no vio la ventana real**.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `ServicioAutenticacion`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- Login: `POST` a `ConfiguracionApi.RUTA_LOGIN` con `{"usuario","contrasenia"}` → `200` con `{estado, codigo, mensaje, token}`.
- Las operaciones protegidas reciben `Authorization: Bearer <token>`.
- Toda respuesta (también las de error) trae `estado`, `codigo` y `mensaje`; los errores usan códigos HTTP 4xx/5xx.
- `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`); además, cualquier `401` en una operación protegida.
- Si el servidor usa otra forma de autenticación (cookies, API key, OAuth), ampliar `ClienteApiHttp` y `ServicioBase`.
- La plantilla **no está verificada** contra ningún servidor concreto: comprobar el contrato real (rutas, campos, códigos) antes de construir el dominio.
