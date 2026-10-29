# Arquitectura del Servidor SOA

Especificación de la arquitectura base del servidor para proyectos académicos SOA implementados en cuatro variantes tecnológicas, tomando **Java + SOAP** como variante de referencia.

**Versión 2.7.** Las pruebas automáticas se limitan a pruebas unitarias sin dependencias externas; las pruebas de conformidad del protocolo se sustituyen por una verificación manual guiada (`CONVENCION_PRUEBAS.md`).

**Versión 2.6.** Incorpora las decisiones validadas en las plantillas del repositorio `arqsof-templates`: convención de errores en el cuerpo de la respuesta y enfoque code-first en ambos servidores. Incorpora además lo aprendido al construir la variante de referencia: entradas como texto validadas y convertidas en el controlador, núcleo de plantilla reutilizable, bloqueo de filas en las modificaciones, categoría de error *No autorizado* y el detalle de las pruebas (JUnit, conformidad y manuales).

**Anexos normativos** (incluidos en las plantillas, independientes del dominio):
- `CONVENCION_SOAP.md`: reglas de interoperabilidad SOAP entre Java, .NET y Android (R1–R10).
- `CONVENCION_REST.md`: reglas de interoperabilidad REST (R1–R12).
- `CONVENCION_PRUEBAS.md`: criterio común de pruebas (qué se prueba, qué no y con cuánto detalle) y verificación manual del contrato.
- `JAVA_TESTS_CONVENTIONS.md`: pruebas manuales en `ec.edu.monster.pruebas` para proyectos Java (servidor y clientes); se aplica al final del proyecto.

Ante cualquier discrepancia sobre cómo se ve un mensaje en el cable, prevalecen los anexos.

---

## 1. Propósito y alcance

Se desarrolla un mismo sistema en cuatro variantes:

| Variante | Lenguaje | Base de datos | Protocolo | Tecnología de publicación |
|---|---|---|---|---|
| **Referencia** | Java 21 | MySQL | SOAP | JAX-WS (Metro) sobre Payara |
| 2 | Java 21 | MySQL | REST | JAX-RS sobre Payara |
| 3 | .NET | SQL Server | SOAP | CoreWCF sobre ASP.NET Core |
| 4 | .NET | SQL Server | REST | ASP.NET Core |

**Supuestos del alcance:**

- Los proyectos son de complejidad baja: operaciones CRUD y cálculos simples.
- Las variantes son equivalentes **a nivel conceptual**. Nunca coexisten: un cliente SOAP solo habla con un servidor SOAP y un cliente REST solo con un servidor REST.
- Dos servidores del mismo protocolo que cumplen su convención son intercambiables para cualquier cliente cambiando solo la URL.
- Cada servidor usa una única base de datos (local o en la nube).
- Java siempre usa MySQL y .NET siempre usa SQL Server. Lenguaje y motor forman un solo eje de cambio.
- La URL y las credenciales de la base de datos se escriben **en texto claro, como constantes dentro del código de `AccesoDB`**. No hay archivos de configuración ni se adjunta nada en los recursos del proyecto (`resources`), por tratarse de entornos locales académicos.
- La autenticación y la autorización (tokens, sesiones, roles) quedan **fuera de la especificación base**; cada proyecto las define si las necesita.

**Objetivo:** que, una vez terminada la variante de referencia, las demás se construyan reutilizando diseño, convenciones y lógica de negocio, reescribiendo únicamente lo que depende del protocolo o de la plataforma.

---

## 2. Principios de diseño

1. **El protocolo es un adaptador, no una capa.** El negocio no sabe que existen SOAP ni REST. Cambiar de protocolo implica reemplazar exactamente un paquete.
2. **Un único punto de manejo de errores.** Las excepciones se lanzan en varias capas, pero se capturan solo en el controlador.
3. **Lo que se garantiza idéntico es el mensaje en el cable, no el documento de contrato.** Cada servidor genera su propio WSDL u OpenAPI desde el código; la convención asegura que el XML o JSON intercambiado sea el mismo. La verificación manual del contrato (`CONVENCION_PRUEBAS.md` §6) lo comprueba.
4. **Éxito y error tienen la misma forma.** Toda respuesta lleva `estado`, `codigo` y `mensaje`; el cliente siempre revisa los mismos campos.
5. **Sin piezas por anticipación.** Solo se crean clases, interfaces o paquetes cuando tienen una responsabilidad clara en el contexto actual.
6. **Simplicidad comprensible.** Se prefiere la solución que un estudiante pueda leer y entender sin conocer patrones avanzados, siempre que no comprometa la separación entre protocolo y negocio.

---

## 3. Estructura de paquetes

Raíz obligatoria: `ec.edu.monster` (en minúsculas también en .NET, como en las plantillas). Cada capa es un paquete **directo bajo la raíz** (`ec.edu.monster.modelo`, `ec.edu.monster.ws`, …); no hay un paquete intermedio de módulo.

**Nombres de paquetes:** se usan exactamente como aparecen en el árbol. `servicios` y `excepciones` van **en plural**, como en las plantillas; el resto (`modelo`, `dto`, `db`, `controlador`, `ws`, `api`, `pruebas`) en singular.

```
ec.edu.monster
│
├── modelo
│   └── Entidades
│
├── dto
│   ├── Solicitudes
│   ├── Respuestas
│   └── Resultado
│
├── excepciones
│   ├── ExcepcionMonster
│   └── Catálogo de códigos
│
├── db
│   ├── AccesoDB
│   ├── Interfaces de repositorio
│   └── Implementaciones de repositorio
│
├── servicios
│   └── Servicios
│
├── controlador
│   ├── Controlador base          (captura central de errores)
│   ├── ValidadorForma            reglas genéricas de forma (obligatorio, longitud, formato, rangos)
│   └── Controladores
│
├── ws                            (solo en variantes SOAP)
│   ├── contrato                  tipos con anotaciones SOAP, envoltura, constantes de la convención
│   ├── Interfaz del endpoint     (cuando la tecnología la exige)
│   ├── Endpoints
│   ├── Mapeadores                Resultado → tipos de contrato
│   └── ConversorTexto            tipos reales → texto del contrato
│
├── api                           (solo en variantes REST)
│   ├── contrato                  tipos con anotaciones JSON
│   ├── Recursos
│   ├── Mapeadores                Resultado ⇄ tipos de contrato
│   └── Manejador de errores de plataforma
│
└── pruebas                       Java: pruebas manuales (src/main) · .NET: xUnit

src/test/java/ec/edu/monster/...   (solo Java) JUnit, misma estructura de paquetes

fuera del proyecto (no se adjunta nada en resources)
└── script de base de datos       creación de tablas y datos iniciales
```

El proyecto **no lleva recursos de configuración**: la URL, el usuario y la contraseña son constantes de `AccesoDB` (§4.4). El script de base de datos tampoco se copia al proyecto; se mantiene en la carpeta de base de datos del repositorio.

**Paquetes descartados y motivo:**

| Paquete | Motivo |
|---|---|
| `config` | La configuración del protocolo vive en `ws`/`api`; la de conexión, como constantes en `AccesoDB`; el ensamblado lo resuelve la plataforma (CDI en Java, punto de arranque en ASP.NET Core). |
| `mapper` independiente | Los mapeadores solo tienen sentido junto al protocolo que traducen y se reemplazan con él. |
| Subtipos de excepción | El catálogo con categoría cumple la misma función con una sola clase de excepción. |
| Traductor de errores a SOAP Fault | Por la convención adoptada, los errores de negocio viajan en el cuerpo con HTTP 200; el mapeador ya construye la envoltura. |
| Contrato congelado en el servidor | Ambos servidores son code-first; la copia del WSDL u OpenAPI se guarda en los **clientes**. |

### 3.1 Convención de nombres de clases

**Regla general: la entidad primero y el tipo de componente después**, porque es el orden natural en español ("cuenta servicio" agrupa todo lo de la cuenta) y deja juntas en el IDE todas las clases de una misma entidad.

Ejemplo para la entidad `Cuenta`:

| Componente | Paquete | Java | .NET |
|---|---|---|---|
| Entidad | `modelo` | `Cuenta` | `Cuenta` |
| DTO de solicitud | `dto` | `DepositoSolicitud` | `DepositoSolicitud` |
| DTO de respuesta | `dto` | `DepositoRespuesta` | `DepositoRespuesta` |
| Interfaz de repositorio | `db` | `CuentaRepositorio` | `ICuentaRepositorio` |
| Implementación de repositorio | `db` | `CuentaRepositorioMySql` | `CuentaRepositorioSqlServer` |
| Servicio | `servicios` | `CuentaServicio` | `CuentaServicio` |
| Controlador | `controlador` | `CuentaControlador` | `CuentaControlador` |
| Endpoint SOAP | `ws` | `CuentaWS` | `CuentaWS` |
| Interfaz del endpoint SOAP | `ws` | `CuentaWSContrato` (si se usa) | `ICuentaWS` |
| Recurso REST | `api` | `CuentaRecurso` | `CuentaRecurso` |
| Mapeador | `ws` / `api` | `CuentaMapeador` | `CuentaMapeador` |
| Prueba automatizada | `src/test` (Java) / `pruebas` (.NET) | `CuentaServicioTest` | `CuentaServicioPruebas` |
| Prueba manual | `pruebas` | `PruebasServicio` (una clase por capa) | No aplica |

**Precisiones:**
- **Interfaces en .NET** llevan el prefijo `I`, por convención de la plataforma. En Java, la interfaz no lleva prefijo.
- **Implementaciones de repositorio** llevan el motor como sufijo. Así el nombre dice qué se reescribe al cambiar de plataforma y evita sufijos genéricos como `Impl`.
- **Clases sin entidad** conservan su nombre fijo: `ControladorBase`, `ValidadorForma`, `AccesoDB`, `ExcepcionMonster`, `CodigoError` (catálogo), `Resultado`, `ConversorTexto`, `RespuestaBase`, `ConvencionSoap` y, en las pruebas manuales, `Verificador`.
- **Núcleo de plantilla:** las clases anteriores no dependen del dominio. Se copian **sin cambios** de la plantilla a cada proyecto, en los paquetes indicados, sin crear paquetes nuevos ni una biblioteca compartida (solo difieren las constantes de conexión de `AccesoDB` y las entradas de dominio del catálogo). Lo propio de cada proyecto son las entidades, repositorios, servicios, controladores, endpoints, mapeadores y tipos de contrato.
- **Pruebas:** sufijo `Test` en Java y `Pruebas` en .NET, como en las plantillas.
- **Excepción de la regla: los tipos de contrato.** Sus nombres son parte del contrato y siguen la convención del protocolo (`RespuestaBase`, `Respuesta<Operación>`, R5 de `CONVENCION_SOAP.md`). No se renombran para cumplir esta regla.
- **Endpoint SOAP:** el nombre del servicio publicado (`serviceName`) es parte del contrato y puede diferir del nombre de la clase si un cliente existente lo exige.


---

## 4. Componentes y responsabilidades

### 4.1 `modelo`

| Componente | Responsabilidad | No hace |
|---|---|---|
| **Entidades** | Representar los datos del negocio con sus tipos reales (decimales exactos, fechas); una clase por tabla de negocio. | Conocer el protocolo ni contener anotaciones de SOAP/REST. |

### 4.2 `dto`

| Componente | Responsabilidad | No hace |
|---|---|---|
| **Solicitudes** | Transportar entradas que no coinciden con una entidad (cálculos, búsquedas con filtros). | Contener lógica ni anotaciones de protocolo. |
| **Respuestas** | Transportar salidas que no coinciden con una entidad (resultados combinados, valores calculados). | Contener lógica ni anotaciones de protocolo. |
| **Resultado** | Estructura neutral que devuelve todo controlador: `estado` (OK/ERROR), `codigo`, `mensaje` y, si hubo éxito, los datos. | Conocer cómo se serializa. |

**Regla de uso:** cuando la entrada o salida coincide con una entidad, se usa la entidad directamente. Los DTOs se crean solo cuando no hay coincidencia.

**Resultado:** en Java es genérico (`Resultado<T>`, con `T` el tipo de los datos de éxito). Las constantes de éxito (estado `OK`, código `200`, mensaje de éxito) viven en el propio `Resultado`, de modo que el controlador base y las pruebas las comparten.

**Relación con la convención:** el Resultado tiene la misma forma que la envoltura `RespuestaBase` de ambas convenciones (R6 en SOAP, R5 en REST). Por eso el mapeador del adaptador es casi directo: copia los tres campos y convierte los datos a texto.

### 4.3 `excepciones`

| Componente | Responsabilidad |
|---|---|
| **ExcepcionMonster** | Única excepción de negocio. Transporta una entrada del catálogo y, opcionalmente, un mensaje específico que reemplaza al mensaje por defecto. |
| **Catálogo de códigos** | Enumeración de errores. Cada entrada define **código textual**, **mensaje por defecto** y **categoría**. |

**Formato del código:** `PREFIJO-NNN`, en mayúsculas, sin tildes y con tres dígitos (`DATA-001`, `CTA-002`). Los códigos y los textos son idénticos en las cuatro variantes.

**Prefijos reservados:**

| Prefijo | Uso |
|---|---|
| `ERR-` | Plataforma y técnicos: `ERR-500`, `ERR-404`, `ERR-405`, `ERR-415` |
| `DATA-` | Validación genérica de datos de entrada (`DATA-001`: dato inválido o JSON mal formado) |

**Prefijos de dominio:** dos a cuatro letras por área o entidad (`CTA-` cuenta, `CAT-` catálogo), definidos por proyecto.

**Reglas del catálogo:**
- Numeración secuencial por prefijo; un código **nunca se reutiliza** con otro significado.
- El éxito usa el código `200`.
- La categoría está en la entrada del catálogo, no en el prefijo: un mismo prefijo puede tener errores de distintas categorías.
- Los códigos exclusivos del cliente (`RED`, `SRV-000`) nunca aparecen en el catálogo del servidor.

**Categorías:**

| Categoría | Significado | HTTP en REST |
|---|---|---|
| Validación | Datos de entrada incorrectos | 400 |
| No encontrado | Se opera sobre algo que no existe | 404 |
| Conflicto | Duplicado o violación de una restricción | 409 |
| No autorizado | Credenciales o permisos insuficientes | 401 |
| Operación inválida | Una regla de negocio impide la acción | 422 |
| Método no permitido | Verbo HTTP no admitido en la ruta (`ERR-405`) | 405 |
| Tipo no soportado | `Content-Type` no admitido (`ERR-415`) | 415 |
| Técnico | Error inesperado | 500 |

`ERR-404` (ruta inexistente) usa *No encontrado*. Las dos categorías de plataforma solo se usan en REST; en SOAP existen en el catálogo pero no salen en el cable. Detalle y mensajes en `CONVENCION_REST.md` §2.1.

**Entrada obligatoria:** `ERR-500` ("Error interno del servidor"), de categoría Técnico. Es la que usa el controlador para reemplazar cualquier excepción no controlada sin filtrar detalles internos.

**Ventaja de la categoría:** en REST, el código HTTP se decide por la categoría y no por cada código individual. Agregar un error nuevo solo implica agregar una entrada al catálogo. En SOAP la categoría no se usa en el cable (siempre HTTP 200). La categoría *No autorizado* se declara aunque la autenticación quede fuera de la especificación base: cualquier proyecto que la defina la necesita, y así el código HTTP de REST sigue saliendo de la categoría.

### 4.4 `db`

| Componente | Responsabilidad | Reglas |
|---|---|---|
| **AccesoDB** | Entregar conexiones abiertas. La URL, el usuario y la contraseña son **constantes de la clase, en texto claro**; no lee archivos de configuración. | Clase concreta, recibida por el servicio (nunca estática). Su método de conexión debe ser reemplazable en pruebas (`virtual` en .NET). Entrega la conexión por su **tipo abstracto**. Una conexión por operación o transacción; nunca una conexión compartida. Sin pool. |
| **Interfaces de repositorio** | Definir las operaciones de persistencia de cada entidad. | Una por entidad. Existen porque las pruebas unitarias requieren sustituirlas. |
| **Implementaciones de repositorio** | Ejecutar SQL escrito a mano, mapear filas ⇄ entidades y traducir errores del motor. | Reciben siempre la conexión; nunca la abren ni la cierran. SQL como constantes de la clase. |

**Por qué AccesoDB no tiene interfaz y los repositorios sí:**
- Un solo método reemplazable → método sustituible, sin interfaz.
- Varias operaciones que conforman un contrato → interfaz.

**Prevención de inyección SQL:**
- Los valores siempre viajan como parámetros (sentencias preparadas en JDBC, comandos con parámetros en ADO.NET). Nunca se concatenan.
- Las partes estructurales (nombres de columna, dirección de ordenamiento) no admiten parámetros; se validan contra una **lista blanca**.
- El usuario de base de datos de la aplicación no es el administrador; solo tiene permisos sobre las tablas del proyecto.

**Mapeo de filas:**
- Método privado de mapeo dentro de cada implementación de repositorio.
- Los valores nulos se manejan explícitamente. Una búsqueda que puede no encontrar nada devuelve un valor opcional (`Optional` en Java, tipo anulable en .NET), nunca un nulo sin declarar.
- Los montos usan tipos decimales exactos (`BigDecimal` en Java, `decimal` en .NET, `DECIMAL` en la base). Nunca punto flotante.

**Concurrencia (lectura para modificar):**
- Cuando una operación lee una fila para luego modificarla, el repositorio ofrece una variante de lectura que **bloquea la fila** hasta el fin de la transacción (`SELECT … FOR UPDATE` en MySQL, `WITH (UPDLOCK, ROWLOCK)` en SQL Server). Sin ese bloqueo, dos operaciones simultáneas leen el mismo valor y una pisa a la otra.
- Si la operación toca varias filas, se bloquean **siempre en el mismo orden** (por ejemplo, ascendente por clave), sin importar el orden en que las pide el usuario, para que dos operaciones cruzadas no se bloqueen entre sí.
- El bloqueo solo tiene efecto dentro de una transacción; por eso estas operaciones usan siempre el patrón de la sección 4.5.

**Traducción de errores del motor:**

| Situación | MySQL | SQL Server | Categoría |
|---|---|---|---|
| Clave duplicada | 1062 | 2627 / 2601 | Conflicto |
| Violación de clave foránea | 1451 / 1452 | 547 | Conflicto u Operación inválida |
| Otros (conexión, sintaxis) | — | — | Técnico (sin traducir; termina en `ERR-500`) |

**Cierre de recursos:** toda conexión, comando y lector se cierra siempre, incluso ante excepciones (try-with-resources en Java, `using` en .NET).

**Esquema de base de datos por plataforma:**

| Plataforma | Motor | Esquema | Lo usan |
|---|---|---|---|
| Java | MySQL | Uno | Java SOAP y Java REST |
| .NET | SQL Server | Uno | .NET SOAP y .NET REST |

- Las variantes de una misma plataforma comparten esquema y script, porque solo difieren en el protocolo. Como nunca se ejecutan al mismo tiempo, no hay conflictos.
- Las plataformas nunca comparten base de datos.
- Ambos esquemas usan los **mismos nombres lógicos** de tablas y columnas, para que la traducción de los repositorios entre plataformas se limite al dialecto.
- El script de creación **no se incluye en el proyecto** (no va en `resources`); se mantiene en la carpeta de base de datos del repositorio. Dentro de una plataforma, SOAP y REST usan el mismo archivo.

### 4.5 `servicios`

| Componente | Responsabilidad | Reglas |
|---|---|---|
| **Servicios** | Aplicar reglas de negocio, ejecutar cálculos, gestionar la conexión y la transacción. | Clases concretas sin interfaz. Reciben AccesoDB y repositorios por constructor. Los cálculos se escriben como métodos independientes que reciben y devuelven valores, sin acceso a datos. No conocen SOAP, REST ni los tipos de contrato. |

**Gestión de conexión y transacción (patrón único):**
1. Pedir una conexión a AccesoDB.
2. Si la operación modifica más de una tabla, iniciar la transacción.
3. Invocar a los repositorios pasándoles la conexión.
4. Confirmar si todo terminó bien; revertir si hubo una excepción y dejar que esta continúe hacia el controlador.
5. Cerrar la conexión siempre.

La transacción se gestiona manualmente en ambas plataformas, ya que no hay pool ni conexiones administradas por el contenedor.

**Cómo se aplica:**
- El patrón se escribe **una sola vez por servicio**, en un método auxiliar privado que recibe la operación como función (por ejemplo `enTransaccion`). Así ninguna operación repite la confirmación, la reversión ni el cierre. Si la reversión misma falla, se registra y se deja continuar la excepción original.
- Los cálculos son métodos **estáticos y puros** (reciben y devuelven valores). Pueden ser estáticos porque no son reemplazables; se prueban sin dobles.
- **Dependencia entre servicios:** si una regla debe ejecutarse dentro de la misma conexión y transacción de otra operación (por ejemplo, verificar la identidad de quien opera antes de acceder a los datos), el servicio que la ofrece expone además una variante que **recibe la conexión**, y el otro servicio lo recibe por constructor. Es la única dependencia servicio → servicio aceptada.
- **Orden de verificación** en una operación: forma de los datos (controlador) → identidad o permisos, si el proyecto los define → existencia de lo que se opera → reglas de negocio.
- **Inyección (Java):** servicios y controladores reciben sus dependencias por un constructor anotado para CDI; `AccesoDB` y las implementaciones de repositorio son beans de alcance de aplicación. En las pruebas se construyen con `new`, sin contenedor.

### 4.6 `controlador`

| Componente | Responsabilidad | No hace |
|---|---|---|
| **Controlador base** | Ejecutar una operación y capturar toda excepción: ExcepcionMonster → Resultado de error con su código y mensaje; cualquier otra → registrar en el log y devolver `ERR-500`. | Propagar excepciones hacia el adaptador. |
| **Controladores** | Validar la forma de la entrada, convertirla a sus tipos reales, invocar al servicio y producir el Resultado mediante el controlador base. | Conocer SOAP, REST, conexiones ni SQL. |
| **ValidadorForma** | Reglas genéricas de forma: obligatorio, longitud máxima o exacta, solo dígitos, decimal positivo con límite de enteros y decimales. Todo texto se recorta antes de validar y un valor en blanco equivale a no enviado. Un incumplimiento lanza el error de validación genérico con un **mensaje específico del campo**. | Conocer un dominio concreto. |

**Reparto de validación:** el controlador valida **forma** (obligatoriedad, formato); el servicio valida **reglas** (que el valor tenga sentido para el negocio).

**Entradas como texto:** cuando la entrada son valores simples, llegan al controlador **como texto**, tal como los recibe el endpoint. El controlador los valida y los convierte a sus tipos reales (decimales exactos, enteros) justo antes de invocar al servicio; el servicio nunca recibe texto sin validar. Los formatos propios de cada dominio (longitudes, límites de cantidades) se declaran como constantes en la entidad correspondiente (`modelo`) y el controlador los combina con las reglas genéricas de `ValidadorForma`.

Es el **único** punto del servidor que captura excepciones. Corresponde al `ControladorBase` de la plantilla `soap_java_server`, con la diferencia de que devuelve el Resultado neutral en lugar del tipo de contrato.

### 4.7 `ws` (variantes SOAP)

| Componente | Responsabilidad |
|---|---|
| **contrato** | Tipos de respuesta que extienden `RespuestaBase` (R6), con las anotaciones de la convención: nombres fijos (R5), campos calificados (R3), todo como texto (R7), listas inicializadas vacías. Incluye el espacio de nombres (TNS) declarado en un único lugar (R2): en Java, una clase de constantes (`ConvencionSoap`) que también guarda los nombres de servicio y los prefijos de `SOAPAction`, y que referencian `package-info.java` y los endpoints. |
| **Interfaz del endpoint** | Exigida por la tecnología (contrato de servicio en CoreWCF; opcional en JAX-WS). No contradice la regla de evitar interfaces. |
| **Endpoints** | Recibir la petición, delegar al controlador y devolver la respuesta. Declaran en cada operación la `SOAPAction` con URI (R4) y el retorno único llamado `respuesta` (R5), y declaran explícitamente el nombre, el servicio y el puerto, para que el contrato no dependa del nombre de la clase. Sin lógica. En Java reciben el controlador por inyección de campo y ofrecen además un constructor público que lo recibe, para las pruebas sin contenedor (JAX-WS exige el constructor sin argumentos). |
| **Mapeadores** | Convertir Resultado → tipo de contrato, pasando los tipos reales a texto (punto decimal, ISO-8601, cultura invariante) mediante `ConversorTexto`. Las entradas simples no se convierten aquí: pasan como texto al controlador (§4.6). Solo cuando la entrada es un objeto compuesto (entidad o DTO) el mapeador la arma. |

**Reglas de los tipos de contrato (aprendidas en Java):**
- Un campo propio de un tipo de respuesta **no puede llamarse `estado`, `codigo` ni `mensaje`**, porque choca con la envoltura heredada; se prefija con la entidad (por ejemplo, el código de una entidad se publica como `codigo<Entidad>`).
- Los tipos de fecha y hora del lenguaje no llegan nunca al contrato: JAXB no serializa `java.time` y deja el elemento vacío. Por eso el contrato guarda todo como texto y la conversión la hace el mapeador (R7).
- Los campos sin valor se omiten del mensaje; las listas se inicializan vacías y se publican dentro de un contenedor.

**Errores en SOAP:** los errores de negocio y `ERR-500` viajan en el cuerpo con `estado = ERROR` y **HTTP 200** (R8). El SOAP Fault queda reservado para fallos de protocolo (operación inexistente, XML mal formado), que produce la plataforma.

### 4.8 `api` (variantes REST)

| Componente | Responsabilidad |
|---|---|
| **contrato** | Tipos JSON con la misma envoltura (`estado`, `codigo`, `mensaje` + campos propios), nombres en camelCase y todo como texto (R4, R5, R9). |
| **Recursos** | Recibir la petición, delegar al controlador y devolver la respuesta. Incluyen rutas, verbos, CORS y serialización. Sin lógica. |
| **Mapeadores** | Convertir cuerpo y parámetros → entidades o DTOs, y Resultado → tipo de contrato. Determinan el código HTTP a partir de la **categoría** del error. |
| **Manejador de errores de plataforma** | Garantizar que los errores que produce el framework (ruta inexistente, verbo no permitido, `Content-Type` incorrecto, JSON mal formado) salgan con la misma envoltura y sus códigos `ERR-404`, `ERR-405`, `ERR-415`, `DATA-001` (R6). Nunca páginas HTML ni `ProblemDetails`. |

**Errores en REST:** código HTTP según la categoría **y** envoltura en el cuerpo (R7). El cliente decide primero por el HTTP y luego por el `codigo`.

### 4.9 `pruebas`

Ver sección 10.

---

## 5. Reglas de dependencia

```
ws | api  →  controlador  →  servicios  →  db
                  ↘              ↘         ↙
             modelo · dto · excepciones   (transversales)
```

- Cada capa solo conoce a la inmediatamente inferior.
- `modelo`, `dto` y `excepciones` pueden ser usados por cualquier capa y no dependen de ninguna.
- Ninguna capa inferior conoce a una superior.
- Nada fuera de `ws`/`api` conoce los tipos de `contrato` ni las anotaciones de protocolo.

**Criterio de verificación:** al pasar de SOAP a REST en el mismo lenguaje, todo lo que está a la derecha de `ws | api` se copia sin modificaciones. Si algo requiere cambios, una regla se rompió.

---

## 6. Flujo de una solicitud

Ejemplo de referencia: registrar un depósito en una cuenta (verifica existencia, calcula, actualiza dos tablas).

| Paso | Capa | Piezas | Acción |
|---|---|---|---|
| 1 | `ws`/`api` | Endpoint/Recurso | Recibe la petición en formato del protocolo (parámetros como texto). |
| 2 | `ws`/`api` | Endpoint/Recurso, Mapeador | Pasa los parámetros simples tal como llegaron (texto). Si la entrada es un objeto compuesto, el mapeador lo arma en un DTO o entidad. |
| 3 | `controlador` | Controlador, ValidadorForma, ExcepcionMonster | Valida la forma y convierte el texto a sus tipos reales. Si falla, lanza error de categoría Validación. |
| 4 | `servicios` | Servicio, AccesoDB, entidades | Obtiene la conexión, inicia la transacción, verifica reglas (No encontrado, Operación inválida) y ejecuta el cálculo. |
| 5 | `db` | Repositorios | Persiste con SQL parametrizado; traduce errores del motor. El servicio confirma o revierte y cierra la conexión. |
| 6 | `controlador` | Controlador base, Resultado | Produce Resultado de éxito, o captura la excepción y produce Resultado de error (`ERR-500` si fue inesperada). |
| 7 | `ws`/`api` | Mapeador | Construye la respuesta con la envoltura. SOAP: siempre HTTP 200. REST: HTTP según la categoría. |

---

## 7. Entradas y salidas

**Entradas al controlador:**
- La entidad, cuando coincide con la operación.
- Un DTO de solicitud, cuando no coincide (cálculos, búsquedas con filtros).
- Valores simples **como texto**, que el controlador valida y convierte (consultas por identificador, operaciones con pocos parámetros).

**Salida del controlador:** siempre el Resultado neutral.

**Representación en el protocolo:**

| Situación | SOAP | REST |
|---|---|---|
| Éxito | HTTP 200, `estado=OK`, `codigo=200`, mensaje fijo de éxito + datos | 200, misma envoltura + datos |
| Error de negocio | HTTP 200, `estado=ERROR`, código y mensaje del catálogo; campos propios omitidos, listas vacías | HTTP según categoría, misma envoltura |
| Error inesperado | HTTP 200, `ERR-500`, mensaje genérico | 500, `ERR-500`, mensaje genérico |
| Error de protocolo / plataforma | SOAP Fault (lo produce la plataforma) | 404 / 405 / 415 / 400 con envoltura (`ERR-404`, `ERR-405`, `ERR-415`, `DATA-001`) |

**Texto de éxito:** el estado, el código `200` y el mensaje de éxito son constantes idénticas en las cuatro variantes.

**Tipos en el cable:** todo dato viaja como texto (R7 SOAP, R9 REST). Números con punto decimal y sin notación científica, fechas en ISO-8601. Los tipos reales (decimales exactos, fechas) existen solo dentro del servidor: las entradas se convierten en el controlador, tras validar su forma, y las salidas en el mapeador, con cultura invariante.

---

## 8. Contratos

| Variante | Enfoque | Cómo se produce el contrato |
|---|---|---|
| Java SOAP | Code-first | Clases anotadas según `CONVENCION_SOAP.md` §3.1; Metro genera el WSDL al desplegar |
| .NET SOAP | Code-first | Contrato de servicio y `DataContract` según `CONVENCION_SOAP.md` §3.2; CoreWCF genera el WSDL |
| Java REST | Code-first | Recursos y tipos según `CONVENCION_REST.md` |
| .NET REST | Code-first | Controladores y tipos según `CONVENCION_REST.md` |

**Por qué no contract-first en el servidor .NET:** se probó copiar el WSDL de Java en el servidor .NET y se descartó, porque obligaba a mantener un contrato duplicado y código de transporte de bajo nivel. Lo que importa es que el mensaje sea el mismo, y eso lo garantiza la convención junto con la verificación manual del contrato.

**Reglas:**
1. Cada servidor genera su propio contrato desde el código, aplicando las declaraciones de su convención.
2. Los WSDL de Java y .NET pueden diferir en detalles aceptados (nombre del puerto, documentos importados, `nillable`); ver `CONVENCION_SOAP.md` §5.
3. Los **clientes** guardan una copia del WSDL en su repositorio y generan su proxy desde ella (wsimport en Java, dotnet-svcutil en .NET), nunca desde la URL en ejecución.
4. El código generado **nunca se edita a mano**.
5. Cambiar operaciones, parámetros o tipos obliga a redesplegar el servidor y actualizar **todos** los clientes.
6. Los nombres de servicio, operaciones, parámetros y tipos son parte del contrato y no se renombran sin regenerar los clientes.

### 8.1 Diseño REST orientado a recursos

Las rutas nombran **recursos** (sustantivos) y el verbo HTTP indica la acción.

**Reglas de rutas:**
1. Recursos en plural, minúscula y sin tildes: `/cuentas`, `/movimientos`.
2. El identificador va en la ruta: `/cuentas/{numero}`.
3. Las operaciones de negocio se modelan como **subrecursos que se crean**: un depósito es `POST /cuentas/{numero}/depositos`, no `POST /depositar`.
4. Los cálculos sin persistencia son consultas: `GET` con parámetros de consulta (`/conversiones/longitud?valor=10&origen=m&destino=km`). Si los datos de entrada son demasiados para la URL, `POST` a un recurso de cálculo (`/calculos/promedio`).
5. Filtros, orden y paginación como parámetros de consulta, nunca como segmentos de ruta.
6. Prefijo común configurable (por ejemplo `/api/`), según R1 de `CONVENCION_REST.md`.

**Correspondencia CRUD:**

| Operación | Verbo y ruta | HTTP de éxito |
|---|---|---|
| Listar | `GET /cuentas` | 200 |
| Consultar uno | `GET /cuentas/{numero}` | 200 |
| Crear | `POST /cuentas` | 200 |
| Actualizar | `PUT /cuentas/{numero}` | 200 |
| Eliminar | `DELETE /cuentas/{numero}` | 200 |
| Operación de negocio | `POST /cuentas/{numero}/depositos` | 200 |

**Todo éxito responde 200** (decisión del 2026-10-27): ni 201 al crear ni 204 al eliminar, porque toda respuesta lleva la envoltura `estado`/`codigo`/`mensaje` y el cliente decide por el HTTP y luego por el `codigo`.

### 8.2 Tabla de endpoints

Cada proyecto REST mantiene una **tabla de endpoints** como documento compartido (`ENDPOINTS_REST.md` en la raíz de cada proyecto REST, servidor y clientes). Es la fuente que Java REST y .NET REST implementan y que los clientes consumen. Se escribe **antes** de implementar la primera variante REST, partiendo del inventario de operaciones SOAP.

**Columnas obligatorias:**

| Columna | Contenido |
|---|---|
| Operación SOAP equivalente | Nombre de la operación en el WSDL; enlaza ambos contratos |
| Verbo | GET, POST, PUT, DELETE |
| Ruta | Relativa al prefijo, con variables entre llaves |
| Parámetros de ruta y consulta | Nombre y descripción; todos como texto |
| Cuerpo de la petición | Campos JSON (camelCase) o "—" |
| Tipo de respuesta | Nombre del tipo y sus campos propios además de la envoltura |
| HTTP de éxito | 200 |
| Errores posibles | Códigos del catálogo y su HTTP |

**Ejemplo:**

| Operación SOAP | Verbo | Ruta | Parámetros | Cuerpo | Respuesta | Éxito | Errores |
|---|---|---|---|---|---|---|---|
| `consultarCuenta` | GET | `cuentas/{numero}` | `numero` | — | `RespuestaCuenta` (`numero`, `saldo`, `estado`) | 200 | `CTA-001` 404 |
| `registrarDeposito` | POST | `cuentas/{numero}/depositos` | `numero` | `monto` | `RespuestaMovimiento` (`numeroMovimiento`, `saldoAnterior`, `saldoNuevo`) | 200 | `DATA-001` 400, `CTA-001` 404, `CTA-002` 422 |

**Reglas de la tabla:**
- Toda operación SOAP tiene una fila; si alguna no tiene equivalente REST, se indica explícitamente.
- Cambiar una fila es cambiar el contrato: obliga a actualizar ambos servidores REST y todos los clientes REST.
- La verificación manual del contrato REST recorre la tabla: cada fila verifica verbo, ruta, HTTP de éxito y al menos un error (`CONVENCION_PRUEBAS.md` §6.2).

---

## 9. Persistencia: resumen de reglas

1. Una conexión por operación o transacción; nunca compartida.
2. Los repositorios siempre reciben la conexión; nunca la abren ni la cierran.
3. El servicio siempre cierra la conexión, haya éxito o fallo.
4. Toda operación que modifique más de una tabla se ejecuta dentro de una transacción.
5. Ningún valor se concatena en SQL; las partes estructurales usan lista blanca.
6. Los montos usan tipos decimales exactos en el lenguaje y en la base.
7. Los nulos se manejan explícitamente en el mapeo.
8. Solo se traducen a ExcepcionMonster los errores del motor que el usuario puede entender.
9. La URL y las credenciales de la base de datos son constantes en texto claro dentro de `AccesoDB`; no se usan archivos de configuración ni se adjunta nada en `resources`. El script de base de datos queda fuera del proyecto.
10. Toda lectura que precede a una modificación de la misma fila la bloquea dentro de la transacción; con varias filas, siempre en el mismo orden.

---

## 10. Pruebas

**Tipo:** exclusivamente pruebas unitarias sobre operaciones atómicas y de negocio, ejecutables **con la base de datos y el servidor apagados**. No hay pruebas de integración, de extremo a extremo ni de protocolo. El criterio completo está en `CONVENCION_PRUEBAS.md`.

| Plataforma | Pruebas automatizadas | Pruebas manuales |
|---|---|---|
| Java | JUnit 5 + Mockito en `src/test/java`, con la misma estructura de paquetes que el código | `ec.edu.monster.pruebas` en `src/main/java`: clases con `main` que replican las JUnit (anexo `JAVA_TESTS_CONVENTIONS.md`) |
| .NET | xUnit + Moq en `ec.edu.monster.pruebas`, dentro del mismo proyecto | No aplica |

**Alcance de las pruebas automatizadas:**

| Componente | Tipo de prueba | Necesita dobles |
|---|---|---|
| Cálculos | Unitaria directa | No |
| Mapeadores (incluida la conversión a texto) | Unitaria directa | No |
| Catálogo | Unitaria directa | No |
| Controlador base: `ERR-500` sin filtrar detalles | Unitaria directa | No |
| Validación de forma del controlador (`ValidadorForma`) | Unitaria directa | No |
| Controladores: conversión, delegación y que un dato inválido no llegue al servicio | Unitaria con doble del servicio | Sí: servicio |
| Orquestación del servicio: qué se pide a los repositorios, en qué orden, confirmación, reversión y cierre de la conexión | Unitaria con dobles | Sí: AccesoDB y repositorios |
| Repositorios (SQL real) | Manual, con base encendida | — |
| Contrato y mensajes en el cable (conformidad) | Manual guiada (`CONVENCION_PRUEBAS.md` §6.1 y §6.2) | — |
| Endpoints contra el servidor desplegado | Manual (SoapUI, Postman o cliente) | — |

**Contrato y protocolo:** no se prueban con código. Se verifican a mano, con el servidor encendido, siguiendo la lista de `CONVENCION_PRUEBAS.md` §6 (WSDL/OpenAPI, formato de las respuestas, errores en el cuerpo con HTTP 200 en SOAP, Fault solo para fallos de plataforma, `ERR-500` sin detalles). Las pruebas automáticas de conformidad que existan en proyectos anteriores (`ConvencionSoapTest`) quedan fuera de alcance y se retiran cuando se decida (`CONVENCION_PRUEBAS.md` §8).

**Comprobar que las pruebas detectan defectos:** antes de darlas por buenas, introducir a propósito un defecto en una regla crítica (por ejemplo, invertir un orden o alterar un cálculo) y comprobar que alguna prueba falla; después restaurar el código.

**Lo que las pruebas unitarias no cubren:** errores de sintaxis SQL, nombres de columna incorrectos y problemas de conexión. Se cubren con una **lista mínima de verificación manual** por entidad (crear, leer, actualizar, eliminar y cada cálculo).

### 10.1 Pruebas manuales en Java

Se rigen por el anexo `JAVA_TESTS_CONVENTIONS.md`, que aplica a servidores y clientes Java. Puntos que afectan a la arquitectura:

- **Momento:** se construyen **al final**, cuando el proyecto está básicamente completo y sus JUnit existen. No forman parte del desarrollo de cada capa.
- **Origen:** cada prueba manual replica **un** método JUnit (relación 1:1). Cantidad: `min(15, métodos JUnit significativos)`; no se inventan pruebas. Las JUnit no se modifican.
- **Ubicación:** `ec.edu.monster.pruebas` en `src/main/java`; viajan con el WAR o JAR, lo cual es aceptado por el requisito del curso.
- **Nombres:** una clase por capa con prefijo `Pruebas` (`PruebasServicio`, `PruebasControlador`, `PruebasMapeador`, `PruebasExcepciones`); los métodos llevan el nombre de su método JUnit y su Javadoc indica de cuál es réplica. Cada clase tiene su propio `main` y expone `ejecutar(int primera)` para poder numerar de corrido; no hay clase agregadora.
- **Aserciones:** JUnit y Mockito tienen alcance `test` y no se ven desde `src/main`. Cada proyecto incluye su `Verificador`: lleva el conteo (`ejecutar`, `resumen`, `total`) y ofrece las comprobaciones como métodos estáticos para usarlas con import estático (`igual`, `verdadero`, `falso` y `lanza`). Como la excepción de negocio lleva un código del catálogo, `lanza` recibe el **código esperado** y devuelve la excepción para comprobar, por ejemplo, su mensaje.
- **Ejecución:** *Run File* en el IDE. No se configura la ejecución por Maven (`exec-maven-plugin`). Sin dependencias nuevas. Cada `main` termina con código 0 si todo pasó y con 1 si alguna prueba falló.

**Criterio de selección para este proyecto:** el módulo existe por una disposición académica y se mantiene mínimo. Solo se replican métodos JUnit que **no requieren dependencias además de JUnit**: sin Mockito, sin dobles de AccesoDB ni de conexión, sin red. En la práctica, las candidatas son:

| Capa | Ejemplos de pruebas replicables |
|---|---|
| Servicios | Cálculos (métodos independientes, sin acceso a datos) |
| Controlador | Validación de forma; controlador base produce `ERR-500` sin filtrar detalles |
| Mapeadores | Conversión entre tipos reales y texto; armado de la envoltura |
| Excepciones | Cada entrada del catálogo tiene código, mensaje y categoría |

La cantidad sigue la regla del anexo, `min(15, JUnit significativos)`, contando solo los que cumplen este criterio. **No se replican:** la orquestación del servicio (usa Mockito) Por eso el diseño del servidor no necesita adaptaciones para las pruebas manuales; la separación de los cálculos en métodos independientes ya garantiza que existan pruebas replicables.

**Asimetría con .NET:** en .NET el paquete `ec.edu.monster.pruebas` contiene las pruebas **automatizadas** (xUnit); en Java contiene las **manuales**. El nombre del paquete coincide, pero su naturaleza no. La asimetría es intencional y responde al requisito del curso.

### 10.2 Consideraciones para .NET (pruebas en el mismo proyecto)

- Desactivar la generación automática del punto de entrada del paquete de pruebas, para evitar el conflicto con el punto de entrada de ASP.NET Core.
- Las pruebas se compilan y despliegan con la aplicación; en despliegues en la nube puede excluirse el paquete `pruebas`.

---

## 11. Matriz de reutilización

Partiendo de **Java SOAP**, existen dos ejes de cambio independientes:
- **Plataforma:** Java + MySQL ↔ .NET + SQL Server
- **Protocolo:** SOAP ↔ REST

**Niveles de reutilización:**
- **Reutilizar:** se usa sin modificaciones.
- **Traducir:** misma estructura y lógica; solo cambia la sintaxis del lenguaje.
- **Reescribir:** misma estructura; el contenido depende de la tecnología.

| Componente | Cambio de plataforma | Cambio de protocolo |
|---|---|---|
| Entidades | Traducir | Reutilizar |
| DTOs y Resultado | Traducir | Reutilizar |
| ExcepcionMonster | Traducir | Reutilizar |
| Catálogo de códigos | Traducir (**códigos y textos idénticos**) | Reutilizar |
| AccesoDB (incluye las constantes de conexión) | Reescribir (JDBC ↔ ADO.NET y cadena de conexión de cada motor) | Reutilizar |
| Interfaces de repositorio | Traducir | Reutilizar |
| Implementaciones de repositorio | Reescribir SQL (dialecto) y códigos de error del motor | Reutilizar |
| Servicios | Traducir (API de conexión y transacción propia de cada plataforma) | Reutilizar |
| Controlador base y controladores | Traducir | Reutilizar |
| Tipos de contrato | Reescribir con las declaraciones de la convención de la plataforma | Reescribir (anotaciones SOAP ↔ JSON) |
| Interfaz del endpoint (SOAP) | Reescribir (obligatoria en CoreWCF) | No aplica en REST |
| Endpoints / Recursos | Reescribir (JAX-WS/JAX-RS ↔ CoreWCF/ASP.NET Core) | Reescribir |
| Mapeadores | Traducir | Reescribir (en REST se agrega la decisión del código HTTP) |
| Manejador de errores de plataforma | Reescribir | Solo existe en REST |
| **Convención (anexo)** | **Reutilizar** | Reemplazar (SOAP ↔ REST) |
| Script de base de datos (externo al proyecto) | Reescribir (dialecto) | Reutilizar |
| Pruebas de núcleo | Traducir (JUnit/Mockito ↔ xUnit/Moq) | Reutilizar |
| Verificación manual del contrato | Reutilizar la lista (§6 de `CONVENCION_PRUEBAS.md`) | Reescribir la lista REST |

**Diferencias de dialecto SQL a considerar:**

| Aspecto | MySQL | SQL Server |
|---|---|---|
| Autoincremento | `AUTO_INCREMENT` | `IDENTITY` |
| Limitar resultados | `LIMIT` | `TOP` u `OFFSET ... FETCH` |
| Clave generada | Mecanismo de claves generadas del driver | `OUTPUT` o `SCOPE_IDENTITY()` |
| Delimitar identificadores | Comilla invertida | Corchetes |
| Leer una fila bloqueándola para modificarla | `SELECT … FOR UPDATE` | `WITH (UPDLOCK, ROWLOCK)` |

**Diferencias de convención SOAP entre plataformas** (detalle en `CONVENCION_SOAP.md` §2–§3):

| Aspecto | Java (JAX-WS) | .NET (CoreWCF) |
|---|---|---|
| Campos calificados (R3) | Hay que declararlo (`package-info.java`, `targetNamespace`) | Por defecto |
| `SOAPAction` con URI (R4) | Hay que declararla en cada operación | Por defecto |
| Namespace del binding | — | Hay que fijarlo (si no, `tempuri.org`) |

---

## 12. Orden de construcción recomendado

Cada paso cambia un solo eje respecto a una variante ya terminada:

```
1. Java SOAP   (referencia)
       │  cambia protocolo
       ▼
2. Java REST
       │  cambia plataforma
       ▼
3. .NET SOAP   (núcleo y db traducidos de Java; convención SOAP de Java SOAP)
       │  cambia protocolo
       ▼
4. .NET REST   (núcleo y db de .NET SOAP; convención REST de Java REST)
```

| Variante destino | Reutilizar | Traducir | Reescribir |
|---|---|---|---|
| **Java REST** (desde Java SOAP) | modelo, dto, excepciones, db, servicios, controlador | — | `api` completo, verificación manual del contrato |
| **.NET SOAP** (desde Java SOAP) | convención SOAP, catálogo (valores) | modelo, dto, excepciones, servicios, controlador, mapeadores, interfaces de repositorio, pruebas | AccesoDB, SQL de repositorios, tipos de contrato, endpoints, script BD (externo al proyecto) |
| **.NET REST** (desde .NET SOAP + Java REST) | núcleo y `db` de .NET SOAP, convención REST | mapeadores y manejador de errores de Java REST | recursos, tipos de contrato |

**Verificación de cada variante nueva:** enviar las mismas peticiones (válidas, inválidas y casos límite) a la variante de referencia del mismo protocolo y comparar `estado`, `codigo`, `mensaje` y campos propios (`CONVENCION_SOAP.md` §6).

---

## 13. Reglas obligatorias

1. Toda clase pertenece a la raíz `ec.edu.monster`; cada capa es un paquete directo bajo ella.
2. Solo `ws`/`api` conocen los tipos de contrato y el protocolo.
3. El controlador base es el único punto que captura excepciones.
4. Solo existe una excepción de negocio: ExcepcionMonster.
5. Cada entrada del catálogo define código, mensaje y categoría; códigos y textos son idénticos en las cuatro variantes. `ERR-500` es obligatorio.
6. Los errores técnicos nunca exponen detalles internos al cliente.
7. Toda respuesta, de éxito o de error, lleva la envoltura `estado`/`codigo`/`mensaje`.
8. En SOAP, los errores de negocio viajan con HTTP 200; en REST, con el código HTTP de su categoría.
9. Todo dato viaja como texto en el contrato; los tipos reales solo existen dentro del servidor.
10. Solo se crean interfaces cuando la tecnología las exige o cuando existen dos implementaciones reales (la de prueba cuenta).
11. Las dependencias se reciben por constructor; ninguna clase reemplazable es estática.
12. Los cálculos se escriben como métodos independientes, sin acceso a datos.
13. Las reglas de persistencia de la sección 9 se aplican sin excepción.
14. Cada servidor genera su contrato desde el código y cumple su convención; la verificación manual del contrato debe estar hecha y registrada.
15. Toda prueba unitaria debe ejecutarse con la base de datos apagada.
16. En Java, las pruebas manuales replican 1:1 las JUnit y se rigen por `JAVA_TESTS_CONVENTIONS.md`.
17. Las entradas simples llegan al controlador como texto; el controlador las valida y las convierte antes de llamar al servicio.
18. Toda lectura que precede a una modificación de la misma fila la bloquea; con varias filas, siempre en el mismo orden.
19. Un campo propio de un tipo de contrato no se llama `estado`, `codigo` ni `mensaje`.
20. Las clases sin dependencia de dominio (núcleo de plantilla) se copian sin cambios entre proyectos.

---

## 14. Decisiones

**Resueltas en esta versión:**

| Tema | Decisión | Fuente |
|---|---|---|
| Estilo SOAP | SOAP 1.1, document/literal wrapped, UTF-8 (R1) | `CONVENCION_SOAP.md` |
| Errores de negocio | En el cuerpo con envoltura; HTTP 200 en SOAP, HTTP por categoría en REST | Plantillas |
| Estrategia de contrato | Code-first en ambos servidores; copia del contrato solo en clientes | Plantillas |
| Raíz en .NET | `ec.edu.monster` en minúsculas | Plantillas |
| Controlador del servidor | Neutral (devuelve Resultado), con controlador base | Especificación |
| Nombres de paquetes | `servicios` y `excepciones` en plural; el resto en singular | Plantillas |
| Autenticación | Fuera de la especificación base | Especificación |
| Estilo REST | Orientado a recursos, con tabla de endpoints compartida | Especificación |
| Configuración de conexión | Constantes en texto claro dentro de `AccesoDB`; sin archivos de configuración ni scripts en `resources` | Especificación |
| Base de datos | Un esquema por plataforma, compartido por SOAP y REST | Especificación |
| Pruebas en Java | JUnit + Mockito en `src/test`; pruebas manuales al final en `ec.edu.monster.pruebas`, solo de JUnit sin dependencias adicionales, ejecutadas desde el IDE y sin clase agregadora | `JAVA_TESTS_CONVENTIONS.md` |
| Alcance de las pruebas | Solo unitarias; sin base de datos, servidor, UI, navegación, integración ni e2e; el contrato se verifica a mano con una lista explícita | `CONVENCION_PRUEBAS.md` |
| Códigos de error | `PREFIJO-NNN`; reservados `ERR-` y `DATA-` | `CONVENCION_REST.md` |
| Categoría *No autorizado* | Se añade al catálogo (401 en REST; no se usa en el cable SOAP) | Especificación |
| Entradas del servidor | Valores simples como texto; el controlador valida la forma (`ValidadorForma`) y convierte a tipos reales | Especificación |
| Núcleo de plantilla | Las clases sin dependencia de dominio se copian sin cambios a cada proyecto; no hay biblioteca compartida | Especificación |
| Modificación de filas | Lectura con bloqueo dentro de la transacción; varias filas siempre en el mismo orden | Especificación |
| Arquitectura del cliente | Especificada en `ARQUITECTURA_CLIENTE.md` | Especificación |
| Nombres de clases | Entidad primero; prefijo `I` en interfaces .NET; motor como sufijo en repositorios | Especificación |

**Pendientes:**

| # | Tema | Estado |
|---|---|---|
| 1 | Plantillas faltantes: servidores REST, contenido de `soap_server_dotnet` (`soap_java_server` ya incluye `db`) | Se actualizarán con la arquitectura final |
