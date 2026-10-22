# Convención REST de la familia de proyectos (cliente .NET)

Reglas que cumplen los servidores REST de la familia (Java/Jakarta EE y .NET/ASP.NET Core) y de las que depende la plantilla `rest_dotnet_consola`.
Si un servidor las cumple, su cliente cambia de servidor solo con la **URL base**. Es la contraparte de `CONVENCION_SOAP.md`; es independiente del dominio.

| # | Regla | Dónde la cumple el cliente |
|---|---|---|
| R1 | **URL base completa**, con prefijo (p. ej. `/api`) y «/» final. El cliente no asume host, puerto ni prefijo; las rutas se escriben relativas y sin barra inicial (`"auth/login"`) | `ConfiguracionApi` |
| R2 | **Verbos:** `GET` para consultas, `POST` para el login y las operaciones con cuerpo. Un verbo equivocado da `405` | `ServicioBase.EnviarAsync` (el servicio elige) |
| R3 | **JSON UTF-8:** `Content-Type: application/json; charset=utf-8` en las peticiones con cuerpo y `Accept: application/json` en todas. Otro `Content-Type` da `415` | `ClienteApi` (`JsonContent`, `CrearHttpClient`) |
| R4 | **Nombres fijos** de propiedades JSON (`camelCase`: `usuario`, `contrasenia`, `unidadOrigen`…). Se leen sin distinguir mayúsculas y se ignoran los campos desconocidos | `dto/*` con `[JsonPropertyName]`; `JsonSerializerDefaults.Web` |
| R5 | **Forma común de respuesta:** toda respuesta, de éxito o de error, es un objeto con `estado` (`OK`/`ERROR`), `codigo` y `mensaje`, más los campos de la operación. Éxito y error de una operación usan el mismo tipo; en error, los campos propios llegan `null` y las listas vacías o ausentes | `dto/RespuestaBaseDto` y los DTO que la extienden |
| R6 | **Errores de la plataforma con la misma estructura:** ruta inexistente (`404 ERR-404`), método no permitido (`405 ERR-405`), `Content-Type` incorrecto (`415 ERR-415`), JSON mal formado (`400 DATA-001`) y error interno (`500 ERR-500`) traen **solo** `estado`, `codigo` y `mensaje` | `ServicioBase` lee siempre la base |
| R7 | **Código HTTP + código propio.** Se decide primero por el HTTP y luego por el `codigo` (tabla abajo). **Nunca** `EnsureSuccessStatusCode()`: se perdería el JSON del error | `ServicioBase.EnviarAsync` |
| R8 | **Token en cabecera:** `Authorization: Bearer <token>` en toda operación protegida; el login es público. Se envía **por petición**, nunca en las cabeceras por defecto del `HttpClient`. Toda respuesta `401` trae `WWW-Authenticate: Bearer` | `ClienteApi`; `ServicioBase.ExigirToken` |
| R9 | **Todo dato viaja como texto**, también `valor` y `resultado`; los decimales con **punto**, sin coma ni notación científica. Un número JSON sin comillas no forma parte del contrato | `ServicioBase.NormalizarValor`; DTO con `string?` |
| R10 | **Rutas con variables codificadas:** el valor de un segmento se codifica con `Uri.EscapeDataString`; el servidor lo recibe ya decodificado y lo compara sin distinguir mayúsculas ni tildes. Las rutas de operación van en minúscula y sin tilde | servicio del dominio |
| R11 | **Sin respuesta no hay código:** servidor caído, red o tiempo agotado no son un error de la API. El cliente fija un tiempo límite (10 s) y lo traduce a «Servidor no disponible, intente más tarde» | `ClienteApi` → `ExcepcionServicio.ServidorNoDisponible` |
| R12 | **Cuerpo que no es JSON** (página de IIS Express, proxy, `502`): el cliente no deja escapar `JsonException`; muestra un mensaje genérico con el código HTTP | `ServicioBase.Leer` / `CrearExcepcion` |

## Códigos propios y HTTP

| Código propio | HTTP | Qué hace el cliente |
|---|---|---|
| `200` | `200` | Leer el DTO y mostrar el resultado |
| `AUTH-001` | `401` | Login: mostrar `mensaje` y volver a pedir credenciales |
| `AUTH-002` / `AUTH-003` | `401` | Token inválido / vencido: descartar el token y volver al login (`ExigeLogin`) |
| `AUTH-004` | `500` | Mostrar `mensaje`; se puede reintentar |
| `DATA-xxx`, `UNIT-001` | `400` | Datos o unidades inválidos: mostrar `mensaje` y volver al ingreso de datos |
| `CAT-001` | `404` | Recurso inexistente en la ruta: mostrar `mensaje` y volver a la selección |
| `ERR-404` / `ERR-405` / `ERR-415` | `404` / `405` / `415` | Error de programación del cliente (ruta, verbo o `Content-Type`): mostrar `mensaje`; revisar el código |
| `ERR-500` | `500` | Mostrar `mensaje`; se puede reintentar |

Los códigos concretos de cada dominio (`DATA-xxx`, `CAT-xxx`…) los define el servidor; los que importan a la plantilla son `AUTH-002` y `AUTH-003`.

## Diferencias con la convención SOAP

| SOAP | REST |
|---|---|
| Errores de negocio con HTTP 200 + `estado = ERROR` | Código HTTP según el error (R7); `estado` no es la única condición |
| Token como primer parámetro de la operación | Cabecera `Authorization: Bearer` (R8) |
| Proxy generado desde el WSDL (`dotnet-svcutil`) | DTO escritos a mano; sin generación de código |
| Fallo de red: `CommunicationException` / `TimeoutException` → `RED` | `HttpRequestException` / timeout → `ServidorNoDisponible` (R11) |
| Un cliente WCF por llamada | Un `HttpClient` de larga vida para todo el programa |
| Paquetes `System.ServiceModel.*` | Ninguno (`System.Net.Http.Json` viene en el SDK) |

## Lo que el código no puede imponer

Queda como acuerdo con el servidor: los nombres de rutas, campos y códigos (R4, R6), que los errores salgan siempre como JSON y no como HTML ni `ProblemDetails` (R5, R6), y que los tokens caduquen (el cliente reacciona al `401`, no cuenta el tiempo).
Si el servidor cambia su contrato, se actualizan `api`, `dto` y `servicio`; la vista y el controlador no se tocan.
