# rest_dotnet_server — Plantilla de servidor REST en .NET (ASP.NET Core 10, SQL Server)

Plantilla para crear el **servidor** de un SOA .NET REST: **C# 14 + ASP.NET Core 10** (API mínima con `System.Text.Json`) ejecutado con Kestrel o IIS Express desde Visual Studio 2026, con persistencia en **SQL Server** por ADO.NET (`Microsoft.Data.SqlClient`). Sigue `ARQUITECTURA_SERVIDOR.md` (estructura del código) y `CONVENCION_REST.md` (mensaje en el cable), ambas copiadas en `docs/`; **si discrepan con este README, mandan `context/arquitectura/`**.

**No contiene nada del dominio.** Trae un **núcleo** que se copia sin cambios y un **ejemplo mínimo** (la entidad genérica `Elemento`) que se borra o se sustituye. Es la contraparte de `soap_dotnet_server` y de `rest_java_server`: el núcleo es el de `soap_dotnet_server` y solo cambia la capa de protocolo (`ws` ↔ `api`).

> **Estado:** `dotnet test` pasa sus **145 pruebas** xUnit (Moq para las dependencias; sin servidor ni base de datos) y `dotnet build` compila sin errores ni advertencias. La capa `api` es la del servidor .NET REST construido a partir de `soap_dotnet_server`, cuyos casos C01–C29 se ejecutaron a mano contra el servidor en marcha. *(No verificado):* **esta plantilla** no se ha levantado como servidor, y el script `docs/bdd/Crea_BD.sql` (ejemplo) no se ha ejecutado.

## 1. Qué contiene

```
rest_dotnet_server/
├── rest_server_dotnet.slnx / .csproj    net10.0, Sdk.Web, SqlClient, xUnit + Moq, GenerateProgramFile=false (sin CoreWCF)
├── Program.cs                           contenedor de dependencias + ManejadorErrores + Rutas
├── Properties/launchSettings.json       perfiles http (5267) e IIS Express (57592 / 44380)
├── docs/                                CONVENCION_REST.md y ARQUITECTURA_SERVIDOR.md (copias de context/arquitectura)
│   ├── ENDPOINTS_REST.md                molde de la tabla de endpoints del proyecto (con el ejemplo)
│   └── bdd/Crea_BD.sql                  base y tabla de ejemplo + usuario de la aplicación (fuera del código)
└── ec/edu/monster/
    ├── NÚCLEO (no depende del dominio; se copia sin cambios)
    │   ├── dto/Resultado, EstadoResultado       estado/código/mensaje/datos neutral que devuelve todo controlador
    │   ├── excepciones/ExcepcionMonster         única excepción de negocio
    │   ├── excepciones/CodigoError              catálogo: DATA-001, ERR-404/405/415 y ERR-500 son núcleo; se agregan los del dominio
    │   ├── controlador/ControladorBase          único punto que captura excepciones (→ Resultado; ERR-500 sin detalles)
    │   ├── controlador/ValidadorForma           reglas genéricas de forma (obligatorio, longitud, dígitos, decimal)
    │   ├── db/AccesoDB                          conexiones; solo cambian sus constantes (servidor, base, usuario, clave)
    │   ├── db/ComandoSqlServer                  crea comandos con su transacción y parámetros tipados (interno)
    │   ├── servicios/Transaccional              patrón único de conexión y transacción (interno)
    │   ├── api/ConversorTexto                   tipos reales → texto del contrato (R9)
    │   ├── api/CodigoHttp                       HTTP por categoría del error (R7)
    │   ├── api/CuerpoPeticion                   exige Content-Type JSON (ERR-415) y entrega el cuerpo como texto
    │   ├── api/CuerpoJson                       lee el cuerpo: nombres sin distinguir mayúsculas (R4), número JSON como texto (R9)
    │   ├── api/RespuestasHttp                   IResult: HTTP por categoría + JSON UTF-8, sin nulos (R3, R5, R7)
    │   ├── api/ManejadorErrores                 middleware de errores de plataforma con la envoltura (R6)
    │   ├── api/contrato/RespuestaBase           envoltura estado/codigo/mensaje (R5)
    │   ├── api/contrato/ConvencionRest          prefijo, tipo de contenido y mensajes fijos
    │   └── pruebas/ConexionFalsa                dobles de DbConnection/DbTransaction para las pruebas de servicios
    └── EJEMPLO (sustituir por el dominio)
        ├── modelo/Elemento · db/IElementoRepositorio + ElementoRepositorioSqlServer · servicios/ElementoServicio
        ├── controlador/ElementoControlador
        ├── api/ElementoRecurso · api/ElementoMapeador · api/Rutas
        ├── api/contrato/Respuesta{Elemento,Elementos}, ElementoDetalle
        └── pruebas/Elemento{Servicio,Controlador,Recurso,Mapeador}Pruebas, RespuestasHttpPruebas (más las pruebas del núcleo)
```

Los archivos `.cs` van en PascalCase; los directorios y espacios de nombres, en minúscula (`namespace ec.edu.monster.servicios;`, **explícito**, porque Visual Studio no lo genera desde la ruta). Dependencias solo hacia abajo: `api → controlador → servicios → db`; `modelo`, `dto` y `excepciones` son transversales. Nada fuera de `api` conoce REST ni HTTP.

## 2. Crear un proyecto a partir de la plantilla

1. Copiar la carpeta con el nombre nuevo, sin `bin/`, `obj/` ni `.vs/`.
2. Renombrar `rest_server_dotnet.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`.
3. `Properties/launchSettings.json`: elegir **puertos propios** si hay otro servidor abierto a la vez (`applicationUrl` del perfil `http`; `applicationUrl` y `sslPort` de `iisSettings`) y cambiar el `launchUrl` por una ruta del contrato. La URL base queda `http://<host>:<puerto>/api/` (R1).
4. `AccesoDB`: servidor, base, usuario y clave. `docs/bdd/Crea_BD.sql`: reemplazar por el script del proyecto (SOAP y REST de una plataforma comparten el mismo).
5. **Escribir `docs/ENDPOINTS_REST.md` antes de implementar** (tabla de endpoints y autenticación del proyecto; el molde trae el ejemplo).
6. Borrar el ejemplo (`Elemento*`, `Respuesta{Elemento,Elementos}`, `ElementoDetalle`, `ELE_*` de `CodigoError`, las filas de `Rutas`, `Program.cs` y las pruebas `Elemento*Pruebas` y `RespuestasHttpPruebas`) y crear las piezas del dominio con la receta de §3, usando el ejemplo como molde. Nombres: entidad primero y tipo después (`CuentaServicio`, `CuentaRecurso`, `ICuentaRepositorio`…; `ARQUITECTURA_SERVIDOR.md` §3.1).
7. Mantener `dotnet test` en verde. Las pruebas del núcleo que citan `ELE_*` (`ControladorBasePruebas`, `ExcepcionMonsterPruebas`, `CodigoErrorPruebas`, `CodigoHttpPruebas`) se cambian por una entrada del dominio.

## 3. Añadir una operación (receta)

1. **Contrato** (`api/contrato`): clase que extiende `RespuestaBase`, con `[JsonPropertyName("…"), JsonPropertyOrder(n)]` en cada propiedad (`n` desde 1: la envoltura usa órdenes negativos y va primero), todo `string?` con `{ get; private set; }`. Listas: `List<T>` inicializada vacía. Un campo propio nunca se llama `estado`, `codigo` ni `mensaje`. No usar `record` para las respuestas: no pueden heredar la envoltura.
2. **Errores:** agregar a `CodigoError` los códigos nuevos (`PREFIJO-NNN`, con categoría) y a su lista `Todos`. Nunca se reutiliza un código. El HTTP sale de la categoría (`CodigoHttp`): no hay que tocarlo.
3. **Repositorio** (`db`): operación en la interfaz `I…Repositorio` y en la implementación `…SqlServer` (SQL como constantes, parámetros con nombre y tipo, recibe la `DbTransaction`, nunca abre ni cierra la conexión).
4. **Servicio:** lógica con `Transaccional.Ejecutar(...)`; cálculos como métodos estáticos puros; lanza `ExcepcionMonster`. Las operaciones públicas son `virtual` (Moq).
5. **Controlador:** valida la forma con `ValidadorForma`, convierte a tipos reales y `return Ejecutar(() => _servicio.X(…));`
6. **Mapeador, recurso y ruta:** el mapeador pasa `Resultado` → contrato con `ConversorTexto`. En el recurso (`…Recurso`, una sola clase por recurso en plural): cada método recibe los parámetros como texto, delega y devuelve `RespuestasHttp.De(…)`; los que traen cuerpo hacen `CuerpoJson.Leer(await CuerpoPeticion.LeerTexto(peticion))`. En `Rutas.cs`: una línea `MapGet`/`MapPost` por fila de `ENDPOINTS_REST.md`, con los parámetros de ruta como `string` (los valida el controlador, no el enrutador). **Todo éxito responde 200**, también al crear.
7. **Registro en `Program.cs`:** repositorio, servicio, controlador y recurso en el contenedor (`AddSingleton`).
8. **Pruebas:** servicio (Moq + `ConexionFalsa`), controlador, mapeador, recurso (con un `HttpRequest` en memoria) y la forma exacta del JSON del tipo nuevo (`RespuestasHttpPruebas`). El contrato REST (verbos, rutas, HTTP, un error por fila) se verifica **a mano** contra `docs/ENDPOINTS_REST.md` (`CONVENCION_PRUEBAS.md` §6.2).

## 4. La convención aplicada

| Regla | Dónde está | Por qué |
|---|---|---|
| **R1** prefijo y URL base | `ConvencionRest.Prefijo` + `MapGroup` en `Rutas` | el cliente guarda la URL base en un solo lugar |
| **R3** JSON UTF-8 | `ConvencionRest.JsonUtf8` en `RespuestasHttp` | única codificación del contrato |
| **R4** nombres fijos, sin distinguir mayúsculas al leer | `JsonPropertyName` (salida); `CuerpoJson` (entrada) | el cuerpo se lee con `JsonDocument` para controlar el error y los nombres |
| **R5** respuesta uniforme | `RespuestaBase` + `Resultado` + mapeadores | éxito y error con el mismo tipo; nulos omitidos; listas vacías, nunca ausentes |
| **R6** errores de plataforma con envoltura | `ManejadorErrores` (middleware, el primero de la canalización) | sin él, ASP.NET Core responde vacío (404/405/415) o con página/`ProblemDetails` |
| **R7** HTTP por categoría | `CodigoHttp` + `RespuestasHttp` | agregar un error solo exige una entrada en el catálogo |
| **R8** credenciales fuera de la URL y de los registros | **no se implementa** (ver §7) | cada proyecto define su mecanismo; el manejador no registra cabeceras ni cuerpos |
| **R9** datos como texto; el servidor tolera un número JSON recibido | `ConversorTexto` (salida); `CuerpoJson.Texto` (entrada: `"valor": 5.5` llega como `"5.5"` y el validador decide) | sin diferencias de cultura ni de tipos entre plataformas |

## 5. Ejecutar

```powershell
dotnet test                                           # sin servidor ni base de datos (la lógica se simula con Moq)
dotnet build
dotnet run --launch-profile http                      # o F5 en Visual Studio con el perfil elegido
```

**Base de datos:** SQL Server local en Docker (`context/entornos/ENTORNO_DOTNET.md` §2.2). Ejecutar `docs/bdd/Crea_BD.sql` (crea `ejemplo`, la tabla y el login `ejemplo`/`admin` con permisos solo sobre ella, no `sa`). `Crea_BD.sql` usa lotes `GO`: con `sqlcmd -C -f 65001 -i`, o *Execute script* en DBeaver.

| Qué | Petición (perfil `http`) |
|---|---|
| Listar | `GET http://localhost:5267/api/elementos` |
| Consultar | `GET http://localhost:5267/api/elementos/0001` |
| Registrar | `POST http://localhost:5267/api/elementos` con `Content-Type: application/json` y cuerpo `{"codigoElemento":"0004","nombre":"Cuarto","valor":"5.00"}` |
| Perfil IIS Express | el mismo con el puerto `57592` |

**Acceso desde emulador o teléfono, IIS Express y firewall:** `context/entornos/IIS_CONFIG.md`. Cambiar de perfil cambia la URL, y cada cliente la guarda en un solo lugar. **Tras cambiar el contrato hay que actualizar `docs/ENDPOINTS_REST.md`, reiniciar el servidor y actualizar todos los clientes** (`api` y `dto` de cada uno). La verificación manual del contrato está en `CONVENCION_PRUEBAS.md` §6.2.

## 6. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| Una ruta inexistente o un verbo equivocado responde vacío, con HTML o con `ProblemDetails` | `ManejadorErrores` no está registrado, o no es el primero | `app.UseMiddleware<ManejadorErrores>()` antes de `Rutas.Mapear`, y sin `UseDeveloperExceptionPage`/`UseExceptionHandler` por delante |
| Los campos salen en otro orden (`codigo` antes que `estado`) | Falta `JsonPropertyOrder` en la subclase, o se usó `order` 0 | Numerar los campos propios desde 1; la envoltura ya usa -3, -2 y -1 |
| Una respuesta de error trae `null` en los campos propios | Se serializó fuera de `RespuestasHttp` o con otras opciones | Devolver siempre `RespuestasHttp.De(…)`; `Opciones` omite los nulos |
| Una respuesta de error pierde los campos de la subclase o sale solo con la envoltura de éxito | Se serializó con el tipo de la base (`JsonSerializer.Serialize<RespuestaBase>`) | `RespuestasHttp.Json()` serializa con el tipo real |
| Las tildes salen como `ó` | Se serializó sin el codificador relajado | Usar `RespuestasHttp.Opciones` |
| `415` con un `POST` válido | Falta `Content-Type: application/json` en la petición | Enviarlo; es la regla (`ERR-415`) |
| Un campo numérico (`"valor": 5.5`) falla con `DATA-001` | Se leyó el cuerpo con un POCO o con `GetString()` | Leer siempre con `CuerpoJson.Texto` (R9) |
| Un `404` de la API con una ruta que parece correcta | El parámetro de ruta lleva una restricción (`{id:int}`) que no coincide | Declararlos sin restricción y validar en el controlador (`DATA-001` 400, no 404) |
| `InvalidOperationException` al llamar a una ruta (recurso no resuelto) | El recurso no está en el contenedor | `builder.Services.AddSingleton<…Recurso>()` |
| `ERR-500` en cada llamada | La base no existe, está apagada o las constantes de `AccesoDB` no coinciden | Ejecutar el script, revisar servidor/base/usuario/clave; el detalle está en la consola del servidor |
| Avisos `NU1902`/`NU1903` | Dependencia transitiva `System.Security.Cryptography.Pkcs` | Ya está fijada en 10.0.14 en el `.csproj` |
| `MSB3027` / `MSB3021`: no se puede copiar el `.dll` | El servidor sigue en ejecución | Detenerlo antes de compilar o compilar con `-p:OutDir=<otra carpeta>` |
| Más problemas de entorno | | `context/entornos/ENTORNO_DOTNET.md` §5 |

## 7. Alcance

- **Autenticación y autorización quedan fuera de la plantilla base** (cada proyecto las define en su `docs/ENDPOINTS_REST.md`; R8 de la convención solo fija que las credenciales no viajan en la URL ni se registran). La categoría `NoAutorizado` y su HTTP 401 ya existen en el catálogo. Al agregarla: leer las credenciales en `api` (nunca en el núcleo), pasarlas al controlador como texto y completar `RespuestasHttp` con lo que el mecanismo exija en un 401 (p. ej. `WWW-Authenticate`).
- **CORS:** no se añade (solo lo aplican los navegadores; ver la decisión del contrato del proyecto).
- **`OPTIONS` y `HEAD`:** los atiende el framework; `ManejadorErrores` solo envuelve las respuestas de error vacías. **(No verificado)** qué devuelve Kestrel.
- El servidor no guarda estado entre llamadas. Sin pool de conexiones propio y sin archivos de configuración de la aplicación (las constantes de `AccesoDB` están en el código), por tratarse de entornos locales académicos. HTTPS no viene configurado.
- Las rutas de ASP.NET Core no distinguen mayúsculas; las de JAX-RS (Java) sí. Diferencia de plataforma sin efecto en el contrato.

## 8. Entorno de referencia

Windows 11, Visual Studio Community 2026, SDK .NET 10, `Microsoft.Data.SqlClient` 7.1.1, xUnit 2.9.3, Moq 4.21.0, SQL Server 2025 en Docker (ver `context/entornos/ENTORNO_DOTNET.md`).
