# CONVENCION_PRUEBAS — Convención de pruebas (servidores y clientes)

Define **qué se prueba, qué no y con cuánto detalle**, para servidores y clientes, sin importar el lenguaje, la tecnología ni el protocolo (SOAP o REST; Java o .NET). Los detalles de cada plataforma (JUnit/Mockito, xUnit/Moq, pruebas manuales en `ec.edu.monster.pruebas`) viven en `ARQUITECTURA_SERVIDOR.md` §10, `ARQUITECTURA_CLIENTE.md` §10 y `JAVA_TESTS_CONVENTIONS.md`; este documento fija el criterio común.

**Versión 1** (2026-10-26).

---

## 1. Principios

1. **Solo pruebas unitarias.** Cada prueba verifica una **operación atómica** (un cálculo, una conversión, una validación, un mapeo) o una **operación de negocio** (lo que hace un servicio o un controlador ante una entrada, incluidos sus errores esperados).
2. **Ejecutables con todo apagado.** Para correrlas no se necesita base de datos, servidor desplegado, red ni archivos externos. Lo que está fuera del componente se reemplaza por un doble.
3. **Simples y directas.** Una idea por prueba, con el menor montaje posible. El objetivo es evitar el **sobredimensionamiento** (más pruebas o más infraestructura de la que el riesgo justifica) y el **gasto de recursos** al ejecutarlas (arranque de servidores, puertos, esperas).
4. **Se prueba el comportamiento, no la forma de implementarlo.** Una prueba falla porque una regla se rompió, no porque se renombró un método privado.
5. **Lo que no es unitario se valida a mano**, siguiendo una lista explícita (§6), no con código de prueba.

## 2. Qué se prueba

| Plataforma | Componente | Qué se verifica |
|---|---|---|
| Servidor | Cálculos, conversiones y mapeadores | Entrada → salida, incluida la conversión a texto y el armado de la envoltura |
| Servidor | Catálogo de errores | Cada código tiene mensaje y categoría |
| Servidor | Validación de forma y controlador base | Entradas inválidas rechazadas; toda excepción no controlada produce `ERR-500` sin detalles internos |
| Servidor | Controladores | Convierten, delegan al servicio y un dato inválido **no llega** al servicio (servicio doble) |
| Servidor | Servicios | Qué se pide a los repositorios, en qué orden, confirmación, reversión y cierre de la conexión (repositorios dobles) |
| Cliente | `ServicioBase` y servicios | Verificación de la envoltura (OK/ERROR/nula), validación y conversión a texto, uso de las credenciales de la sesión (cliente doble) |
| Cliente | Capa `ws`/`api` | Traducción de fallos de red a `RED` y de respuestas ilegibles a `SRV-000`; resolución de la URL configurada |
| Cliente | Controladores | Invariantes del molde: valida vacíos antes de llamar, usa solo servicios, devuelve el resultado correcto, muestra el mensaje y no el código (servicio y vista dobles) |

## 3. Qué no se prueba con código

| Fuera de alcance | Cómo se valida |
|---|---|
| Repositorios (SQL real, columnas, conexión) | Lista manual con la base encendida (crear, leer, actualizar, eliminar y cada cálculo) |
| Mensajes en el cable y contrato (WSDL/OpenAPI, namespaces, `SOAPAction`, HTTP, JSON) | **Verificación manual del contrato** (§6) |
| Interfaz: vistas, distribución de pantallas, navegación y bucle principal | Manual, durante el desarrollo, según el plan de construcción |
| Integración entre componentes y flujos extremo a extremo | Manual, contra el servidor desplegado (SoapUI, Postman o el cliente) |
| Rendimiento, concurrencia, seguridad de transporte | Fuera del curso |

Consecuencia: **no se escriben pruebas que publiquen un servicio o un servidor falso** ni que abran un puerto, aunque sea local.

## 4. Cuánto probar

No hay una métrica mínima ni máxima. El criterio es proporcional al riesgo:

1. **Una prueba por regla.** Cada regla de negocio, validación o conversión relevante tiene al menos una prueba que falla si se rompe. Si dos pruebas verifican lo mismo, se deja una.
2. **Camino feliz y un camino de error.** Todo componente con lógica propia tiene, como mínimo, el caso que sale bien y el error que puede producir. Se agregan más solo por **reglas propias** (una frontera numérica, un orden, una reversión), no por cobertura de líneas.
3. **Componentes sin lógica propia** (delegación pura, getters, clases generadas, vistas) no se prueban.
4. **Controladores del cliente:** son delgados; basta con verificar su invariante más importante y el manejo de un error. Se amplía solo si el controlador toma una decisión que ninguna otra capa cubre.
5. **Comprobar que las pruebas detectan defectos:** en las reglas críticas del servidor (cálculos, orden, reversión), introducir a propósito el defecto, verificar que alguna prueba falla y restaurar el código. No se exige en el cliente.

## 5. Reglas de escritura

- El nombre describe el comportamiento (`depositarRechazaImporteConMasDeDosDecimales`), no el método.
- Disposición *preparar → ejecutar → verificar*; pocas aserciones, todas del mismo comportamiento.
- Independientes entre sí y sin orden; sin `sleep`, sin reloj ni aleatoriedad reales, sin archivos, sin red.
- Dobles: escritos a mano o con la librería de la plataforma (Mockito, Moq). Se prefiere lo que ya usa el proyecto; no se agrega una dependencia solo para las pruebas de un componente.
- La misma estructura de paquetes que el código bajo prueba.
- Los datos de prueba son ficticios y suficientes para la regla; no se leen de la base de datos.
- **Pruebas manuales en Java** (`ec.edu.monster.pruebas`): réplicas de JUnit, sin dependencias adicionales, según `JAVA_TESTS_CONVENTIONS.md`. Son una exigencia del curso, no una forma de prueba adicional.

## 6. Verificación manual del contrato

Reemplaza a las pruebas automáticas de conformidad. Se hace **con el servidor encendido** y la herramienta que el equipo ya usa (SoapUI, Postman, `curl` o el propio cliente). La hace quien construye o cambia el servicio o el cliente; el resultado se registra en el `PROJECT.md` del proyecto con fecha.

**Cuándo:** al crear un servicio, al cambiar un contrato (operación, campo, código de error), al migrar una variante (otro lenguaje o protocolo) y antes de entregar.

### 6.1 Servidor SOAP

| # | Qué hacer | Qué debe verse | Regla |
|---|---|---|---|
| 1 | Abrir `<url>?wsdl` y su `.xsd` | Estilo `document/literal`; un solo TNS `http://ws.monster.edu.ec/`; `elementFormDefault="qualified"`; `soapAction` con URI por operación; elemento de retorno `respuesta`; tipos con nombre y campos en el orden esperado | R1–R5 |
| 2 | Importar el WSDL en la herramienta | Se crean las operaciones esperadas, ni más ni menos, con sus parámetros en texto | R5, R7 |
| 3 | Por **cada operación**, enviar una petición válida | HTTP 200; `estado=OK`, `codigo=200`, mensaje fijo; campos propios presentes y calificados con el TNS | R3, R6 |
| 4 | Verificar el formato de los datos de esa respuesta | Números con punto decimal y dos decimales; fechas ISO; opcionales ausentes cuando no tienen valor; ningún campo propio llamado `estado`, `codigo` o `mensaje` | R7 |
| 5 | En operaciones con lista, enviar un caso con resultados y otro sin | Contenedor con el elemento repetido; sin resultados llega **vacío**, no ausente | R7 |
| 6 | Por **cada código del catálogo**, provocar el error (cuenta inexistente, credenciales inválidas, etc.) | HTTP 200; `estado=ERROR`; `codigo` y `mensaje` del catálogo; **sin SOAP Fault**; campos propios omitidos y lista vacía | R6, R8 |
| 7 | Enviar un dato con forma inválida (vacío, no numérico, con signo, demasiados decimales) | HTTP 200 y `estado=ERROR` con el código de datos; el servicio no llegó a ejecutar | R8 |
| 8 | Enviar XML mal formado y luego una operación inexistente | SOAP Fault generado por la plataforma, sin trazas internas | R8 |
| 9 | Provocar un fallo interno (por ejemplo, base apagada) | `ERR-500` con mensaje genérico; sin texto de la excepción ni de SQL | R8 |
| 10 | Repetir 3 y 6 de **una** operación desde un cliente real de otra plataforma, si existe | Misma respuesta que en la herramienta | R3, R4 |

### 6.2 Servidor REST

| # | Qué hacer | Qué debe verse |
|---|---|---|
| 1 | Por cada fila de `ENDPOINTS_REST.md`, llamar con verbo, ruta y cuerpo válidos | El código HTTP de éxito de la fila; JSON con nombres fijos y la envoltura |
| 2 | Por cada fila, provocar al menos un error de negocio | El código HTTP de su categoría; envoltura con `estado=ERROR`, `codigo` y `mensaje` |
| 3 | Llamar a una ruta inexistente, con verbo no permitido y con JSON mal formado | Código HTTP de la plataforma **con la misma envoltura**, sin trazas internas |
| 4 | Provocar un fallo interno | `ERR-500` genérico |
| 5 | En una operación con cuerpo, repetir una petición válida con un valor numérico **sin comillas** (`"importe": 100.50`) | La misma respuesta que con el valor como texto (R9); un valor como `1E+2` se rechaza igual, con `DATA-001` |

### 6.3 Cliente

Con el servidor encendido y datos de prueba conocidos:

| # | Qué hacer | Qué debe verse |
|---|---|---|
| 1 | Comparar el WSDL/OpenAPI de la copia en recursos con el del servidor (`?wsdl`) | Sin diferencias; si las hay, regenerar el proxy desde la copia actualizada |
| 2 | Por **cada operación**, ejecutarla desde el cliente con datos válidos | Los datos de la respuesta llegan completos (ningún campo vacío o `null`) y se muestran con el formato de la especificación |
| 3 | Por cada código del catálogo que la operación puede producir, provocarlo | Se muestra **el mensaje** del servidor, nunca el código; la aplicación sigue operando |
| 4 | Enviar un dato vacío o con forma inválida | El cliente lo rechaza antes de llamar (vacío) o muestra el mensaje del servidor (forma) |
| 5 | Operar con credenciales inválidas durante una sesión | Se informa que la sesión ya no es válida y se vuelve al inicio |
| 6 | Apagar el servidor y repetir una operación | «Servidor no disponible, intente más tarde», sin trazas ni cierre inesperado |
| 7 | Apuntar la URL a un servicio HTTP que no sea el del proyecto | «No se pudo interpretar la respuesta del servidor» |
| 8 | Cambiar la URL por propiedad o variable de entorno | El cliente usa la nueva sin recompilar |

Para operaciones que **modifican datos** se anotan los saldos o valores antes y después, y se comprueban contra lo esperado; si la prueba no debe alterar los datos de la base, se limita a lecturas y a peticiones que el servidor rechaza.

### 6.4 Registro

Cada paso se marca como hecho en el `PROJECT.md` con la fecha y, si falló, el defecto encontrado. Los pasos específicos del dominio (cuentas, importes y resultados esperados) se concretan en la documentación del dominio, no aquí.

## 7. Lista de comprobación antes de dar por terminadas las pruebas

- [ ] Todas las pruebas corren con base de datos, servidor y red apagados
- [ ] No hay pruebas que publiquen un servicio, abran un puerto o lean archivos externos
- [ ] Cada regla de negocio y cada código de error que el componente produce tiene su prueba
- [ ] No hay pruebas de vistas, de navegación ni de integración
- [ ] En el servidor, las reglas críticas se comprobaron con un defecto introducido a propósito
- [ ] La verificación manual del contrato (§6) está hecha y registrada en `PROJECT.md`
- [ ] En Java, las pruebas manuales de `ec.edu.monster.pruebas` siguen `JAVA_TESTS_CONVENTIONS.md`

## 8. Transición

Antes de esta convención se escribieron **pruebas automáticas de conformidad** (`ConvencionSoapTest` en el servidor, `ConvencionClienteTest` y su `WSEjemplo` en el cliente y en las plantillas). Quedan **fuera de alcance**: no se amplían, no se crean nuevas y se retiran de cada proyecto cuando se decida, junto con el WSDL de ejemplo y la ejecución `generar-proxy-ejemplo` del `pom.xml`. Mientras permanezcan, no cuentan como parte de las pruebas exigidas.

## 9. Decisiones

| Tema | Decisión |
|---|---|
| Tipo de prueba automática | Solo unitarias, sobre operaciones atómicas y de negocio |
| Dependencias externas | Ninguna: sin base de datos, servidor desplegado ni red |
| Contrato y protocolo | Verificación manual guiada (§6), no automática |
| Interfaz, navegación, integración y e2e | Fuera de las pruebas automáticas |
| Cantidad de pruebas | Sin métrica; proporcional a las reglas y a los errores del componente |
| Dobles | Los de la plataforma o escritos a mano, sin dependencias nuevas |
