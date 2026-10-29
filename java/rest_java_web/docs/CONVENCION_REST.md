# CONVENCION_REST — Convención de interoperabilidad REST (Java ⇄ .NET ⇄ móvil)

Reglas para que **cualquier servicio REST** del equipo —escrito en Java (JAX-RS/Jakarta EE) o en .NET (ASP.NET Core) y de cualquier dominio— sea consumible por **cualquier cliente** (Java, .NET, Android a mano) cambiando solo una URL. Es la contraparte de `CONVENCION_SOAP.md`.
No describe el dominio del servicio: eso va en `RESUMEN_FUNCIONALIDADES.md`. **Tampoco fija cómo se autentica el cliente**: el mecanismo de credenciales no es 100 % estándar y se ajusta a cada proyecto, así que se especifica en su `ENDPOINTS_REST.md` (§6). Aquí solo se define **cómo se ve el REST en el cable** y qué hace cada plataforma para producirlo.

> Estado: **diseño (2026-10-27); verificada en el servidor Java REST (2026-10-29).** R1 a R12 se tomaron de las plantillas `rest_dotnet_*` y de `ARQUITECTURA_SERVIDOR.md` (§4.3, §4.8, §8). Se aplicaron al construir el servidor Java REST sin cambios; lo comprobado y las decisiones de plataforma están en §8.1. **2026-10-29:** el cliente de consola Java REST también la aplica y se verificó a mano (§8.2). **Sin verificar:** el servidor .NET REST y los clientes REST de escritorio, web y móvil.

---

## 0. Idea central

**No se busca que los contratos sean idénticos en el código**; se busca que **la petición y la respuesta que viajan sean las mismas** en cualquier servidor de la familia, y que cada plataforma lo logre con su herramienta por defecto, sin código especial de transporte. Se consigue acordando unas pocas reglas (§1) y aplicando en cada plataforma un puñado de declaraciones (§3).

La **tabla de endpoints** de cada proyecto (`ENDPOINTS_REST.md`) es el contrato: define rutas, verbos, cuerpos, respuestas, errores y autenticación. Esta convención define lo que **todas** las tablas cumplen.

---

## 1. Reglas del contrato

| # | Regla | Por qué |
|---|---|---|
| **R1** | **URL base completa**, con prefijo común configurable (p. ej. `/api/`) y «/» final. El prefijo va **siempre después del contexto propio de la plataforma** (el *context root* en Java; la ruta base, si la hay, en .NET): `http://<host>:<puerto>/<contexto, si lo hay>/api/`. El cliente no asume host, puerto ni prefijo; las rutas se escriben **relativas y sin barra inicial** (`"cuentas/00100001/depositos"`). La URL base es una constante en **un solo lugar** del código (valor por defecto no secreto) y se puede sobrescribir sin recompilar (propiedad, variable de entorno, recurso de configuración). | Cambiar de servidor es cambiar una línea; un clon del repositorio funciona en local sin configurar secretos. |
| **R2** | **Verbos con su significado:** `GET` para consultas (sin cuerpo), `POST` para crear y para las operaciones de negocio, `PUT` para actualizar, `DELETE` para eliminar. Un verbo no permitido en una ruta da `405`. | Lo esperan por igual los servidores Java y .NET y las herramientas de prueba. |
| **R3** | **JSON UTF-8:** `Content-Type: application/json; charset=utf-8` en las peticiones con cuerpo y `Accept: application/json` en todas. Otro `Content-Type` da `415`. | Es el único formato del contrato; evita diferencias de codificación entre plataformas. |
| **R4** | **Nombres fijos** de propiedades JSON en `camelCase`, que no derivan del código (`usuario`, `codigoCuenta`, `saldoNuevo`…). Se leen **sin distinguir mayúsculas** y se **ignoran** los campos desconocidos. Ningún campo propio se llama `estado`, `codigo` ni `mensaje`. | Los clientes escritos a mano dependen de los nombres; cambiarlos obliga a actualizarlos todos. |
| **R5** | **Forma común de respuesta:** toda respuesta, de éxito o de error, es un objeto con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos propios de la operación. Éxito y error de una operación usan **el mismo tipo**; en un error los campos propios se **omiten** y las listas llegan **vacías, nunca ausentes**. | El cliente revisa siempre los mismos tres campos. Es la misma envoltura `RespuestaBase` de SOAP (R6 de `CONVENCION_SOAP.md`). |
| **R6** | **Errores de la plataforma con la misma estructura:** ruta inexistente (`404 ERR-404`), verbo no permitido (`405 ERR-405`), `Content-Type` incorrecto (`415 ERR-415`), JSON mal formado (`400 DATA-001`) y error interno (`500 ERR-500`) traen **solo** `estado`, `codigo` y `mensaje`. **Nunca** una página HTML ni un `ProblemDetails`. | Los frameworks responden por defecto con su propio formato; sin esta regla el cliente recibe cuerpos que no puede interpretar. |
| **R7** | **Código HTTP + código propio.** El servidor decide el HTTP por la **categoría** del error (§2) y siempre incluye la envoltura. El cliente decide primero por el HTTP y luego por el `codigo`, y **lee el cuerpo también cuando el HTTP es de error**: nunca descarta el cuerpo por el código (en .NET, nunca `EnsureSuccessStatusCode()`). | En REST el código HTTP forma parte del contrato; en SOAP, en cambio, los errores de negocio llegan con 200. |
| **R8** | **La autenticación no forma parte de esta convención.** El mecanismo (cabecera, cuerpo, token, sesión…) lo define cada proyecto en `ENDPOINTS_REST.md` (§6). Sí se fija: las **credenciales nunca viajan en la URL ni se registran** (ni en el servidor ni en el cliente), y una petición sin credenciales válidas recibe `401` con la envoltura y un código de categoría *No autorizado* del catálogo del proyecto. | No existe una forma estándar única de manejar credenciales; lo que sí es común es no filtrarlas y responder igual ante un fallo. |
| **R9** | **Todo dato viaja como texto**, también los importes, cantidades y resultados; los decimales con **punto**, sin coma ni notación científica; fechas en ISO‑8601. **Quien emite** (cliente o servidor) escribe siempre texto entre comillas; las respuestas nunca llevan números JSON. **El servidor, al recibir, tolera** un número o un booleano JSON sin comillas como valor de un campo (`"importe": 100.50`) y lo trata como su texto escrito (`"100.50"`), sin convertirlo ni reformatearlo: el validador aplica las mismas reglas de siempre (un `1E+2` o un `10.999` se rechaza igual, con `DATA-001`). Un objeto, una lista o un `null` equivalen a campo ausente. Interpretarlo es responsabilidad del servidor: el cliente no necesita ningún cuidado especial. | Evita diferencias de cultura, de precisión y de serialización entre plataformas, y hace que cualquier cliente funcione con cualquier servidor aunque su biblioteca JSON serialice un valor como número. |
| **R10** | **Rutas con variables codificadas:** el valor de un segmento de ruta se codifica (`Uri.EscapeDataString`, `URLEncoder`…); el servidor lo recibe ya decodificado. Los recursos van en plural, en minúscula y sin tilde; las operaciones de negocio son **subrecursos que se crean** (`POST cuentas/{codigo}/depositos`, no `POST depositar`). Filtros, orden y paginación, como parámetros de consulta. | Un código con espacios o caracteres reservados rompe la ruta si no se codifica. |
| **R11** | **Sin respuesta no hay código:** servidor caído, red o tiempo agotado no son un error de la API. El cliente fija un **tiempo límite de 10 s** y lo traduce a «Servidor no disponible, intente más tarde» (código propio del cliente `RED`). | El cliente distingue «falló la red» de «falló el negocio». |
| **R12** | **Cuerpo que no es JSON** (página de un proxy o de IIS Express, `502`): el cliente no deja escapar la excepción del deserializador; muestra un mensaje genérico con el código HTTP, si existe (código propio del cliente `SRV-000`). | Evita mostrar trazas o HTML al usuario. |

---

## 2. Código HTTP y código propio

El **HTTP de éxito es siempre `200`** (decisión del 2026-10-27): para consultas, actualizaciones, eliminaciones y también al crear, incluidas las operaciones de negocio que crean un movimiento. No se usan `201` ni `204`, porque toda respuesta lleva la envoltura y el cliente decide por el HTTP y luego por el `codigo` (R7).

El **HTTP de error** sale de la categoría de la entrada del catálogo (`ARQUITECTURA_SERVIDOR.md` §4.3), no de cada código individual:

| Categoría | HTTP | Qué hace el cliente |
|---|---|---|
| Validación (`DATA-xxx`) | `400` | Mostrar `mensaje`; se queda en el ingreso de datos |
| No autorizado | `401` | Mostrar `mensaje` y actuar según el mecanismo de autenticación del proyecto (`ENDPOINTS_REST.md`) |
| No encontrado | `404` | Recurso inexistente: mostrar `mensaje` |
| Conflicto | `409` | Duplicado o restricción violada: mostrar `mensaje` |
| Operación inválida | `422` | Una regla de negocio lo impide: mostrar `mensaje` |
| Técnico (`ERR-500`) | `500` | Mostrar `mensaje` genérico; se puede reintentar |
| Método no permitido (`ERR-405`) | `405` | Defecto del código del cliente (verbo): mostrar `mensaje` y corregir el cliente |
| Tipo no soportado (`ERR-415`) | `415` | Defecto del código del cliente (`Content-Type`): mostrar `mensaje` y corregir el cliente |

`ERR-404` (ruta inexistente) usa la categoría *No encontrado*; se distingue de un recurso inexistente (`CTA-001`, etc.) por su `codigo`, y para el cliente indica un defecto de la ruta.

### 2.1 Errores de plataforma (comunes a todos los proyectos)

Los produce el framework y no dependen del dominio: **mismos códigos, mismo HTTP y mismos mensajes en cualquier proyecto** (R6). Se dan de alta en el catálogo de errores del servidor (prefijo `ERR-` reservado para plataforma y técnicos, `ARQUITECTURA_SERVIDOR.md` §4.3) y el catálogo debe ser idéntico en las variantes Java y .NET. Como todo error, su HTTP sale de la **categoría** de su entrada (R7), sin casos especiales: `ERR-404` usa *No encontrado* y cada uno de `ERR-405` y `ERR-415` tiene su propia categoría (*Método no permitido*, *Tipo no soportado*).

| Situación | HTTP | Código | Categoría | Mensaje |
|---|---|---|---|---|
| Ruta inexistente | 404 | `ERR-404` | No encontrado | Ruta no encontrada |
| Verbo no permitido en la ruta | 405 | `ERR-405` | Método no permitido | Método no permitido |
| `Content-Type` distinto de `application/json` en una petición con cuerpo | 415 | `ERR-415` | Tipo no soportado | Tipo de contenido no soportado |
| Cuerpo JSON mal formado | 400 | `DATA-001` | Validación | Cuerpo de la petición inválido |
| Excepción no prevista | 500 | `ERR-500` | Técnico | Error interno del servidor |

Aunque SOAP no los use, un servidor que se migre a REST debe tenerlos en el catálogo **antes** de construir la capa `api`, para no cambiar luego el núcleo.

Los códigos concretos de cada dominio (`PREFIJO-NNN`) los define el servidor; el cliente solo necesita los que cambian su comportamiento y los lista en `ENDPOINTS_REST.md`. Los códigos propios del cliente (`RED`, `SRV-000`) **nunca** aparecen en el catálogo del servidor.

---

## 3. Recetas para el servidor

Marcadores: `<Recurso>` = recurso, `<op>` = operación de negocio. No hay lógica de dominio aquí: las clases de negocio no cambian.

### 3.1 Java (JAX-RS / JSON-B)
1. **Prefijo común:** una clase `Application` con `@ApplicationPath("api")` (R1) y un **context root** propio, distinto del de un WAR SOAP si ambos conviven.
2. **Recursos** con `@Path`, `@Produces`/`@Consumes` en JSON UTF-8 (R3); sin lógica: reciben, delegan al controlador y devuelven. Los **cuerpos de entrada** se reciben como texto y se leen con JSON-P (`JsonObject`): se busca cada nombre sin distinguir mayúsculas (R4; JSON-B no lo hace al deserializar) y cada valor se entrega como texto, **incluido un número JSON** (R9).
3. **Tipos de contrato** como POJO con getters y setters, nombres en `camelCase`, todo `String`, campos sin valor omitidos y listas inicializadas vacías (R4, R5, R9).
4. **`ExceptionMapper`** para ruta inexistente, verbo no permitido, `Content-Type` incorrecto, JSON mal formado y cualquier excepción no prevista, todos con la envoltura (R6). Debe estar registrado (`@Provider` dentro del paquete escaneado); si no, el servidor responde con su página HTML.
5. **HTTP por categoría** (R7): el mapeador de respuestas obtiene la categoría del catálogo a partir del código del `Resultado`.
6. **No registrar** cabeceras ni cuerpos de petición (R8).

### 3.2 .NET (ASP.NET Core)
1. **Prefijo común** con `MapGroup`/ruta base (R1) y controladores o *endpoints* que reciben, delegan al controlador del núcleo y devuelven; sin lógica.
2. **Serialización** con `System.Text.Json`: nombres en `camelCase` y **sin valores nulos**; los campos del cuerpo se entregan al validador como `string` para que el enlace de modelo no los convierta antes de validar (R4, R9), **también cuando llegan como número JSON** (R9). Por defecto `System.Text.Json` no lee un número en una propiedad `string`: hace falta un convertidor o leer el cuerpo como `JsonElement`/`JsonObject` **(no verificado: se comprueba al construir .NET REST)**.
3. **Validación automática del modelo desactivada** (`ApiController`): valida el controlador del núcleo, no el framework, para que el error salga con la envoltura (R6).
4. **Manejador de errores de plataforma** (middleware o `UseStatusCodePages`/`UseExceptionHandler`) que cubra `404`, `405`, `415`, JSON mal formado y excepciones no controladas con la envoltura; **nunca** `ProblemDetails` ni páginas HTML (R6).
5. **HTTP por categoría** (R7) igual que en Java.

---

## 4. Recetas para el cliente

| Cliente | Qué hacer |
|---|---|
| **Java SE / servlet** | `java.net.http.HttpClient` único y reutilizado; JSON UTF-8; tiempo límite de 10 s (R11); lee siempre el cuerpo (R7) |
| **.NET** | `HttpClient` de larga vida para todo el programa; `System.Net.Http.Json`; propiedades con `[JsonPropertyName]` en `camelCase`, lectura sin distinguir mayúsculas e ignorando campos desconocidos (R4); `Uri.EscapeDataString` en los segmentos de ruta (R10); **nunca `EnsureSuccessStatusCode()`** (R7) |
| **Android a mano** | `HttpURLConnection` (o el cliente HTTP de la plataforma) con JSON UTF-8; URL base en un recurso de configuración (R1); HTTP sin cifrar solo con `network_security_config` (`ENTORNO_ANDROID.md`) |

Reglas comunes a todos los clientes:
- Tratar **por separado** el fallo de red (`RED`, R11), la respuesta ilegible (`SRV-000`, R12) y el error de negocio (envoltura, R7).
- Decidir por el código HTTP y luego por el `codigo` de la envoltura; mostrar el `mensaje`, nunca el código.
- Enviar los datos como texto con punto decimal y cultura invariante (R9); un valor opcional o una lista pueden venir ausentes en una respuesta de error. Si por la biblioteca JSON un valor sale como número sin comillas, no es un error del cliente: el servidor lo interpreta como su texto (R9); el cliente no debe tratarlo de forma especial ni depender de ello.
- No llamar al servidor en el hilo de la interfaz; fijar el tiempo límite (R11).
- Un cambio de rutas, campos o códigos obliga a actualizar `api` y `dto` en **todos** los clientes (`ARQUITECTURA_CLIENTE.md` §8); la vista y el controlador no se tocan.

---

## 5. Diferencias con la convención SOAP

| SOAP | REST |
|---|---|
| Errores de negocio con HTTP 200 + `estado = ERROR` | Código HTTP según la categoría del error (R7); `estado` no es la única condición |
| Contrato: WSDL/XSD generado por el servidor; el cliente genera su proxy | Contrato: `ENDPOINTS_REST.md`; los DTO se escriben a mano, sin generación de código |
| TNS único, `SOAPAction`, campos calificados (R2 a R4 de SOAP) | Rutas y verbos (R1, R2, R10), JSON con nombres fijos (R4) |
| Fallo de red: excepción del proxy o del cliente HTTP → `RED` | Sin respuesta o tiempo agotado → `RED` (R11); cuerpo no JSON → `SRV-000` (R12) |
| Un cliente (proxy) por servicio | Un `HttpClient` reutilizado en todo el programa |
| Fault solo para fallos de protocolo | Errores de plataforma con envoltura propia (R6) |

---

## 6. Qué especifica cada proyecto en `ENDPOINTS_REST.md`

La convención deja abierto lo que depende del proyecto. `ENDPOINTS_REST.md` (`ARQUITECTURA_SERVIDOR.md` §8.2) lo fija en dos partes:

1. **Tabla de endpoints:** una fila por operación SOAP equivalente, con verbo, ruta, parámetros, cuerpo, tipo de respuesta, HTTP de éxito y errores posibles con su HTTP. Cambiar una fila es cambiar el contrato.
2. **Autenticación (R8):** cómo viajan las credenciales o el token (cabecera, cuerpo…), en qué operaciones, cómo reacciona el servidor ante credenciales ausentes o inválidas, y qué hace el cliente ante un `401`. Si el mecanismo usa una cabecera, se envía **por petición** y nunca en las cabeceras por defecto del cliente HTTP, y una respuesta `401` trae la cabecera `WWW-Authenticate` que corresponda.

También se anotan ahí las rarezas del proyecto (por ejemplo, un error cuya categoría da un HTTP distinto del recurso de la ruta).

---

## 7. Verificación

La lista paso a paso, con lo que debe verse en cada uno, está en `CONVENCION_PRUEBAS.md` §6.2 (contrato REST: una fila de `ENDPOINTS_REST.md` por paso) y §6.3 (clientes); no hay pruebas automáticas de conformidad. Antes de dar por bueno un servicio nuevo o un cambio de contrato:

1. **Recorrer `ENDPOINTS_REST.md`:** cada fila verifica verbo, ruta, HTTP de éxito, la forma exacta del JSON y al menos un error con su HTTP y su envoltura. En las operaciones con cuerpo, repetir una petición válida con los valores numéricos **sin comillas** (`"importe": 100.50`): debe dar la misma respuesta que con texto (R9).
2. **Errores de plataforma:** ruta inexistente, verbo no permitido, `Content-Type` incorrecto y JSON mal formado devuelven la envoltura (R6), nunca HTML.
3. **Comparación cruzada:** las mismas peticiones (válidas, inválidas y límite) contra Java REST y .NET REST dan los mismos `estado`, `codigo`, `mensaje` y campos propios, ignorando lo que cambia (cabeceras del servidor, fecha).
4. **Un cliente real de cada tipo** contra cada servidor.

---

## 8. Aplicarla a otro servicio o dominio

0. Partir de la plantilla de la plataforma: Java, `rest_java_server`; .NET, aún por crear (mientras tanto, la plantilla SOAP del servidor).
1. Elegir el prefijo y el context root (R1).
2. Escribir `ENDPOINTS_REST.md`: tabla de endpoints y autenticación (§6), **antes** de implementar.
3. Aplicar la receta de la plataforma (§3). La lógica de negocio va en capas sin dependencias de REST.
4. Guardar una copia de `ENDPOINTS_REST.md` en los clientes y verificar (§7).

### 8.1 Lo aprendido al construir Java REST (2026-10-29)

Primera variante construida (servidor Java, Payara 7, `jakartaee-api 11.0.0`), desplegada y verificada a mano por el responsable del proyecto contra `ENDPOINTS_REST.md`. Las reglas R1 a R12 se aplicaron en el servidor sin cambios; esto es lo que conviene saber para la siguiente variante:

| Tema | Hallazgo |
|---|---|
| Dependencias | JAX-RS, JSON-B y JSON-P compilan y funcionan con `jakartaee-api` solo (`provided`); no hace falta ninguna dependencia más en el servidor |
| Orden de propiedades | JSON-B ordena alfabéticamente por defecto. Hay que fijarlo con `@JsonbPropertyOrder`; con herencia (envoltura en una clase base), **cada subclase lista todas las propiedades**, incluidas `estado`, `codigo` y `mensaje`, para que la envoltura salga primero (comprobado con Yasson 3.0.4) |
| POJO frente a *record* | Un *record* no puede heredar la envoltura; se usa un POJO con una clase base |
| Campos sin valor | JSON-B omite los `null` por defecto (R5). Las listas se inicializan vacías para que salgan como `[]` |
| Nombres sin distinguir mayúsculas (R4) | JSON-B no lo hace al deserializar. Se leen los cuerpos de entrada con JSON-P (`JsonObject`) buscando el nombre en minúscula |
| Número JSON como valor de campo (R9) | Regla general desde R9: el servidor lo acepta y lo trata como su texto escrito (`100.50` llega como `"100.50"`); el validador decide. En Java se logra leyendo el cuerpo con JSON-P (§3.1); en .NET habrá que configurarlo (§3.2). Verificado a mano en Java REST con dos decimales |
| Manejador de errores | Un solo `ExceptionMapper<Throwable>` con `@Provider` clasifica ruta inexistente, verbo, `Content-Type`, petición mal formada y cualquier otra excepción; lo demás sale como `ERR-500` sin detalles. Conserva la cabecera `Allow` en el `405` |
| Negociación de contenido | Sin `@Produces` en los recursos (el tipo se fija al construir la respuesta) para no recibir un `406` ajeno a la envoltura |
| `401` | Todo `401` lleva `WWW-Authenticate` (`Basic realm=…`) y la envoltura |
| Recursos con la misma ruta raíz | Se agrupan en una clase por recurso (`@Path("cuentas")` con todas sus operaciones) |
| Pruebas | Las excepciones de JAX-RS no se pueden construir sin la implementación del servidor: se simulan con Mockito; `Response` y los recursos se verifican a mano. Para probar la forma del JSON sin servidor se añaden `yasson` y `parsson` con alcance `test` |
| Categorías de plataforma | `ERR-404` usa *No encontrado*; `ERR-405` y `ERR-415` tienen categoría propia (§2, §2.1) |

**Diferencias de plataforma aceptadas:** ninguna en el mensaje del cable. **Pendiente para .NET REST:** comparar las mismas peticiones contra Java REST (§7, punto 3) y anotar aquí las diferencias de ASP.NET Core.

> **Verificación pendiente:** `OPTIONS` y `HEAD` (los atiende el framework, fuera del contrato) y un cliente real de cada tipo contra el servidor (§7, punto 4); **consola Java hecha** (§8.2), faltan escritorio, web y móvil.

### 8.2 Lo aprendido al construir el cliente de consola Java REST (2026-10-29)

Primer cliente REST construido (consola Java, `04. CLICON/CliCon_Eurekabank_java_REST_GR10`), copiando el núcleo de la consola Java SOAP y escribiendo solo `api`. Verificado a mano por el responsable contra el servidor Java REST; sus pruebas unitarias no usan red.

| Tema | Hallazgo |
|---|---|
| Dependencias | HTTP con `java.net.http.HttpClient` (JDK 21) y JSON con Gson 2.11; los DTO del núcleo se leen sin anotaciones porque sus campos se llaman como el JSON (R4, R9) |
| Separar transporte y traducción | Una interfaz mínima de transporte (verbo, ruta, cuerpo, `Authorization` → código y cuerpo) y una clase estática que arma y lee: todo el armado y la lectura se prueban sin red con un transporte falso; solo el transporte real abre conexiones |
| Envoltura con HTTP de error | Se lee el cuerpo siempre, también con 400, 401, 404, 422 y 500; la decisión la toma `ServicioBase` por el `estado` y el `codigo` |
| `SRV-000` | Cuerpo vacío, que no es JSON, que no es un objeto o sin `estado` (página de un proxy, `502`): el mensaje lleva el código HTTP |
| `RED` | Cualquier `IOException` del transporte (servidor caído, red, tiempo agotado de 10 s, URL mal configurada) |
| Credenciales | `login` en el cuerpo; el resto con `Authorization: Basic` por petición, puesta solo por la implementación; nunca en la ruta ni en cabeceras por defecto |
| Rutas | Cada variable de ruta se codifica con `%20` y no con `+`; la URL base lleva «/» final y las rutas son relativas sin barra inicial |
| Un solo `HttpClient` | Compartido por todo el programa (estático en el transporte real) |
| Paridad con la consola SOAP | Fuera de `ws`/`api`, el núcleo difiere solo en el `import` de las interfaces del cliente y en comentarios que mencionaban SOAP (`ARQUITECTURA_CLIENTE.md` §5) |
| Ejecución manual | Las pruebas manuales que usan `api` necesitan Gson en el *classpath* (`mvn exec:java` o *Run File* en el IDE lo resuelven; con `java -cp` hay que añadirlo) |
