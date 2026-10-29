# soap_java_server — Plantilla de servidor SOAP en Java (Jakarta EE 11 + JAX-WS, Payara 7, MySQL)

Plantilla para crear el **servidor** de un SOA Java SOAP: **Java 21 + Jakarta EE 11 (JAX-WS/JAXB de Metro)** desplegado como WAR en **Payara 7**, con persistencia en **MySQL** por JDBC. Sigue `ARQUITECTURA_SERVIDOR.md` (estructura del código) y `CONVENCION_SOAP.md` (mensaje en el cable), ambas copiadas en `docs/`; **si discrepan con este README, mandan `context/arquitectura/`**.

**No contiene nada del dominio.** Trae un **núcleo** que se copia sin cambios y un **ejemplo mínimo** (la entidad genérica `Elemento`) que se borra o se sustituye.

> **Estado:** `mvn test` pasa sus **80 pruebas** (JDK 21, Maven 3.9), incluidas 6 de **conformidad** que publican el servicio con Metro —el mismo motor de Payara— y lo llaman por SOAP; las 4 clases de pruebas manuales (`ec.edu.monster.pruebas`) dan 10/10; `mvn package` genera el WAR. *(No verificado):* el despliegue del WAR en Payara y la ejecución contra una base MySQL real (las pruebas no usan base de datos).

## 1. Qué contiene

```
soap_java_server/
├── pom.xml                              war, Java 21, jakartaee-api + Metro "provided", mysql-connector-j, JUnit 5, Mockito
├── docs/                                CONVENCION_SOAP.md y ARQUITECTURA_SERVIDOR.md (copias de context/arquitectura)
│   └── bdd/Crea_BD.sql                  base y tabla de ejemplo + usuario de la aplicación (fuera del código, no va en resources)
├── src/main/java/ec/edu/monster/
│   ├── NÚCLEO (no depende del dominio; se copia sin cambios)
│   │   ├── dto/Resultado                       estado/codigo/mensaje/datos neutral que devuelve todo controlador
│   │   ├── excepciones/ExcepcionMonster        única excepción de negocio
│   │   ├── controlador/ControladorBase         único punto que captura excepciones (→ Resultado; ERR-500 sin detalles)
│   │   ├── controlador/ValidadorForma          reglas genéricas de forma (obligatorio, longitud, dígitos, decimal)
│   │   ├── db/AccesoDB                         conexiones; solo cambian sus constantes (URL, usuario, clave)
│   │   ├── ws/ConversorTexto                   tipos reales → texto del contrato
│   │   ├── ws/contrato/RespuestaBase           envoltura estado/codigo/mensaje (R6)
│   │   ├── ws/contrato/package-info            campos calificados con el TNS (R3)
│   │   ├── ws/contrato/ConvencionSoap          TNS y constantes (solo se agregan servicios/acciones)
│   │   ├── excepciones/CodigoError             catálogo: DATA-001, ERR-404/405/415 y ERR-500 son núcleo; se agregan los del dominio
│   │   └── pruebas/Verificador                 aserciones para las pruebas manuales
│   └── EJEMPLO (sustituir por el dominio)
│       ├── modelo/Elemento · db/ElementoRepositorio(+MySql) · servicios/ElementoServicio
│       ├── controlador/ElementoControlador
│       ├── ws/ElementoWS · ws/ElementoMapeador · ws/contrato/Respuesta{Elemento,Elementos}, ElementoDetalle
│       └── pruebas/Pruebas{Excepciones,Controlador,Servicio,Mapeador}
├── src/main/webapp/                     index.html, WEB-INF/{web.xml, beans.xml (CDI), glassfish-web.xml (context root)}
└── src/test/java/ec/edu/monster/        JUnit: una clase de prueba por clase (misma estructura de paquetes)
```

Dependencias solo hacia abajo: `ws → controlador → servicios → db`; `modelo`, `dto` y `excepciones` son transversales. Nada fuera de `ws` conoce SOAP.

## 2. Crear un proyecto a partir de la plantilla

1. Copiar la carpeta con el nombre nuevo (sin `target/`).
2. `pom.xml`: `artifactId`, `name` y `finalName`; `glassfish-web.xml`: `<context-root>` (los dos coinciden con el nombre del proyecto).
3. `AccesoDB`: URL, usuario y clave de la base. `docs/bdd/Crea_BD.sql`: reemplazar por el script del proyecto (SOAP y REST de una plataforma comparten el mismo).
4. `ConvencionSoap`: nombre(s) del servicio y su prefijo de `SOAPAction`. El TNS (`http://ws.monster.edu.ec/`) **no se cambia**.
5. Borrar el ejemplo (`Elemento*`, `Respuesta{Elemento,Elementos}`, `ElementoDetalle`, `ELE_*` de `CodigoError`, las pruebas `Elemento*` y `Pruebas{Servicio,Mapeador}` del ejemplo) y crear las piezas del dominio con la receta de §3, usando el ejemplo como molde. Nombres: entidad primero y tipo después (`CuentaServicio`, `CuentaWS`…; `ARQUITECTURA_SERVIDOR.md` §3.1).
6. Ajustar `ConvencionSoapTest` (nombre del servicio, operaciones y campos) y mantener `mvn test` en verde.

## 3. Añadir una operación (receta)

1. **Contrato** (`ws/contrato`): tipo que extiende `RespuestaBase`, con `@XmlAccessorType(FIELD)`, `@XmlType(name = …, propOrder = …)`, todo `String`; listas inicializadas vacías con `@XmlElementWrapper` + `@XmlElement`. Un campo propio nunca se llama `estado`, `codigo` ni `mensaje`.
2. **Errores:** agregar a `CodigoError` los códigos nuevos (`PREFIJO-NNN`, con categoría). Nunca se reutiliza un código.
3. **Repositorio** (`db`): operación en la interfaz y en la implementación `…MySql` (SQL como constantes, parámetros preparados, nunca abre ni cierra la conexión).
4. **Servicio:** lógica y `enTransaccion` si modifica datos; cálculos como métodos estáticos puros; lanza `ExcepcionMonster`.
5. **Controlador:** valida la forma con `ValidadorForma`, convierte a tipos reales y `return ejecutar(() -> servicio.x(…));`
6. **Mapeador y endpoint:** el mapeador pasa `Resultado` → contrato con `ConversorTexto`; en el `…WS` conservar las **tres** declaraciones por operación (`action` con URI, `@WebResult(name = "respuesta")`, `@WebParam` con `targetNamespace`).
7. **Pruebas:** servicio (Mockito), controlador, mapeador y conformidad (`ConvencionSoapTest`); réplica en `pruebas/`.

## 4. La convención aplicada

| Regla | Dónde está | Por qué |
|---|---|---|
| **R2** un solo TNS | `ConvencionSoap.TNS` | lo referencian `@WebService`, `@WebParam`, `@WebResult`, `action` y `package-info` |
| **R3** campos calificados | `ws/contrato/package-info.java` + `targetNamespace` en `@WebParam`/`@WebResult` | JAXB los deja sin namespace; .NET los espera con él |
| **R4** `SOAPAction` = URI | `@WebMethod(action = ACCION_… + "<op>")` | JAX-WS usa `""`; WCF/.NET lo rechaza |
| **R5** nombres fijos | `serviceName`, `portName`, `operationName`, `name = "respuesta"`, `@XmlType(name)` | los clientes dependen de ellos |
| **R6** respuesta uniforme | `RespuestaBase` + `Resultado` + mapeadores | éxito y error con el mismo tipo |
| **R7** datos como texto | `ConversorTexto` | sin diferencias de cultura ni de tipos entre plataformas |
| **R8** errores en el cuerpo | `ControladorBase.ejecutar` (`ExcepcionMonster` → código y mensaje; otra → `ERR-500`) | HTTP 200 con `estado = ERROR`, sin SOAP Fault |

Sin `package-info.java` el envoltorio sale calificado pero los campos no; `ConvencionSoapTest` lo detecta.

## 5. Ejecutar

```
mvn test        # sin Payara y sin base de datos (la lógica se simula con Mockito)
mvn package     # target/soap_java_server.war
```

Pruebas manuales (réplica de JUnit, sin Maven): ejecutar `main` de `ec.edu.monster.pruebas.Pruebas{Excepciones,Controlador,Servicio,Mapeador}` desde el IDE.

**Base de datos:** ejecutar `docs/bdd/Crea_BD.sql` en MySQL (crea `ejemplo`, la tabla y el usuario `ejemplo`/`admin` con permisos solo sobre ella).

**Desplegar en Payara 7:** consola `http://localhost:4848`, `asadmin deploy target/soap_java_server.war` o desde el IDE. Con el context root de `glassfish-web.xml`:

| Qué | URL |
|---|---|
| WSDL | `http://localhost:8080/soap_java_server/ServicioElemento?wsdl` |
| Esquema | `http://localhost:8080/soap_java_server/ServicioElemento?xsd=1` |

**Tras cambiar el contrato hay que redesplegar y regenerar o actualizar todos los clientes.** La verificación manual del contrato real está en `CONVENCION_PRUEBAS.md` §6.

## 6. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| Un cliente .NET recibe `estado`, `codigo`… vacíos | Falta `package-info.java` o está en otro paquete | Debe estar en `ws/contrato` junto a los tipos |
| El servicio rechaza parámetros válidos (`DATA-001`) | El cliente los manda sin namespace | Calificarlos con el TNS (R3) |
| .NET responde HTTP 500 `ActionNotSupported` | El cliente envía `SOAPAction: ""` | Declarar `action=` en `@WebMethod` y regenerar el cliente |
| `ConvencionSoapTest` falla con "Address already in use" | Puerto ocupado | Usa un puerto libre; cerrar procesos Java colgados |
| 404 en `…?wsdl` | Context root distinto | Revisar `glassfish-web.xml` y la consola de Payara |
| `ERR-500` en cada llamada | La base no existe, está apagada o las constantes de `AccesoDB` no coinciden | Ejecutar el script, revisar URL/usuario/clave; el detalle está en el log de Payara |

## 7. Alcance

Autenticación y autorización quedan fuera de la plantilla base (cada proyecto las define; la categoría `NO_AUTORIZADO` ya existe en el catálogo). Sin pool de conexiones y sin archivos de configuración, por tratarse de entornos locales académicos.

## 8. Entorno de referencia

Windows 11, JDK 21, Maven 3.9.11, `jakarta.jakartaee-api` 11.0.0, Metro `webservices-rt` 4.0.6, JUnit 5.10.2, Mockito 5.11.0, `mysql-connector-j` 8.4.0, Payara 7 (ver `context/entornos/ENTORNO_JEE.md`).
