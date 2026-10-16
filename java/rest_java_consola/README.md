# Plantilla: cliente de consola Java para APIs REST

Base reutilizable para construir el cliente de consola de **cualquier sistema** cuyo servidor exponga una API **REST/JSON** (JAX-RS en Jakarta EE)
con el esquema de respuesta de esta familia de proyectos (`estado`, `codigo`, `mensaje` + campos de la operación; token Bearer). Es la plantilla **mínima**:
trae el transporte, los tipos comunes y la entrada/salida por consola, y deja marcado lo que cambia con el **dominio**. No incluye pantallas de ejemplo ni inicio de sesión: se construyen con la receta de §4.

- **Lenguaje:** Java 21. **Interfaz:** texto por la consola (`Scanner` / `System.out`). **Arquitectura:** MVC por capas, sin frameworks.
- **Transporte:** `java.net.http.HttpClient` (JDK, sin dependencia). **JSON:** Gson 2.11.0.
- **Verificada:** `mvn test` → BUILD SUCCESS, 3 pruebas JUnit en verde (URL base, codificación de segmentos, `esExito`), sin servidor.
  **No se ha ejecutado:** el cliente contra un servidor real, ninguna petición HTTP, la secuencia de pantallas (la plantilla no trae ninguna) ni el jar `-shaded` con `java -jar`.

> Si eres un agente que continúa un proyecto creado desde esta plantilla: lee primero el `PROJECT.md` **de ese proyecto** (estado real,
> decisiones, pendientes) y usa este README solo como guía de la estructura y de las recetas.

---

## 1. Crear un proyecto a partir de la plantilla

1. **Copiar la carpeta** completa a un directorio nuevo (sin `target/`; Maven lo regenera).
2. **Abrir en NetBeans** (*Open Project*; es un proyecto Maven) o en cualquier IDE con Maven. Requiere **JDK 21** y Maven; la primera vez descarga las dependencias (Internet).
3. **Renombrar** el proyecto en `pom.xml` (`artifactId`; opcionalmente agregar `name`). El paquete **`ec.edu.monster`** es la convención y **no se cambia**.
4. **Configurar el servidor** en `src/main/java/ec/edu/monster/api/ClienteApiHttp.java`: `BASE_POR_DEFECTO` (contexto de la aplicación + prefijo de la API, sin «/» final). Ver §8.
5. **Crear `PROJECT.md`** a partir del esqueleto de esta carpeta y mantenerlo al día.
6. **Probar el servidor** (Postman/curl, ver §7) y ejecutar: `mvn compile exec:java`. Con la plantilla sin tocar solo imprime la URL configurada y termina.

## 2. Qué es compartido y qué se adapta

| Parte | Archivos | ¿Se toca? |
|---|---|---|
| **Transporte HTTP** | `api/ClienteApi` (interfaz), `api/ClienteApiHttp`, `api/RespuestaHttp` | `BASE_POR_DEFECTO` siempre; el resto no |
| **DTO base** | `dto/RespuestaBaseDto` (`estado`, `codigo`, `mensaje`) | No; se agregan DTO del dominio al lado |
| **Sesión y errores** | `modelo/Sesion` (token), `excepciones/ExcepcionServicio` (`exigeLogin()`, `servidorNoDisponible()`) | No |
| **Entrada/salida** | `vista/VistaBase` (`pedir`, `elegir`, `mostrar`, `mostrarError`) | No; las vistas del dominio la extienden |
| **Composición** | `Main` (UTF-8 en la salida, manejo de error de arranque, `TODO` del montaje) | Se completa con los servicios, vistas y controladores del dominio |
| **Servicios, controladores, vistas** | `servicio/`, `controlador/` (carpetas vacías, `.gitkeep`), `vista/` | **Se crean** (receta §4) |

Las zonas a adaptar están marcadas con **`CAMBIAR`** y **`TODO`** en el código (`grep -rnE "CAMBIAR|TODO" src pom.xml`); `BASE_POR_DEFECTO` también trae un valor de ejemplo.

## 3. Estructura

```
src/main/java/ec/edu/monster/
├── Main.java                   punto de composición (Sesion, ClienteApi, servicios, vistas, controladores) y bucle principal
├── modelo/        Sesion                                  entidades: solo atributos, sin lógica
├── excepciones/   ExcepcionServicio                       código + mensaje; exigeLogin(), servidorNoDisponible()
├── dto/           RespuestaBaseDto (+ los del dominio)    POJOs para Gson
├── api/           ClienteApi, ClienteApiHttp, RespuestaHttp
├── servicio/      (vacía)                                 única capa que conoce api y dto
├── controlador/   (vacía)                                 coordinan vista y servicio; devuelven un Resultado
└── vista/         VistaBase (+ las del dominio)           única capa que lee el teclado y escribe en la consola
src/test/java/ec/edu/monster/           JUnit 5 (sin servidor), misma estructura
```

Dependencias permitidas: `vista ← controlador → servicio → api/dto`; `modelo` y `excepciones` no dependen de nadie.
**La vista nunca importa `api` ni `dto`**: recibe y devuelve texto y tipos simples. Si el contrato HTTP cambia solo se tocan `api`, `dto` y `servicio`.
**Solo la vista usa `System.in`/`System.out`**; ni el controlador ni el servicio imprimen ni leen.

## 4. Receta: agregar una operación REST y su pantalla de consola

Ejemplo: `GET /items` que devuelve una lista. Se sigue **siempre el mismo orden**, de abajo hacia arriba:

1. **DTO** (si la respuesta no es solo `estado`/`codigo`/`mensaje`) — POJO que extienda `RespuestaBaseDto`; los nombres de campo son los del JSON del servidor.
   Para cuerpos de petición, un `SolicitudXxxDto` con los campos del JSON. Todo dato viaja como **texto** (`"valor": "1.5"`).
   ```java
   public class RespuestaListaDto extends RespuestaBaseDto {
       protected List<String> elementos;
       public List<String> getElementos() { return elementos == null ? new ArrayList<>() : elementos; }   // nunca nulo
   }
   ```
2. **Servicio** — `servicio/ServicioXxx`. Si hay varios, una clase común `ServicioBase(ClienteApi api, Sesion sesion)` que ellos extiendan, con:
   - `token()`: lanza `AUTH-002` sin llamar al servidor si la `Sesion` no tiene token.
   - `get(ruta, Clase)`, `post(ruta, cuerpo, Clase)` (con token) y `postPublico(...)` (sin token, para el login): serializan con Gson, llaman a `api.enviar(...)` y **interpretan por código HTTP**:
     2xx → DTO; otro → `ExcepcionServicio(codigo, mensaje)` leídos del cuerpo JSON (o `HTTP-<n>` / «Error del servidor (HTTP n)» si no es JSON).
     Un `401` en una operación protegida se trata como `AUTH-002`; si `exigeLogin()`, se descarta el token.
   - `IOException` de `api.enviar` → `ExcepcionServicio(ExcepcionServicio.RED, "Servidor no disponible, intente más tarde", e)`.
   ```java
   public List<String> items() {
       return List.copyOf(get("/items", RespuestaListaDto.class).getElementos());   // get() lanza AUTH-002 si no hay sesión
   }
   ```
   Para segmentos de ruta usar `ClienteApiHttp.codificarSegmento(texto)`. Aquí van las reglas **del cliente** (campo vacío, formato, coma decimal → punto);
   las reglas del dominio las decide el servidor y el cliente muestra su `mensaje`.
   (Las plantillas `rest_java_escritorio` y `rest_java_movil` traen una `ServicioBase` con este comportamiento ya escrita y probada: usarla como molde.)
3. **Vista** — `vista/XxxVista extends VistaBase`: pide datos con `pedir(...)` / `elegir(...)`, muestra con `mostrar(...)` y `mostrarError(...)`. Sin lógica de negocio ni llamadas al servidor.
4. **Controlador** — `controlador/XxxControlador`: pide a la vista, llama al servicio, muestra el resultado o `e.getMessage()` (solo el mensaje). Devuelve un `enum Resultado` (p. ej. `SEGUIR`, `IR_AL_LOGIN`, `SALIR`).
   Si `e.exigeLogin()` → `IR_AL_LOGIN`. Si `e.servidorNoDisponible()` se muestra el mensaje y se conservan los datos ya capturados (no se cierra el programa).
5. **Composición** — en `Main`: crear la `Sesion` (**una sola** para toda la ejecución) y el `ClienteApi` (ya creados), los servicios, las vistas y los controladores, y un bucle que ejecute el controlador actual y cambie al siguiente según su `Resultado`.
   Todo `RuntimeException` no previsto se captura en el bucle (mensaje por `System.err`), nunca termina con una traza.
6. **Pruebas** — `ClienteApi` es una interfaz de un método: el doble es una lambda que devuelve `RespuestaHttp(codigo, json)` (ver `ClienteApiHttpTest` para el estilo de JUnit 5).
   Casos mínimos del servicio: OK, error de negocio, cuerpo que no es JSON, servidor no disponible, sesión vencida (`401`), coma enviada como punto.
   Los controladores se prueban con una vista falsa (subclase de `VistaBase` que devuelve respuestas fijas), sin tocar la consola.

Reglas que se repiten en todo dominio:
- **Se decide por el código HTTP**, no por `estado`: el cliente confía en el código y usa `codigo`/`mensaje` del cuerpo solo para describir el error. Sin respuesta (servidor caído, timeout) → `RED`.
- **El token** vive en `Sesion` (memoria), va en `Authorization: Bearer`, no se guarda en disco ni en logs ni se imprime. Ante `AUTH-002`/`AUTH-003` o un `401` se descarta y se vuelve al login; no se cuenta el tiempo en el cliente.
- Todo dato viaja como **texto**; los decimales con **punto** (normalizar la coma antes de enviar, en el servicio).
- Los campos del JSON pueden faltar o venir `null` (errores de plataforma): no darlos por hechos.
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

- **Servidor por defecto:** `ClienteApiHttp.BASE_POR_DEFECTO`. Sin recompilar: `-Dapi.base.url=http://host:8080/Contexto/api` o la variable de entorno `API_BASE_URL`.
- `Main` no comprueba la conexión al arrancar: con el servidor apagado el programa abre y el error aparece en la primera operación («Servidor no disponible, intente más tarde»).
- **NetBeans:** *Run* ejecuta `ec.edu.monster.Main` (propiedad `exec.mainClass` del `pom.xml`). La consola de NetBeans no es una terminal: no hay `System.console()`.
- **Terminal:** `mvn compile exec:java` (`exec:java` no compila); con otro servidor: `mvn compile exec:java -Dapi.base.url=http://192.168.1.10:8080/Contexto/api`.
- **Jar ejecutable:** `mvn package` genera `target/<artifactId>-<version>-shaded.jar` (`maven-shade-plugin`, incluye Gson): `java -jar target/…-shaded.jar`. El `dependency-reduced-pom.xml` que genera Maven en la raíz es un artefacto de la compilación; no se versiona.

## 7. Comandos y verificación

```
mvn test                     # pruebas locales (sin servidor)
mvn compile exec:java        # ejecutar contra el servidor configurado
mvn package                  # pruebas + jar normal y jar -shaded
```
Probar el servidor sin la aplicación: `curl -i -X POST <BASE>/auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"...\",\"contrasenia\":\"...\"}"`.

Verificación con el programa en marcha (las vistas y los controladores se prueban a mano o con vistas falsas):
1. Operación correcta: el resultado se muestra con el formato esperado.
2. Error de negocio: se muestra el mensaje del servidor y se vuelve al menú.
3. Con el servidor apagado: aparece «Servidor no disponible, intente más tarde» y el programa sigue respondiendo.
4. Token vencido (`401`): vuelve al login con el mensaje.
5. Tildes y «ñ» en entrada y salida (también en segmentos de ruta); entrada vacía y opción inválida en los menús.

Un agente que no pueda ejecutar el programa interactivamente debe decir explícitamente qué verificó (pruebas, ejecuciones con `stdin` redirigido, contra qué servidor) y que **no lo usó en una terminal real** si es el caso.

## 8. Qué asume la plantilla del servidor

Si el nuevo servidor no cumple alguno de estos puntos, el cambio se limita a `api`, `dto` y `servicio`:

- REST con cuerpos **JSON** UTF-8. El cliente envía siempre `Accept: application/json`, y `Content-Type: application/json; charset=UTF-8` si hay cuerpo.
- Login: `POST` a una ruta del servidor (p. ej. `/auth/login`) con `{"usuario","contrasenia"}` → `200` con `{estado, codigo, mensaje, token}`.
- Las operaciones protegidas reciben `Authorization: Bearer <token>`.
- Toda respuesta (también las de error) trae `estado`, `codigo` y `mensaje`; los errores usan códigos HTTP 4xx/5xx.
- `AUTH-002` / `AUTH-003` significan «token inválido / vencido» (`ExcepcionServicio.exigeLogin()`); además, cualquier `401` en una operación protegida.
- Si el servidor usa otra forma de autenticación (cookies, API key, OAuth), ampliar `ClienteApiHttp` y `ServicioBase`.
- La plantilla **no está verificada** contra ningún servidor concreto: comprobar el contrato real (rutas, campos, códigos) antes de construir el dominio.
- Para un servicio **SOAP** usar la plantilla `soap_java_consola`.

## 9. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| «Servidor no disponible, intente más tarde» | Servidor apagado, API no desplegada o `BASE_POR_DEFECTO` distinta de la real (contexto + prefijo). Probar la URL con `curl` |
| «Error del servidor (HTTP 404)» | Ruta mal escrita o prefijo de la API ausente en `BASE_POR_DEFECTO` |
| Respuesta con campos vacíos | El nombre de un campo del DTO no coincide con el del JSON (Gson no avisa: lo deja `null`); los nombres son el contrato con el servidor |
| `IllegalArgumentException` al armar la URI | Segmento de ruta sin codificar (espacios, tildes): usar `ClienteApiHttp.codificarSegmento` |
| Tildes o «ñ» corruptas en la salida | La terminal no está en UTF-8 (`chcp 65001`) o se quitó el `PrintStream` UTF-8 de `Main` |
| El menú repite «Opción inválida» sin parar | La entrada estándar se cerró (ver §5, «Entrada cerrada») |
| `System.console()` es `null` | Se ejecuta desde el IDE o con la entrada redirigida: usar `pedir(...)` como alternativa |
