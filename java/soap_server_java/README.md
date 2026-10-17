# soap_server_java — Plantilla de servidor SOAP en Java (Jakarta EE 11 + JAX-WS, Payara 7)

Plantilla para crear servidores **SOAP/WSDL** con **Java 21 + Jakarta EE 11 (JAX-WS/JAXB de Metro)** desplegados como WAR en **Payara 7**, que cumplen la **convención de interoperabilidad** del equipo: cualquier cliente —Java, .NET o Android a mano— puede usarlos, y un servidor .NET que siga la misma convención es intercambiable cambiando solo una URL.
Es la hermana de `soap_server_dotnet` (CoreWCF). **No contiene nada del dominio**: incluye un servicio de ejemplo mínimo (`eco` y `listar`) para borrar o sustituir.

> **Estado:** compila y pasa sus 16 pruebas con `mvn test` (Maven 3.9, JDK 21), incluidas 7 pruebas de **conformidad** que publican el servicio con Metro —el mismo motor de Payara— y lo llaman por SOAP. Se comprobó que esas pruebas **fallan** si se quita `package-info.java`. *Por verificar:* el despliegue del WAR en Payara desde esta plantilla (el proyecto de origen sí funciona en Payara; el WAR de la plantilla se construye con el mismo `pom.xml` y descriptores).

## 1. Qué contiene

```
soap_server_java/
├── pom.xml                                  war, Java 21, jakartaee-api 11 + Metro (webservices-rt) "provided", JUnit 5
├── CONVENCION_SOAP.md                       reglas de interoperabilidad (copia de la del proyecto de origen; independiente del dominio)
├── src/main/java/ec/edu/monster/
│   ├── ws/WSEjemplo.java                    punto de entrada SOAP: sin lógica, solo delega
│   ├── controlador/ControladorBase.java     traduce excepciones a respuestas de error (y registra las inesperadas)
│   ├── controlador/EjemploControlador.java  ejemplo: coordina servicio -> respuesta
│   ├── servicios/ServicioEjemplo.java       ejemplo de lógica, sin SOAP
│   ├── dto/                                 EspacioNombres (TNS), package-info (campos calificados), RespuestaBase/Eco/Lista, FabricaRespuestas
│   ├── excepciones/                         CodigoError, ExcepcionNegocio
│   └── utilidades/ConfiguracionServidor.java
├── src/main/webapp/                         index.html, WEB-INF/web.xml, WEB-INF/beans.xml (CDI, bean-discovery-mode="all")
└── src/test/java/ec/edu/monster/
    ├── servicios/ServicioEjemploTest        lógica, sin servidor
    ├── controlador/EjemploControladorTest   errores de negocio, ERR-500 sin filtrar detalles
    └── ws/ConvencionSoapTest                conformidad: WSDL/XSD y mensajes reales en el cable
```

Capas (dependencias solo hacia abajo): `ws → controlador → servicios`; `dto`, `excepciones` y `utilidades` son transversales. `servicios` no importa nada de `jakarta.jws` ni de los DTO.

## 2. Crear un proyecto a partir de la plantilla

1. Copiar la carpeta con el nombre nuevo (sin `target/`).
2. En `pom.xml`: `artifactId`, `name` y **`<finalName>`** (el nombre del WAR es el *context root* por defecto en Payara).
3. **Fijar el namespace de destino en un solo lugar:** `dto/EspacioNombres.SERVICIO` (forma sugerida `http://<dominio-invertido>/`, con barra final). Lo usan `@WebService`, `@WebParam`, `@WebResult`, `action` y `package-info.java`.
4. Renombrar `WSEjemplo` y su `serviceName` por el nombre del servicio (PascalCase, p. ej. `WSMiServicio`), ajustando en cada `action` el segmento `WSEjemplo/` (la acción es `TNS + <Servicio> + "/" + <operación>`), el enlace de `index.html` y `ConvencionSoapTest`.
5. Sustituir las operaciones de ejemplo por las del dominio (§3) y borrar `RespuestaEcoDto`, `RespuestaListaDto` (o dejar la lista si la usa), `ServicioEjemplo` y `EjemploControlador`.
6. Ajustar los paquetes si no se usará `ec.edu.monster` (refactor del IDE; `package-info.java` debe quedar en el paquete de los DTO).
7. `mvn test`: las pruebas de conformidad deben seguir en verde.

## 3. Añadir una operación (receta)

1. **DTO** que extiende `RespuestaBaseDto`: `@XmlAccessorType(FIELD)`, `@XmlType(name = "RespuestaX", propOrder = {…})`, campos `private String …` (todo `String`; opcionales sin valor quedan `null` y JAXB los omite; listas inicializadas con `new ArrayList<>()` y `@XmlElementWrapper` + `@XmlElement`).
2. **`FabricaRespuestas`:** `okX(…)` y `errorX(ExcepcionNegocio)` (el error conserva el **mismo tipo**).
3. **Servicio:** método que lanza `ExcepcionNegocio(CodigoError.…)` ante un error de negocio. Añadir los códigos a `CodigoError`.
4. **Controlador:** `return ejecutar(() -> FabricaRespuestas.okX(servicio.x(…)), FabricaRespuestas::errorX);`
5. **`WS…`:** copiar una operación completa conservando las **tres** declaraciones de la convención:
   ```java
   @WebMethod(operationName = "x", action = EspacioNombres.SERVICIO + "WSMiServicio/x")
   @WebResult(name = "respuesta", targetNamespace = EspacioNombres.SERVICIO)
   public RespuestaXDto x(@WebParam(name = "param", targetNamespace = EspacioNombres.SERVICIO) String param) { … }
   ```
6. Añadir pruebas en `ConvencionSoapTest` (campos calificados, error de negocio con HTTP 200).

## 4. La convención aplicada (qué hace cada pieza)

Resumen de `CONVENCION_SOAP.md` §3.1 (leerlo completo antes de usar la plantilla):

| Regla | Dónde está en la plantilla | Por qué |
|---|---|---|
| **R3** campos de los tipos calificados | `dto/package-info.java`: `@XmlSchema(namespace = TNS, elementFormDefault = QUALIFIED)` | JAXB los deja **sin** namespace por defecto; .NET los espera con namespace |
| **R3** envoltorio, parámetros y retorno calificados | `targetNamespace = TNS` en `@WebParam` y `@WebResult` | es otra capa (JAX-WS) y se declara aparte |
| **R4** `SOAPAction` = URI de la operación | `@WebMethod(action = TNS + "<Servicio>/<op>")` | JAX-WS usa `""`; WCF/.NET la rechaza con HTTP 500 |
| **R5** nombres fijos | `serviceName`, `operationName`, `@WebResult(name = "respuesta")`, `@XmlType(name = …)` | los clientes dependen de ellos |
| **R6** respuesta uniforme | `RespuestaBaseDto` (`estado`/`codigo`/`mensaje`) + `FabricaRespuestas` | éxito y error con el mismo tipo |
| **R7** datos simples | todo `String`, opcionales omitidos, listas vacías (nunca ausentes) | sin diferencias de cultura ni de tipos entre plataformas |
| **R8** errores en el cuerpo | `ControladorBase.ejecutar`: `ExcepcionNegocio` → respuesta `ERROR` (HTTP 200); cualquier otra → `ERR-500` genérico | el cliente distingue red/protocolo de negocio |

**Sin `package-info.java`** el servicio queda a medias: el envoltorio sale calificado y los campos del tipo **no** (`<ns2:respuesta><estado>…`), y un cliente .NET lee `estado` como vacío. `ConvencionSoapTest` lo detecta.

## 5. Ejecutar

### Pruebas (sin Payara)
```
mvn test
```
`ConvencionSoapTest` publica `WSEjemplo` con `Endpoint.publish` en un puerto libre y comprueba el WSDL, el XSD y los mensajes. Las dependencias de Metro son `provided`: las pruebas las ven, el WAR no las empaqueta.

### Desplegar en Payara 7
```
mvn package            # genera target/soap_server_java.war
```
Desplegar el WAR con la consola de administración de Payara (`http://localhost:4848`), con `asadmin deploy target/soap_server_java.war` o desde NetBeans/IntelliJ (*Run* con el servidor Payara). Con el `finalName` del `pom.xml`:

| Qué | URL |
|---|---|
| Servicio | `http://localhost:8080/soap_server_java/WSEjemplo` |
| WSDL | `http://localhost:8080/soap_server_java/WSEjemplo?wsdl` |
| Esquema | `http://localhost:8080/soap_server_java/WSEjemplo?xsd=1` |

Para otro *context root*: cambiar `finalName`, o `asadmin deploy --contextroot /otro …`, o un `glassfish-web.xml` con `<context-root>`.
**Tras cambiar el contrato (operaciones, parámetros, tipos) hay que redesplegar y regenerar o actualizar todos los clientes.**

### Qué revisar en el WSDL desplegado
`XSD` con `elementFormDefault="qualified"`; en el WSDL, `soapAction` con la URI de cada operación; puerto `<Servicio>Port`. (Lista completa en `CONVENCION_SOAP.md` §6.)

## 6. Consumir el servicio

- **Cliente .NET:** generar con `dotnet-svcutil` o *WCF Web Service Reference* desde `…?wsdl`; fijar el endpoint por configuración. Si dos servidores (Java y .NET) siguen la convención, el mismo proxy sirve para ambos cambiando la URL.
- **Cliente Java (JAX-WS):** `wsimport`/`jaxws-maven-plugin` desde una copia del WSDL; si el XSD de otro servidor trae `nillable`, usar un archivo de binding con `generateElementProperty="false"`. Escrito a mano: misma interfaz con las anotaciones de §3 y el mismo `package-info.java`; crear el puerto con `getPort(Interfaz.class)` (sin `QName` de puerto).
- **Android / HTTP a mano:** parámetros con el prefijo del TNS, `SOAPAction` con la URI, lectura de la respuesta por nombre local (`CONVENCION_SOAP.md` §4 c).

## 7. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| Un cliente .NET recibe `estado`, `codigo`… vacíos o nulos | Falta `package-info.java`, está en otro paquete o no se compiló | Colocarlo en el paquete de los DTO; comprobar que el WAR contiene `package-info.class` |
| El servicio rechaza los parámetros ("campos obligatorios" con datos válidos) | El cliente manda los parámetros **sin** namespace | Calificarlos con el TNS (regla R3); en Android, prefijo `ns:` en cada parámetro |
| El servidor .NET responde HTTP 500 `ActionNotSupported` al cliente de Java | El cliente envía `SOAPAction: ""` | Declarar `action=` en `@WebMethod` y regenerar/actualizar el cliente |
| El cliente falla al crear el puerto: "`WSXPort` is not a valid port" | Pide el puerto por nombre y el servidor lo llama distinto | Usar `getPort(Interfaz.class)` (regla R10) |
| `ConvencionSoapTest` falla con "Address already in use" | Puerto ocupado | Ya usa un puerto libre; cerrar procesos Java colgados |
| 404 en `…/WSEjemplo?wsdl` | Context root distinto del esperado | Ver el nombre del WAR / `finalName` y la consola de Payara |
| `ClassNotFoundException` de JAX-WS al ejecutar fuera de Payara | Metro es `provided` | Ejecutar con las pruebas (`mvn test`) o añadir el classpath de Metro |

## 8. Alcance

Sin base de datos, sin seguridad (la autenticación es del dominio: token, WS-Security, etc.), sin REST. Una sola instancia: si el servicio guarda estado en memoria (tokens, sesiones), debe ser *thread-safe* (`ConcurrentHashMap`) y de ámbito `@ApplicationScoped`.

## 9. Entorno de referencia (verificado el 2026-10-16)

Windows 11, JDK 21, Maven 3.9.11, Jakarta EE 11 (`jakarta.jakartaee-api` 11.0.0), Metro `webservices-rt` 4.0.6, JUnit 5.12.2, Payara 7.
