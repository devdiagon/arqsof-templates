# CONVENCION_SOAP — Convención de interoperabilidad SOAP (Java ⇄ .NET ⇄ móvil)

Reglas para que **cualquier servicio SOAP** del equipo —escrito en Java (JAX-WS/Jakarta EE) o en .NET (CoreWCF) y de cualquier dominio— sea consumible por **cualquier cliente** (Java, .NET, Android a mano) cambiando solo una URL.
No describe el dominio del servicio (conversión de unidades, ventas, etc.): eso va en `RESUMEN_FUNCIONALIDADES.md`. Aquí solo se define **cómo se ve el SOAP en el cable** y qué hace cada plataforma para producirlo.

> Estado: **verificada** el 2026-10-16 con un servidor Java (Payara/Metro) y un servidor .NET (CoreWCF) que implementan el mismo servicio, y con clientes JAX-WS, clientes generados con `dotnet-svcutil` y un cliente Android armado a mano (ver §6).

---

## 0. Idea central

**No se busca que los dos WSDL sean idénticos**; se busca que **el XML que viaja sea el mismo** y que cada plataforma siga **generando su propio WSDL automáticamente** con su herramienta por defecto, sin código especial de transporte. Se consigue acordando unas pocas reglas (§1) y aplicando en cada plataforma un puñado de declaraciones (§3).

Se intentó antes lo contrario —copiar el WSDL de Java en el servidor .NET y despachar los mensajes a mano— y se **descartó**: funciona, pero obliga a mantener un contrato duplicado y código de bajo nivel en cada servicio.

---

## 1. Reglas del contrato

| # | Regla | Por qué |
|---|---|---|
| **R1** | **SOAP 1.1 sobre HTTP, estilo `document/literal` con parámetros envueltos (*wrapped*), UTF‑8.** Es el valor por defecto de JAX-WS y de WCF. | Es el estilo que ambos generan y que todas las herramientas de clientes entienden. |
| **R2** | **Un solo namespace de destino (TNS)** para el servicio, los envoltorios y los tipos. Forma sugerida: `http://<dominio-invertido>/` con barra final. | Evita mezclar namespaces entre tipos y mensajes. |
| **R3** | **Todos los elementos van calificados con el TNS** (`elementFormDefault="qualified"`): el envoltorio de la operación, cada parámetro, el elemento de retorno y cada campo de los tipos. Ejemplo: `<ws:login><ws:usuario>…</ws:usuario></ws:login>`. | En .NET (`DataContract`) siempre es así y **no se puede cambiar de forma idiomática**; en Java por defecto es lo contrario (campos sin namespace) y **sí se puede declarar**. Es la regla que más rompe la interoperabilidad. |
| **R4** | **`SOAPAction` = TNS + `<NombreServicio>` + `/` + `<operación>`**, entre comillas: `"http://ws.ejemplo.edu.ec/MiServicio/login"`. | Es el valor que WCF genera por defecto y **exige**: rechaza la cabecera vacía con HTTP 500 (`ActionNotSupported`). JAX-WS usa `""` por defecto, así que hay que declararlo. Los clientes **siempre la envían**. |
| **R5** | **Los nombres son parte del contrato y no derivan del código**: servicio y tipos en PascalCase (`MiServicio`, `RespuestaBase`), operaciones y parámetros en camelCase (`obtenerUnidades`, `unidadOrigen`), un único elemento de retorno llamado `respuesta`. | Los clientes generados y los escritos a mano dependen de ellos. Cambiarlos obliga a regenerar todos los clientes. |
| **R6** | **Toda operación devuelve un tipo que extiende `RespuestaBase`** con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`; los campos propios de cada operación van después, en orden fijo. Éxito y error usan **el mismo tipo**. | El cliente revisa siempre los mismos tres campos y el WSDL no cambia entre éxito y error. |
| **R7** | **Datos simples como `string`.** Números con punto decimal y sin notación científica, fechas en ISO‑8601, sin tipos propios de una plataforma (`decimal`, `DateTime`, enumeraciones) en el contrato. Campos opcionales **omitidos** cuando no tienen valor. Las listas son un elemento contenedor con el elemento repetido dentro (`<elementos><elemento>…</elemento></elementos>`) y **llegan vacías, nunca ausentes**, en caso de error. | Evita diferencias de cultura, de precisión y de serialización entre plataformas. |
| **R8** | **Los errores de negocio viajan en el cuerpo** (`estado=ERROR`, `codigo`, `mensaje`) con **HTTP 200**. El SOAP Fault queda para fallos de protocolo (operación inexistente, XML mal formado). Toda excepción no controlada se traduce a un código genérico (`ERR-500`) **sin filtrar detalles internos**. | El cliente distingue "falló la red/el protocolo" (excepción) de "falló el negocio" (respuesta). |
| **R9** | **El cliente usa la URL completa del servicio, escrita como constante en el código** (un solo lugar, valor por defecto no secreto), y **no asume su forma** (Java: `/<contexto>/<Servicio>`; .NET: una ruta libre). Puede sobrescribirse sin recompilar (propiedad, variable de entorno), pero la constante es la fuente normal. El WSDL se publica en `<url>?wsdl`. | Cambiar de servidor es cambiar una línea; un clon del repositorio funciona en local sin configurar secretos. |
| **R10** | **El cliente no depende de nombres que decide la plataforma**: nombre del puerto o del binding, número de documentos del WSDL, orden de las operaciones. Pide el puerto por la interfaz (`getPort(Interfaz)`) o usa la clase generada. | CoreWCF nombra el puerto `<Binding>_<Contrato>` y no se puede cambiar; Metro usa `<Servicio>Port`. |

**Convención de errores/estado y de seguridad.** `estado/codigo/mensaje` (R6) y el paso de un *token* como parámetro son decisiones de este conjunto de proyectos, no del protocolo: otro dominio puede definir otra envoltura, pero debe cumplir R1–R5 y R7–R10.

---

## 2. Por qué estas reglas: qué hace cada plataforma "por defecto"

Medido contra el servicio real (no es teoría):

| Aspecto | JAX-WS / JAXB (Java) | CoreWCF / DataContract (.NET) | Decisión |
|---|---|---|---|
| Campos dentro de los mensajes | **sin namespace** (`<usuario>`) | **con** el namespace del servicio | Calificados (R3): Java lo declara, .NET ya lo hace |
| `SOAPAction` | `""` | `<TNS><Contrato>/<operación>` | URI por operación (R4): Java lo declara |
| Nombre del puerto | `<Servicio>Port` | `<Binding>_<Contrato>` | No depender de él (R10) |
| Campos `string` en el XSD | `minOccurs="0"` | `minOccurs="0"` y `nillable="true"` | Aceptado; ver §5 |
| Binding en el WSDL | mismo documento | documento aparte con `wsdl:import` | Aceptado (R10) |
| Binding sin `Namespace` | — | sale en `http://tempuri.org/` | Fijar `Namespace` del binding y del contrato |

Consecuencia práctica de **no** aplicar R3/R4 (medida): un cliente escrito para un servidor leía **campos vacíos** del otro (login válido → «campos obligatorios») y el servidor .NET devolvía HTTP 500 a la `SOAPAction` vacía.

---

## 3. Recetas para el servidor

Marcadores: `<TNS>` = namespace de destino, `<Servicio>` = nombre del servicio, `<op>` = operación. No hay lógica de dominio aquí: las clases de negocio no cambian.

### 3.1 Java (JAX-WS / JAXB)
1. **`package-info.java`** en el paquete de los tipos de respuesta (mismo paquete, mismo nombre de carpeta):
   ```java
   @XmlSchema(namespace = "<TNS>", elementFormDefault = XmlNsForm.QUALIFIED)
   package <paquete.dto>;

   import jakarta.xml.bind.annotation.XmlNsForm;
   import jakarta.xml.bind.annotation.XmlSchema;
   ```
   Hace que **los campos de los tipos** (capa JAXB) vayan calificados. Se puede sustituir por `@XmlElement(namespace="<TNS>")` en cada campo (y en `@XmlElementWrapper`/`@XmlElement` de las listas), pero es repetitivo y un campo nuevo sin anotar queda sin namespace sin avisar.
2. **En la clase del servicio** (capa JAX-WS: envoltorio, parámetros y retorno), en **cada** operación:
   ```java
   @WebMethod(operationName = "<op>", action = "<TNS><Servicio>/<op>")
   @WebResult(name = "respuesta", targetNamespace = "<TNS>")
   public RespuestaX <op>(@WebParam(name = "parametro", targetNamespace = "<TNS>") String parametro) { … }
   ```
   con `@WebService(serviceName = "<Servicio>", targetNamespace = "<TNS>")` en la clase.
3. Tipos: `@XmlAccessorType(FIELD)`, `@XmlType(name = "RespuestaX", propOrder = {…})`, listas con `@XmlElementWrapper` + `@XmlElement`, e **inicializadas** a lista vacía.

Sin (1) se obtiene un híbrido peligroso: el envoltorio calificado pero los campos del tipo sin namespace.

### 3.2 .NET (CoreWCF sobre ASP.NET Core)
1. **Contrato:** `[ServiceContract(Name = "<Servicio>", Namespace = "<TNS>")]`; cada operación con `[OperationContract(Name = "<op>")]` y `[return: MessageParameter(Name = "respuesta")]`. Los **nombres de los parámetros C#** son los nombres XML (camelCase). La acción por defecto de WCF ya cumple R4.
2. **Implementación:** `[ServiceBehavior(Name = "<Servicio>", Namespace = "<TNS>", InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]`; las dependencias entran por constructor (singletons).
3. **Tipos:** `[DataContract(Name = "RespuestaX", Namespace = "<TNS>")]`; cada propiedad con `[DataMember(Name = "campo", Order = n)]` (base primero) y `EmitDefaultValue = false` en los opcionales. Listas: `[CollectionDataContract(Name = …, ItemName = "elemento", Namespace = "<TNS>")]` sobre una clase que hereda de `List<string>`, inicializada vacía.
4. **Binding y metadatos:** `new BasicHttpBinding { Name = "<Servicio>PortBinding", Namespace = "<TNS>" }` (sin `Namespace` el binding sale en `tempuri.org`); `AddServiceModelMetadata()` con `HttpGetEnabled = true` y `UseRequestHeadersForMetadataAddressBehavior` para que el WSDL anuncie el host con el que se consultó. HTTPS: segundo endpoint con `new BasicHttpBinding(BasicHttpSecurityMode.Transport)` (CoreWCF no tiene `BasicHttpsBinding`), solo si el host tiene una dirección HTTPS.

No usar `XmlSerializerFormat`, `MessageContract`, `Message` crudo ni un WSDL estático: se probaron para lograr "campos sin namespace" en .NET y no son idiomáticos ni necesarios si se aplica R3.

---

## 4. Recetas para el cliente

Orden de preferencia: **(a) generar** desde el WSDL, **(b) contrato a mano** con las mismas anotaciones del servidor, **(c) sobre XML a mano** (solo donde no hay librería, p. ej. Android).

| Cliente | Qué hacer |
|---|---|
| **(a) Generado** (`wsimport` / `jaxws-maven-plugin`, `dotnet-svcutil` / *WCF Web Service Reference*) | Guardar una copia del WSDL en el repositorio y generar desde ella. Fijar el endpoint por configuración (R9); no usar la URL fija del código generado. En Java, si el XSD trae `nillable`, usar un archivo de binding con `generateElementProperty="false"` para evitar `JAXBElement<String>`. |
| **(b) JAX-WS a mano** (Java) | Misma interfaz con `@WebService`, `@WebMethod(action=…)`, `@WebResult` y `@WebParam` con `targetNamespace`, y **el mismo `package-info.java`** en los DTO (§3.1). Crear el puerto con `Service.create(wsdlUrl, qnameServicio).getPort(Interfaz.class)` (**sin** `QName` de puerto) y fijar `ENDPOINT_ADDRESS_PROPERTY` con la URL completa (R9, R10). Timeouts con las propiedades del runtime. |
| **(b) WCF a mano** (.NET) | Mismo `[ServiceContract]`/`[DataContract]` del servidor, `BasicHttpBinding` y `EndpointAddress` desde configuración. |
| **(c) Sobre a mano** (Android, `HttpURLConnection`/`HttpClient`) | Petición: operación en el TNS con prefijo (`<ns:op xmlns:ns="<TNS>">`) y **cada parámetro con el mismo prefijo** (`<ns:usuario>`); cabecera `SOAPAction` con la URI de la operación (R4); `Content-Type: text/xml; charset=utf-8`. Respuesta: leer por **nombre local**, ignorando prefijos y namespaces; aceptar HTTP 200 y 500 (el 500 trae un Fault, que se trata como fallo de comunicación). URL en un recurso de configuración. HTTP sin cifrar: `network_security_config` con el host de desarrollo. |

Reglas comunes a todos los clientes:
- Tratar **por separado** fallo de red/protocolo (excepción) y fallo de negocio (`estado`/`codigo`).
- Campos opcionales y listas: pueden venir ausentes; no dar por hecho que existen.
- Enviar números con punto decimal e idioma invariante; UTF‑8 de extremo a extremo.
- No llamar al servicio en el hilo de la interfaz; fijar timeouts (~10 s).
- Un cambio de operaciones, parámetros o tipos obliga a regenerar o actualizar **todos** los clientes.

---

## 5. Diferencias aceptadas (no rompen la compatibilidad)

- Nombre del puerto y del binding (R10), y que el WSDL de .NET tenga el binding en un documento importado.
- `nillable="true"` en los `string` del XSD de .NET (mitigable con el archivo de binding de §4).
- Con HTTPS activo en CoreWCF, el WSDL declara **dos puertos** (http y https); los clientes que piden el puerto por interfaz funcionan igual.
- Precisión numérica propia del dominio (p. ej. `decimal` de .NET ≈ 28 dígitos frente a `BigDecimal` ilimitado de Java) y los **textos** de los mensajes: no son del protocolo, pero conviene acordarlos para que las respuestas sean idénticas (ver `RESUMEN_FUNCIONALIDADES.md`).
- Certificados de desarrollo de ASP.NET (HTTPS): no los reconoce Java sin importarlos; usar HTTP en laboratorio.

---

## 6. Verificación

**Antes de dar por bueno un servicio nuevo o un cambio de contrato:**
1. **Revisar el WSDL/XSD generado:** TNS único; `elementFormDefault="qualified"` (o `form="qualified"` por elemento); `soapAction` con URI por operación; una operación por caso de uso; tipos con el nombre esperado y campos en orden; elemento de retorno `respuesta`.
2. **Prueba de conformidad cruzada:** enviar las mismas peticiones (válidas, inválidas y casos límite) a ambos servidores y comparar, campo a campo, `estado/codigo/mensaje` y los campos propios (ignorando valores que cambian, como un token). En el servicio de referencia se usaron 86 peticiones.
3. **Un cliente real de cada tipo** contra cada servidor: JAX-WS, proxy generado con `dotnet-svcutil` (desde el WSDL de cada servidor) y el cliente Android.

**Resultado en el servicio de referencia (Payara ⇄ CoreWCF, 2026-10-16):**
- 86 peticiones al Payara desplegado y al servidor .NET: **84 respuestas idénticas** (también los textos de los mensajes); las 2 distintas son números de más de 28 dígitos.
- Cliente JAX-WS (consola, escritorio y web) y la lógica de red del cliente Android (probada contra Payara y .NET; la app se verificó en el emulador contra Payara): operan igual contra ambos servidores.
- Proxies `dotnet-svcutil` generados desde el WSDL de cada servidor: operan igual contra el servidor .NET y contra el motor Metro de Payara publicado de forma independiente; **no se probaron aún contra el Payara desplegado**.

---

## 7. Aplicarla a otro servicio o dominio

0. Partir de la plantilla de la plataforma: `soap_server_java` (Jakarta EE + JAX-WS) o `soap_server_dotnet` (CoreWCF); ambas llevan una copia de este documento y su servicio/estructura ya cumple la convención.
1. Elegir **TNS** y **nombre del servicio** (R2, R5).
2. Definir operaciones y parámetros (todo `string`, R7) y los tipos de respuesta que extienden `RespuestaBase` (R6).
3. Aplicar la receta de la plataforma (§3). La lógica de negocio va en capas sin dependencias de SOAP.
4. Publicar el WSDL, **guardar una copia** en el repositorio de los clientes y verificar (§6).
5. Documentar las operaciones y los códigos de error del dominio en su `RESUMEN_FUNCIONALIDADES.md`.

---

## 8. Estado en este proyecto (servicio "conversor de unidades")

| Componente | Estado de la convención |
|---|---|
| Servidor Java (Payara) | Aplicada (§3.1): `package-info.java` + `targetNamespace` y `action` en las 6 operaciones |
| Clientes Java: consola, escritorio, web | Aplicada (§4, JAX-WS a mano): mismas anotaciones, `package-info`, `getPort(Interfaz)`, `crearConEndpoint(url)`. **Pendiente (opcional):** que `crear()` lea una URL completa (`ws.url`); hoy toma una base y añade `/<Servicio>`, válido para Payara pero no para la ruta de .NET (R9) |
| Cliente móvil Android | Aplicada (§4 c): parámetros con prefijo, `SOAPAction` por operación, URL en `res/values/config.xml` |
| Servidor .NET (CoreWCF) | Aplicada con el código idiomático (§3.2), con los mismos textos de login que Java. Única diferencia conocida: números de más de ~28 dígitos |
| Clientes .NET (consola, escritorio, web, móvil) | Por construir; se generan desde el WSDL (§4 a) |
