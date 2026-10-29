# ENDPOINTS_REST — Contrato REST del proyecto (MOLDE con el ejemplo `Elemento`)

> **Plantilla.** Este archivo muestra la estructura y el nivel de detalle que debe tener el contrato REST de un proyecto. Las filas, los campos y los códigos son del **ejemplo** (`Elemento`): se sustituyen por los del dominio **antes de implementar**. Reglas generales: `CONVENCION_REST.md` (R1 a R12). Qué hace el sistema, los formatos y el catálogo de errores del dominio: la especificación del servidor del proyecto (`RESUMEN_FUNCIONALIDADES.md`).
> Cada cliente REST guarda una **copia** de este archivo como contrato, igual que los clientes SOAP guardan el WSDL. Cambiar una fila obliga a actualizar el servidor y todos los clientes.

---

## 1. Contexto y reglas heredadas

| Dato | Valor |
|---|---|
| Contexto de la plataforma | *Context root* del WAR (Java), p. ej. `/rest_java_server` |
| Prefijo común (R1) | `api`, **siempre inmediatamente después del contexto** |
| URL base completa (R1) | `http://<host>:<puerto>/<contexto>/api/` (con «/» final). Las rutas de la tabla son relativas y sin barra inicial; cada cliente guarda la URL base en un solo lugar |
| Base de datos | *(la del proyecto)* |
| Formato de los datos | *(los de la especificación del servidor: longitudes, decimales, formatos de fecha…)* |
| Todo dato como texto (R9) | Las respuestas nunca llevan números JSON. En las **peticiones**, el servidor aplica la regla general de R9 (`CONVENCION_REST.md`): acepta un número JSON como valor de campo (`"importe": 100.50`) y lo trata como su texto (`"100.50"`); el validador decide. La plantilla ya lo hace (`CuerpoJson`). El cliente sigue enviando texto y no necesita ningún cuidado especial |
| CORS | No se añade, salvo que un cliente web llame a la API desde el navegador |

**Orden de validación en una operación** (el primer fallo es el que se informa): 1) forma de los datos (`DATA-001`), también de las variables de ruta; 2) credenciales, si el proyecto las tiene (`AUTH-…`); 3) existencia y reglas de negocio. Un cuerpo vacío, mal formado o que no es un objeto JSON da `400` `DATA-001` («Cuerpo de la petición inválido»).

## 2. Autenticación (R8)

*(Obligatoria en el contrato de cada proyecto; la plantilla no la implementa.)* Especificar:

- Cómo viajan las credenciales o el token (cuerpo, cabecera `Authorization`…), **por qué operaciones** y nunca en la URL.
- Qué responde el servidor ante credenciales **ausentes o mal formadas** y ante credenciales **inválidas** (`401` con la envoltura y, si el mecanismo lo exige, la cabecera `WWW-Authenticate`).
- Qué no se registra (ni cabeceras ni cuerpos).
- Qué hace el cliente ante un `401`.
- Los límites aceptados del mecanismo elegido.

## 3. Tabla de endpoints

Un método de la interfaz del cliente por fila. Rutas relativas a la URL base; los segmentos `{…}` se codifican (R10). Todo valor de entrada y salida es texto (R9). **Todo éxito responde HTTP 200**, también al crear.

| Operación | Verbo | Ruta | Credenciales | Cuerpo | Respuesta (campos propios, además de la envoltura) | Éxito | Errores posibles |
|---|---|---|---|---|---|---|---|
| `listarElementos` | `GET` | `elementos` | *(según §2)* | — | `RespuestaElementos`: lista `elementos` de `elemento` (`codigoElemento`, `nombre`, `valor`) | 200 | — |
| `consultarElemento` | `GET` | `elementos/{codigoElemento}` | *(según §2)* | — | `RespuestaElemento`: `codigoElemento`, `nombre`, `valor` | 200 | `DATA-001` 400, `ELE-001` 404 |
| `registrarElemento` | `POST` | `elementos` | *(según §2)* | `codigoElemento`, `nombre`, `valor` | `RespuestaElemento`: los mismos campos | 200 | `DATA-001` 400, `ELE-002` 409 |

Además, **cualquier fila** puede devolver los errores de plataforma de `CONVENCION_REST.md` §2.1 (`ERR-404`, `ERR-405`, `ERR-415`, `DATA-001` por JSON mal formado) y `ERR-500` 500.

Notas de la tabla:
- Las operaciones de negocio que crean algo son **subrecursos** o `POST` sobre el recurso en plural (R10); aun así responden `200`.
- La variable de ruta se valida como el parámetro equivalente: una ruta con un código de forma inválida responde `400` `DATA-001`, no `404`.
- Las listas llegan **vacías, nunca ausentes**, también en los errores. En un error, los campos propios se **omiten** (R5).

### 3.1 Ejemplos de cuerpos

Petición de `registrarElemento` (`POST elementos`):
```json
{ "codigoElemento": "0004", "nombre": "Cuarto", "valor": "5.00" }
```
Respuesta de éxito (HTTP 200):
```json
{ "estado": "OK", "codigo": "200", "mensaje": "Operación exitosa",
  "codigoElemento": "0004", "nombre": "Cuarto", "valor": "5.00" }
```
Respuesta de error (HTTP 409), solo la envoltura:
```json
{ "estado": "ERROR", "codigo": "ELE-002", "mensaje": "El elemento ya existe" }
```
Respuesta de error de una consulta con lista (HTTP 500): la lista llega vacía.
```json
{ "estado": "ERROR", "codigo": "ERR-500", "mensaje": "Error interno del servidor", "elementos": [] }
```

## 4. Códigos HTTP por categoría

El HTTP de error sale de la categoría de la entrada del catálogo (`ARQUITECTURA_SERVIDOR.md` §4.3), no de cada código. Catálogo de la plantilla (las filas `ELE-…` son del ejemplo; el resto es núcleo):

| Código | Mensaje por defecto | Categoría | HTTP |
|---|---|---|---|
| `200` | Operación exitosa | — | 200 |
| `DATA-001` | Dato de entrada inválido (mensaje específico del campo) | Validación | 400 |
| `ELE-001` | El elemento no existe | No encontrado | 404 |
| `ELE-002` | El elemento ya existe | Conflicto | 409 |
| `ERR-404` | Ruta no encontrada | No encontrado | 404 |
| `ERR-405` | Método no permitido | Método no permitido | 405 |
| `ERR-415` | Tipo de contenido no soportado | Tipo no soportado | 415 |
| `ERR-500` | Error interno del servidor | Técnico | 500 |

Un proyecto con autenticación agrega sus códigos `AUTH-…` (categoría *No autorizado*, 401) y los de su dominio. Las rarezas (un error cuya categoría da un HTTP distinto del recurso de la ruta) se anotan aquí.

## 5. Decisiones

| # | Tema | Decisión | Estado |
|---|---|---|---|
| | *(una fila por decisión del proyecto: autenticación, credenciales ausentes, CORS, forma de los tipos JSON…)* | | |
