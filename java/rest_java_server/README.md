# rest_java_server — Plantilla de servidor REST en Java (Jakarta EE 11 + JAX-RS, Payara 7, MySQL)

Plantilla para crear el **servidor** de un SOA Java REST: **Java 21 + Jakarta EE 11 (JAX-RS y JSON-B/JSON-P del propio `jakartaee-api`)** desplegado como WAR en **Payara 7**, con persistencia en **MySQL** por JDBC. Sigue `ARQUITECTURA_SERVIDOR.md` (estructura del código) y `CONVENCION_REST.md` (mensaje en el cable), ambas copiadas en `docs/`; **si discrepan con este README, mandan `context/arquitectura/`**.

**No contiene nada del dominio.** Trae un **núcleo** que se copia sin cambios y un **ejemplo mínimo** (la entidad genérica `Elemento`) que se borra o se sustituye. Es la contraparte de `soap_java_server`: el núcleo es el mismo y solo cambia la capa de protocolo (`ws` ↔ `api`).

> **Estado:** `mvn test` pasa sus **96 pruebas** (JDK 21, Maven 3.9); las 4 clases de pruebas manuales (`ec.edu.monster.pruebas`) dan 10/10; `mvn package` genera el WAR (sin Yasson ni Parsson: solo el driver de MySQL). *(No verificado):* el despliegue de **esta** plantilla en Payara y su ejecución contra una base MySQL real (las pruebas no usan base ni servidor). La misma capa `api` sí se desplegó y se probó a mano en un proyecto Java REST construido a partir de ella.

## 1. Qué contiene

```
rest_java_server/
├── pom.xml                              war, Java 21, jakartaee-api "provided", mysql-connector-j, JUnit 5, Mockito, Yasson/Parsson (test)
├── docs/                                CONVENCION_REST.md y ARQUITECTURA_SERVIDOR.md (copias de context/arquitectura)
│   ├── ENDPOINTS_REST.md                molde de la tabla de endpoints del proyecto (con el ejemplo)
│   └── bdd/Crea_BD.sql                  base y tabla de ejemplo + usuario de la aplicación (fuera del código, no va en resources)
├── src/main/java/ec/edu/monster/
│   ├── NÚCLEO (no depende del dominio; se copia sin cambios)
│   │   ├── dto/Resultado                       estado/codigo/mensaje/datos neutral que devuelve todo controlador
│   │   ├── excepciones/ExcepcionMonster        única excepción de negocio
│   │   ├── controlador/ControladorBase         único punto que captura excepciones (→ Resultado; ERR-500 sin detalles)
│   │   ├── controlador/ValidadorForma          reglas genéricas de forma (obligatorio, longitud, dígitos, decimal)
│   │   ├── db/AccesoDB                         conexiones; solo cambian sus constantes (URL, usuario, clave)
│   │   ├── api/ConversorTexto                  tipos reales → texto del contrato (R9)
│   │   ├── api/CodigoHttp                      HTTP por categoría del error (R7)
│   │   ├── api/CuerpoJson                      lee el cuerpo de una petición: nombres sin distinguir mayúsculas (R4)
│   │   ├── api/RespuestasHttp                  Response: HTTP por categoría + JSON UTF-8 (R3, R7)
│   │   ├── api/ManejadorErrores                errores de plataforma con la envoltura (R6)
│   │   ├── api/AplicacionRest                  @ApplicationPath("api") (R1)
│   │   ├── api/contrato/RespuestaBase          envoltura estado/codigo/mensaje (R5)
│   │   ├── api/contrato/ConvencionRest         prefijo, tipo de contenido y mensajes fijos
│   │   ├── excepciones/CodigoError             catálogo: DATA-001, ERR-404/405/415 y ERR-500 son núcleo; se agregan los del dominio
│   │   └── pruebas/Verificador                 aserciones para las pruebas manuales
│   └── EJEMPLO (sustituir por el dominio)
│       ├── modelo/Elemento · db/ElementoRepositorio(+MySql) · servicios/ElementoServicio
│       ├── controlador/ElementoControlador
│       ├── api/ElementoRecurso · api/ElementoMapeador · api/contrato/Respuesta{Elemento,Elementos}, ElementoDetalle
│       └── pruebas/Pruebas{Excepciones,Controlador,Servicio,Mapeador}
├── src/main/webapp/                     index.html, WEB-INF/{web.xml, beans.xml (CDI), glassfish-web.xml (context root)}
└── src/test/java/ec/edu/monster/        JUnit: una clase de prueba por clase (misma estructura de paquetes)
```

Dependencias solo hacia abajo: `api → controlador → servicios → db`; `modelo`, `dto` y `excepciones` son transversales. Nada fuera de `api` conoce REST.

## 2. Crear un proyecto a partir de la plantilla

1. Copiar la carpeta con el nombre nuevo (sin `target/`).
2. `pom.xml`: `artifactId`, `name` y `finalName`; `glassfish-web.xml`: `<context-root>` (los dos coinciden con el nombre del proyecto). La URL base queda `http://<host>:<puerto>/<context-root>/api/` (R1).
3. `AccesoDB`: URL, usuario y clave de la base. `docs/bdd/Crea_BD.sql`: reemplazar por el script del proyecto (SOAP y REST de una plataforma comparten el mismo).
4. **Escribir `docs/ENDPOINTS_REST.md` antes de implementar** (tabla de endpoints y autenticación del proyecto; el molde trae el ejemplo).
5. Borrar el ejemplo (`Elemento*`, `Respuesta{Elemento,Elementos}`, `ElementoDetalle`, `ELE_*` de `CodigoError`, las pruebas `Elemento*` y `Pruebas{Servicio,Mapeador}` del ejemplo) y crear las piezas del dominio con la receta de §3, usando el ejemplo como molde. Nombres: entidad primero y tipo después (`CuentaServicio`, `CuentaRecurso`…; `ARQUITECTURA_SERVIDOR.md` §3.1).
6. Si el proyecto lo generó NetBeans, quitar su recurso de ejemplo y su `persistence.xml` (no se usa JPA) y comprobar que `jakartaee-api` es **11.0.0** y no el milestone `11.0.0-M1`.
7. Mantener `mvn test` en verde.

## 3. Añadir una operación (receta)

1. **Contrato** (`api/contrato`): POJO que extiende `RespuestaBase`, con `@JsonbPropertyOrder` que **repite** `"estado", "codigo", "mensaje"` antes de los campos propios (JSON-B ordena alfabéticamente), todo `String`; listas inicializadas vacías. Un campo propio nunca se llama `estado`, `codigo` ni `mensaje`. No usar *records* para las respuestas: no pueden heredar la envoltura.
2. **Errores:** agregar a `CodigoError` los códigos nuevos (`PREFIJO-NNN`, con categoría). Nunca se reutiliza un código. El HTTP sale de la categoría (`CodigoHttp`).
3. **Repositorio** (`db`): operación en la interfaz y en la implementación `…MySql` (SQL como constantes, parámetros preparados, nunca abre ni cierra la conexión).
4. **Servicio:** lógica y `enTransaccion` si modifica datos; cálculos como métodos estáticos puros; lanza `ExcepcionMonster`.
5. **Controlador:** valida la forma con `ValidadorForma`, convierte a tipos reales y `return ejecutar(() -> servicio.x(…));`
6. **Mapeador y recurso:** el mapeador pasa `Resultado` → contrato con `ConversorTexto`. En el recurso (`…Recurso`): una sola clase por recurso en plural; `@GET`/`@POST` con su `@Path`; los cuerpos entran como `String` y se leen con `CuerpoJson.leer(cuerpo)`; se devuelve `RespuestasHttp.de(…)`. **No** declarar `@Produces`; **todo éxito responde 200**.
7. **Pruebas:** servicio (Mockito), controlador, mapeador, y la forma del JSON del tipo nuevo (`ContratoJsonTest`); réplica en `pruebas/`. El contrato REST (verbos, rutas, HTTP) se verifica **a mano** contra `docs/ENDPOINTS_REST.md` (`CONVENCION_PRUEBAS.md` §6.2).

## 4. La convención aplicada

| Regla | Dónde está | Por qué |
|---|---|---|
| **R1** prefijo y URL base | `ConvencionRest.PREFIJO` + `AplicacionRest` + `glassfish-web.xml` | el cliente guarda la URL base en un solo lugar |
| **R3** JSON UTF-8 | `ConvencionRest.JSON_UTF8` en `RespuestasHttp` | única codificación del contrato |
| **R4** nombres fijos, sin distinguir mayúsculas al leer | `@JsonbPropertyOrder` y getters (salida); `CuerpoJson` (entrada) | JSON-B no ignora mayúsculas al deserializar, por eso el cuerpo se lee con JSON-P |
| **R5** respuesta uniforme | `RespuestaBase` + `Resultado` + mapeadores | éxito y error con el mismo tipo; listas vacías, nunca ausentes |
| **R6** errores de plataforma con envoltura | `ManejadorErrores` (`@Provider`, `ExceptionMapper<Throwable>`) | el framework responde con HTML si no está registrado |
| **R7** HTTP por categoría | `CodigoHttp` + `RespuestasHttp` | agregar un error solo exige una entrada en el catálogo |
| **R8** credenciales fuera de la URL y de los registros | **no se implementa** (ver §7) | cada proyecto define su mecanismo |
| **R9** datos como texto | `ConversorTexto` | sin diferencias de cultura ni de tipos entre plataformas |

## 5. Ejecutar

```
mvn test        # sin Payara y sin base de datos (la lógica se simula con Mockito)
mvn package     # target/rest_java_server.war
```

Pruebas manuales (réplica de JUnit, sin Maven): ejecutar `main` de `ec.edu.monster.pruebas.Pruebas{Excepciones,Controlador,Servicio,Mapeador}` desde el IDE, o por línea de comandos `java "-Dfile.encoding=UTF-8" -cp target/classes ec.edu.monster.pruebas.PruebasMapeador` (en PowerShell, el argumento `-D` va entre comillas).

**Base de datos:** ejecutar `docs/bdd/Crea_BD.sql` en MySQL (crea `ejemplo`, la tabla y el usuario `ejemplo`/`admin` con permisos solo sobre ella).

**Desplegar en Payara 7:** consola `http://localhost:4848`, `asadmin deploy target/rest_java_server.war` o desde el IDE. Con el context root de `glassfish-web.xml`:

| Qué | Petición |
|---|---|
| Listar | `GET http://localhost:8080/rest_java_server/api/elementos` |
| Consultar | `GET http://localhost:8080/rest_java_server/api/elementos/0001` |
| Registrar | `POST http://localhost:8080/rest_java_server/api/elementos` con `Content-Type: application/json` y cuerpo `{"codigoElemento":"0004","nombre":"Cuarto","valor":"5.00"}` |

**Tras cambiar el contrato hay que actualizar `docs/ENDPOINTS_REST.md`, redesplegar y actualizar todos los clientes** (`api` y `dto` de cada uno). La verificación manual del contrato está en `CONVENCION_PRUEBAS.md` §6.2.

## 6. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| Una ruta inexistente responde con una página HTML | `ManejadorErrores` no se registró (sin `@Provider` o fuera del paquete escaneado) | Dejarlo en `ec.edu.monster.api` con `@Provider` |
| Los campos salen en orden alfabético (`codigo` antes que `estado`) | Falta `@JsonbPropertyOrder` en la subclase | Listar en cada respuesta `estado`, `codigo`, `mensaje` y luego los campos propios |
| Una respuesta de error trae `null` en los campos propios | Se serializó con otro motor o se pidió incluir nulos | JSON-B omite los nulos por defecto: no cambiar esa opción |
| `406 Not Acceptable` con cuerpo ajeno a la envoltura | Se declaró `@Produces` en un recurso | Quitarlo; el tipo lo fija `RespuestasHttp` |
| Un recurso nuevo no responde (404 de la API) | Dos clases con el mismo `@Path` raíz | Agrupar las operaciones del recurso en una sola clase |
| `jakarta.ws.rs.*` o `jakarta.json.*` no reconocido | Falta `jakartaee-api` o el IDE no recargó el POM | *Reload POM* y *Clean and Build*; no agregar `webservices-rt` |
| `ContratoJsonTest` o `CuerpoJsonTest` fallan al crear el lector o el serializador de JSON | Faltan `yasson` y `parsson` de alcance `test` (la API sola no trae implementación) | Restaurarlos en el `pom.xml` |
| 404 en todas las rutas | Context root distinto | Revisar `glassfish-web.xml` y la consola de Payara |
| `ERR-500` en cada llamada | La base no existe, está apagada o las constantes de `AccesoDB` no coinciden | Ejecutar el script, revisar URL/usuario/clave; el detalle está en el log de Payara |

## 7. Alcance

- **Autenticación y autorización quedan fuera de la plantilla base** (cada proyecto las define en su `docs/ENDPOINTS_REST.md`; R8 de la convención solo fija que las credenciales no viajan en la URL ni se registran). La categoría `NO_AUTORIZADO` y su HTTP 401 ya existen en el catálogo. Al agregarla: leer las credenciales en `api` (nunca en el núcleo), pasarlas al controlador como texto y completar `RespuestasHttp` con lo que el mecanismo exija en un 401.
- **CORS:** no se añade (solo lo aplican los navegadores; ver la decisión del contrato del proyecto).
- **`OPTIONS` y `HEAD`:** los atiende el framework con su propia respuesta, sin la envoltura. **(No verificado)** qué devuelve Payara.
- Sin pool de conexiones y sin archivos de configuración, por tratarse de entornos locales académicos.

## 8. Entorno de referencia

Windows 11, JDK 21, Maven 3.9.11, `jakarta.jakartaee-api` 11.0.0, JUnit 5.10.2, Mockito 5.11.0, Yasson 3.0.4 y Parsson 1.1.6 (solo prueba), `mysql-connector-j` 8.4.0, Payara 7 (ver `context/entornos/ENTORNO_JEE.md`).
