# soap_dotnet_server — Plantilla de servidor SOAP en .NET (ASP.NET Core 10 + CoreWCF, SQL Server)

Plantilla para crear el **servidor** de un SOA .NET SOAP: **C# 14 + ASP.NET Core 10 + CoreWCF 1.9.1** ejecutado con Kestrel o IIS Express desde Visual Studio 2026, con persistencia en **SQL Server** por ADO.NET (`Microsoft.Data.SqlClient`). Sigue `ARQUITECTURA_SERVIDOR.md` (estructura del código) y `CONVENCION_SOAP.md` (mensaje en el cable), ambas copiadas en `docs/`; **si discrepan con este README, mandan `context/arquitectura/`**.

**No contiene nada del dominio.** Trae un **núcleo** que se copia sin cambios y un **ejemplo mínimo** (la entidad genérica `Elemento`) que se borra o se sustituye.

> **Estado:** `dotnet test` pasa sus **84 pruebas** xUnit (Moq para las dependencias; sin servidor ni base de datos) y `dotnet build` compila sin errores ni advertencias. El núcleo es el del servidor .NET SOAP construido y verificado a mano contra SQL Server (casos de conformidad C01–C29 de ese proyecto). *(No verificado):* esta plantilla **no se ha levantado** como servidor, no se ha consultado su WSDL, y el script `docs/bdd/Crea_BD.sql` (ejemplo) no se ha ejecutado.

## 1. Qué contiene

```
soap_dotnet_server/
├── soap_server_dotnet.slnx / .csproj    net10.0, Sdk.Web, CoreWCF, SqlClient, xUnit + Moq, GenerateProgramFile=false
├── Program.cs                           contenedor de dependencias + CoreWCF (metadatos y un endpoint de ejemplo)
├── Properties/launchSettings.json       perfiles http (5267) e IIS Express (57592 / 44380)
├── docs/                                CONVENCION_SOAP.md y ARQUITECTURA_SERVIDOR.md (copias de context/arquitectura)
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
    │   ├── ws/ConversorTexto                    tipos reales → texto del contrato
    │   ├── ws/contrato/RespuestaBase            envoltura estado/codigo/mensaje (R6)
    │   ├── ws/contrato/ConvencionSoap           TNS y constantes (solo se agregan servicios)
    │   └── pruebas/ConexionFalsa                dobles de DbConnection/DbTransaction para las pruebas de servicios
    └── EJEMPLO (sustituir por el dominio)
        ├── modelo/Elemento · db/IElementoRepositorio + ElementoRepositorioSqlServer · servicios/ElementoServicio
        ├── controlador/ElementoControlador
        ├── ws/IElementoWS · ws/ElementoWS · ws/ElementoMapeador
        ├── ws/contrato/Respuesta{Elemento,Elementos}, ElementoDetalle, ListaElementos
        └── pruebas/Elemento{Servicio,Controlador,Mapeador}Pruebas (más las pruebas del núcleo)
```

Los archivos `.cs` van en PascalCase; los directorios y espacios de nombres, en minúscula (`namespace ec.edu.monster.servicios;`, **explícito**, porque Visual Studio no lo genera desde la ruta). Dependencias solo hacia abajo: `ws → controlador → servicios → db`; `modelo`, `dto` y `excepciones` son transversales. Nada fuera de `ws` conoce SOAP.

## 2. Crear un proyecto a partir de la plantilla

1. Copiar la carpeta con el nombre nuevo, sin `bin/`, `obj/` ni `.vs/`.
2. Renombrar `soap_server_dotnet.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`.
3. `Properties/launchSettings.json`: elegir **puertos propios** si hay otro servidor abierto a la vez (`applicationUrl` del perfil `http`; `applicationUrl` y `sslPort` de `iisSettings`) y cambiar el `launchUrl` por la ruta del servicio.
4. `AccesoDB`: servidor, base, usuario y clave. `docs/bdd/Crea_BD.sql`: reemplazar por el script del proyecto (SOAP y REST de una plataforma comparten el mismo).
5. `ConvencionSoap`: nombre(s) del servicio. El TNS (`http://ws.monster.edu.ec/`) **no se cambia**.
6. Borrar el ejemplo (`Elemento*`, `Respuesta{Elemento,Elementos}`, `ElementoDetalle`, `ListaElementos`, `ELE_*` de `CodigoError` y las pruebas `Elemento*Pruebas`) y crear las piezas del dominio con la receta de §3, usando el ejemplo como molde. Nombres: entidad primero y tipo después (`CuentaServicio`, `CuentaWS`, `ICuentaRepositorio`…; `ARQUITECTURA_SERVIDOR.md` §3.1). Ajustar `Program.cs` (registro de dependencias y endpoint).
7. Mantener `dotnet test` en verde. Las pruebas del núcleo que citan `ELE_*` (`ControladorBasePruebas`, `ExcepcionMonsterPruebas`, `CodigoErrorPruebas`) se cambian por una entrada del dominio.

## 3. Añadir una operación (receta)

1. **Contrato** (`ws/contrato`): tipo que extiende `RespuestaBase`, con `[DataContract(Name, Namespace = ConvencionSoap.Tns)]`, cada propiedad con `[DataMember(Name, Order)]` y `EmitDefaultValue = false` en los opcionales, todo `string`. Listas: una clase que hereda de `List<T>` con `[CollectionDataContract(Name, ItemName, Namespace)]`, inicializada vacía. Un campo propio nunca se llama `estado`, `codigo` ni `mensaje`.
2. **Errores:** agregar a `CodigoError` los códigos nuevos (`PREFIJO-NNN`, con categoría). Nunca se reutiliza un código.
3. **Repositorio** (`db`): operación en la interfaz `I…Repositorio` y en la implementación `…SqlServer` (SQL como constantes, parámetros con nombre y tipo, recibe la `DbTransaction`, nunca abre ni cierra la conexión).
4. **Servicio:** lógica con `Transaccional.Ejecutar(...)`; cálculos como métodos estáticos puros; lanza `ExcepcionMonster`. Las operaciones públicas son `virtual` (Moq).
5. **Controlador:** valida la forma con `ValidadorForma`, convierte a tipos reales y `return Ejecutar(() => _servicio.X(…));`
6. **Mapeador y endpoint:** el mapeador pasa `Resultado` → contrato con `ConversorTexto`. En la interfaz `I…WS`, por operación: `[OperationContract(Name = "…")]` y `[return: MessageParameter(Name = ConvencionSoap.Retorno)]`; los nombres de los parámetros C# son los nombres XML. En la clase `…WS`, `[ServiceBehavior(Name, Namespace, PerCall, Multiple)]`.
7. **Registro en `Program.cs`:** dependencias en el contenedor, **la clase `…WS` con `AddTransient`** y el endpoint con `AddServiceEndpoint` (binding con `Name` y `Namespace`).
8. **Pruebas:** servicio (Moq + `ConexionFalsa`), controlador, mapeador; la conformidad del protocolo se verifica a mano (`CONVENCION_PRUEBAS.md` §6).

## 4. La convención aplicada

| Regla | Dónde está | Por qué |
|---|---|---|
| **R2** un solo TNS | `ConvencionSoap.Tns` | lo referencian `[ServiceContract]`, `[ServiceBehavior]`, el binding y todos los `[DataContract]` |
| **R3** campos calificados | `[DataContract(Namespace = Tns)]` | en `DataContract` siempre es así: no usar `XmlSerializerFormat`, `MessageContract` ni `Message` crudo |
| **R4** `SOAPAction` = URI | acción por defecto de WCF (`TNS + Servicio + "/" + operación`) | WCF rechaza la cabecera vacía; los clientes la envían |
| **R5** nombres fijos | `Name` en `ServiceContract`, `OperationContract`, `MessageParameter`, `DataContract` y `DataMember` | los clientes dependen de ellos |
| **R6** respuesta uniforme | `RespuestaBase` + `Resultado` + mapeadores | éxito y error con el mismo tipo |
| **R7** datos como texto | `ConversorTexto` | sin diferencias de cultura ni de tipos entre plataformas |
| **R8** errores en el cuerpo | `ControladorBase.Ejecutar` (`ExcepcionMonster` → código y mensaje; otra → `ERR-500`) | HTTP 200 con `estado = ERROR`, sin SOAP Fault |

Sin `Namespace` en el binding, el binding del WSDL sale en `http://tempuri.org/`; sin `Namespace` en el `ServiceBehavior`, el servicio también.

## 5. Ejecutar

```powershell
dotnet test                                           # sin servidor ni base de datos (la lógica se simula con Moq)
dotnet build
dotnet run --launch-profile http                      # o F5 en Visual Studio con el perfil elegido
```

**Base de datos:** SQL Server local en Docker (`context/entornos/ENTORNO_DOTNET.md` §2.2). Ejecutar `docs/bdd/Crea_BD.sql` (crea `ejemplo`, la tabla y el login `ejemplo`/`admin` con permisos solo sobre ella, no `sa`). `Crea_BD.sql` usa lotes `GO`: con `sqlcmd -C -f 65001 -i`, o *Execute script* en DBeaver.

| Qué | URL (perfil `http`) |
|---|---|
| WSDL | `http://localhost:5267/ServicioElemento?wsdl` |
| Perfil IIS Express | `http://localhost:57592/ServicioElemento?wsdl` |

**Acceso desde emulador o teléfono, IIS Express y firewall:** `context/entornos/IIS_CONFIG.md`. Cambiar de perfil cambia la URL, y cada cliente la guarda en un solo lugar. **Tras cambiar el contrato hay que regenerar o actualizar todos los clientes.** La verificación manual del contrato real está en `CONVENCION_PRUEBAS.md` §6.1.

## 6. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| El WSDL se publica, pero **toda llamada responde HTTP 500 `InternalServiceFault`** («does not have a default (parameter-less) constructor») | La clase `…WS` recibe dependencias por constructor y no está registrada en el contenedor | `builder.Services.AddTransient<…WS>()`. Para ver la causa: `IncludeExceptionDetailInFaults` en el `ServiceBehavior`, solo temporalmente |
| WSDL con `targetNamespace="http://tempuri.org/"` | Falta `Namespace` en `[ServiceContract]`, `[ServiceBehavior]` o en el binding | Fijarlo en los tres |
| El WSDL anuncia `localhost` aunque se consulte por IP | Falta `UseRequestHeadersForMetadataAddressBehavior` | Registrarlo en `Program.cs` |
| Un cliente recibe `estado`, `codigo`… vacíos | El cliente manda los parámetros sin el namespace | Calificarlos con el TNS (R3) |
| HTTP 500 `ActionNotSupported` | El cliente envía `SOAPAction: ""` | Enviar la URI (R4) |
| 404 en `…?wsdl` | La ruta del `AddServiceEndpoint` no coincide con la URL | Revisar la ruta y el `launchUrl` |
| `ERR-500` en cada llamada | La base no existe, está apagada o las constantes de `AccesoDB` no coinciden | Ejecutar el script, revisar servidor/base/usuario/clave; el detalle está en la consola del servidor |
| Avisos `NU1902`/`NU1903` | Dependencia transitiva `System.Security.Cryptography.Pkcs` | Ya está fijada en 10.0.14 en el `.csproj` |
| `MSB3027` / `MSB3021`: no se puede copiar el `.dll` | El servidor sigue en ejecución | Detenerlo antes de compilar o compilar con `-p:OutDir=<otra carpeta>` |
| Más problemas de entorno | | `context/entornos/ENTORNO_DOTNET.md` §5 |

## 7. Alcance

Autenticación y autorización quedan fuera de la plantilla base (cada proyecto las define; la categoría `NoAutorizado` ya existe en el catálogo). El servidor no guarda estado entre llamadas. Sin pool de conexiones propio y sin archivos de configuración de la aplicación (las constantes de `AccesoDB` están en el código), por tratarse de entornos locales académicos. HTTPS (segundo endpoint con `BasicHttpSecurityMode.Transport`) no viene configurado: ver `CONVENCION_SOAP.md` §3.2.

## 8. Entorno de referencia

Windows 11, Visual Studio Community 2026, SDK .NET 10, CoreWCF 1.9.1, `Microsoft.Data.SqlClient` 7.1.1, xUnit 2.9.3, Moq 4.21.0, SQL Server 2025 en Docker (ver `context/entornos/ENTORNO_DOTNET.md`).
