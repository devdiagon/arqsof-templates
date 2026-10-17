# soap_server_dotnet — Plantilla de servidor SOAP en .NET (CoreWCF)

Plantilla para crear servidores **SOAP/WSDL** con **C# + ASP.NET Core 10 + CoreWCF**, ejecutados localmente desde **Visual Studio 2026 Community** con Kestrel o **IIS Express**.

> **Estado de la plantilla:** proyecto web de .NET 10 (`Microsoft.NET.Sdk.Web`) con la **base ya preparada (fase 0)**: paquetes CoreWCF y xUnit instalados, `GenerateProgramFile=false`, directorios en minúscula creados (con `.gitkeep`) y perfiles de ejecución definidos. Sin lógica de dominio. `Program.cs` responde "Hello World!" en `/`; **cualquier otra ruta da 404** hasta que se añada CoreWCF y un endpoint (§3). El código de este README se probó en un proyecto descartable (CoreWCF `1.9.1` sobre `net10.0` publicó el WSDL con HTTP 200); lo marcado como *por verificar* no se ha probado.

## 1. Qué contiene

```
soap_server_dotnet/
├── soap_server_dotnet.slnx          solución (formato .slnx de VS 2026)
├── soap_server_dotnet.csproj        net10.0, Sdk.Web, CoreWCF 1.9.1, xUnit, GenerateProgramFile=false
├── Program.cs                       plantilla "Hello World!" (se reemplaza en §3)
├── appsettings.json / .Development.json
├── ec/edu/monster/{ws,controlador,dto,servicios,seguridad,modelo/conversores,excepciones,utilidades,pruebas}/   (vacíos, con .gitkeep)
├── Properties/launchSettings.json   perfiles `http` (5267) e `IIS Express` (57592 / 44380)
├── CONVENCION_SOAP.md               reglas de interoperabilidad SOAP Java ⇄ .NET (copia de la del proyecto de origen; independiente del dominio)
└── README.md
```

Git ignora `bin/`, `obj/`, `.vs/` y los archivos de usuario (`*.csproj.user`), ver `.gitignore`.

## 2. Crear un proyecto a partir de la plantilla

1. Copiar la carpeta con el nombre nuevo (ej. `MiProyecto_SOAP_DOTNET`). No copiar `bin`, `obj` ni `.vs`.
2. Renombrar `soap_server_dotnet.csproj` y `.slnx`, y corregir en el `.slnx` la ruta del `<Project Path="…csproj" />`.
3. Elegir **puertos propios** en `Properties/launchSettings.json` (`applicationUrl` del perfil `http`; `applicationUrl` y `sslPort` de `iisSettings`) para no chocar con otros proyectos abiertos a la vez.
4. Cambiar el `launchUrl` por la ruta de tu endpoint (hoy `WSConUniSOAPDotNet?wsdl`, ejemplo del proyecto de origen).
5. Abrir el `.slnx` en Visual Studio.

## 3. Convertirlo en un servidor SOAP

### 3.1 Paquetes (ya instalados en la plantilla; solo para actualizar o como referencia)
```powershell
dotnet add package CoreWCF.Http --version 1.9.1
dotnet add package CoreWCF.Primitives --version 1.9.1
```
Son los únicos paquetes del servidor (versión estable al 2026-10-16; comprobar si hay una más nueva).

### 3.2 Estructura (directorios **en minúscula**, ya creados; borrar los `.gitkeep` que no se usen)
```
ec/edu/monster/
  ws/ controlador/ dto/ servicios/ seguridad/ modelo/ excepciones/ utilidades/ pruebas/
```
Los archivos `.cs` van en PascalCase; los directorios y espacios de nombres, en minúscula (`namespace ec.edu.monster.servicios;`, **explícito**, porque VS no lo generaría desde la ruta). Capa `ws` sin lógica: solo delega a `controlador` → `servicios` → `modelo`.

### 3.3 Contrato y servicio
```csharp
// ec/edu/monster/ws/IMiServicio.cs
using CoreWCF;
namespace ec.edu.monster.ws;

[ServiceContract(Name = "MiServicio", Namespace = "http://ws.misitio.ec/")]
public interface IMiServicio
{
    [OperationContract(Name = "login")]
    [return: MessageParameter(Name = "respuesta")]   // nombre único del elemento de retorno (regla R5)
    RespuestaLoginDto Login(string usuario, string contrasenia);
}

// ec/edu/monster/ws/MiServicio.cs
[ServiceBehavior(Name = "MiServicio", Namespace = "http://ws.misitio.ec/",
                 InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
public class MiServicio(AutenticacionControlador auth) : IMiServicio
{
    public RespuestaLoginDto Login(string usuario, string contrasenia) => auth.Login(usuario, contrasenia);
}
```
- **Fijar `Namespace` en `[ServiceContract]`, `[ServiceBehavior]` y en el binding (§3.4):** si no, el WSDL sale con `http://tempuri.org/` (comprobado).
- Los **nombres de los parámetros C#** son el contrato XML: `usuario`, no `Usuario`. Nombrar las operaciones con `[OperationContract(Name = …)]`.
- DTO con `[DataContract(Namespace = …)]` y `[DataMember(Name = …, Order = n)]`; `EmitDefaultValue = false` para que los nulos no aparezcan. Para listas con elementos con nombre propio: `[CollectionDataContract(ItemName = "elemento")]` (verificado: produce `<elementos><elemento>…</elemento></elementos>`).
- Si el servicio no debe lanzar `SOAP Fault` por errores de negocio, capturar excepciones en el controlador y devolver siempre un DTO con `estado`, `codigo` y `mensaje`.

### 3.4 `Program.cs`
```csharp
using CoreWCF;
using CoreWCF.Configuration;
using CoreWCF.Description;
using ec.edu.monster.ws;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddServiceModelServices().AddServiceModelMetadata();
builder.Services.AddSingleton<IServiceBehavior, UseRequestHeadersForMetadataAddressBehavior>();
// registrar aquí tus servicios/controladores como Singleton si guardan estado compartido
// builder.Services.AddSingleton<AutenticacionControlador>();

var app = builder.Build();

app.UseServiceModel(sb =>
{
    sb.AddService<MiServicio>();
    // Namespace del binding: sin él el WSDL declara el binding en http://tempuri.org/
    sb.AddServiceEndpoint<MiServicio, IMiServicio>(
        new BasicHttpBinding { Name = "MiServicioPortBinding", Namespace = "http://ws.misitio.ec/" }, "/MiServicioSOAP");
    app.Services.GetRequiredService<ServiceMetadataBehavior>().HttpGetEnabled = true;
});

app.Run();
```
- `BasicHttpBinding` = SOAP 1.1 sobre HTTP. `UseRequestHeadersForMetadataAddressBehavior` hace que el WSDL anuncie el host con el que se consultó (`localhost`, IP, `10.0.2.2`).
- El estado en memoria (tokens, sesiones) debe vivir en un **singleton** y ser *thread-safe* (`ConcurrentDictionary`). Para el reloj, `TimeProvider` inyectable.
- Quitar el `MapGet("/", …)` de la plantilla (o dejarlo como comprobación de que la app corre).

### 3.5 Verificar
Ejecutar y abrir `http://localhost:5267/MiServicioSOAP?wsdl`: debe devolver el XML del WSDL. Revisar en él `targetNamespace`, nombres de operaciones, parámetros y el elemento de retorno antes de generar clientes.

### 3.6 Interoperar con servidores y clientes Java (JAX-WS) — convención verificada

Un servicio CoreWCF hecho como en §3.3–3.5 **ya cumple** la convención de este conjunto de proyectos; no hace falta código de transporte especial ni copiar el WSDL de otro servidor. Lo que hay que saber está en **`CONVENCION_SOAP.md`** (copia incluida en esta carpeta; es independiente del dominio). Resumen de las reglas que importan al escribir el servicio .NET:

1. **Todos los elementos calificados con el namespace de destino** (envoltorio, parámetros, retorno y campos de los DTO). `DataContract` ya lo hace; **no** usar `XmlSerializerFormat`, `MessageContract`, `Message` crudo ni un WSDL estático para "quitar" el namespace: se probaron y no son necesarios.
2. **`SOAPAction` = namespace + servicio + `/` + operación**, que es la acción por defecto de WCF. Un cliente que mande `SOAPAction: ""` recibe HTTP 500 (`ActionNotSupported`): los clientes deben enviar la URI (si el otro lado es Java, declararla con `@WebMethod(action=…)`).
3. **Nombres fijos**: `[ServiceContract(Name, Namespace)]`, `[OperationContract(Name)]`, `[return: MessageParameter(Name = "respuesta")]`, `[DataContract(Name, Namespace)]`, `[DataMember(Name, Order)]`; los nombres de parámetros C# son los XML.
4. **Namespace del binding**: `new BasicHttpBinding { Name = …, Namespace = … }`; sin él, el binding del WSDL sale en `tempuri.org`.
5. **Respuesta uniforme** (`RespuestaBase` con `estado`/`codigo`/`mensaje`), datos como `string`, opcionales omitidos (`EmitDefaultValue = false`) y listas vacías, nunca ausentes.
6. **No depender** del nombre del puerto (CoreWCF lo llama `<Binding>_<Contrato>`) ni de que el WSDL sea un solo documento.

En el lado Java hay que declarar lo contrario de sus valores por defecto (`package-info.java` con `@XmlSchema(... QUALIFIED)` y `targetNamespace`/`action` en las anotaciones del servicio); la receta completa, las de cliente (JAX-WS a mano, WCF, Android) y la lista de verificación están en `CONVENCION_SOAP.md` §3, §4 y §6.

Límites conocidos entre plataformas: `decimal` de .NET (~28 dígitos) frente a `BigDecimal` de Java (ilimitado); `nillable="true"` en los `string` del XSD de .NET (en un cliente Java generado con `wsimport` usar un archivo de binding con `generateElementProperty="false"`).
## 4. Ejecución local con Visual Studio e IIS Express

### 4.1 Perfiles y URLs
| Perfil (desplegable de VS) | Hospedaje | URL base |
|---|---|---|
| `http` | Kestrel (`dotnet run`) | `http://localhost:5267` |
| `IIS Express` (HTTP) | IIS Express, en proceso | `http://localhost:57592` |
| `IIS Express` (HTTPS) | IIS Express, en proceso | `https://localhost:44380` |

La URL del servicio es *base + ruta del endpoint* (`/MiServicioSOAP`). **Un cliente = una URL**: cambiar de perfil cambia la URL y reinicia el estado en memoria (los tokens/sesiones se pierden).

### 4.2 Qué tener en cuenta con IIS Express
- Funciona **en proceso** con CoreWCF sin cambios de código ni de `.csproj`. En este modo manda `iisSettings` de `launchSettings.json`; `UseUrls`/`--urls` se ignoran.
- Dejar **autenticación anónima habilitada y Windows deshabilitada** (ya así en `iisSettings`). Con Windows activa, `BasicHttpBinding` sin seguridad falla.
- No hace falta IIS completo ni el Hosting Bundle (en el equipo de origen no están instalados). IIS Express es parte de Visual Studio; si aparece `HTTP Error 500.19`/ANCM, reparar Visual Studio.
- Usar la versión de 64 bits: Herramientas → Opciones → Proyectos y soluciones → Proyectos web.
- No es un servicio de Windows: **si Visual Studio no lo ejecuta, el servidor está apagado**. Detener/recompilar/reiniciar borra el estado en memoria.
- **HTTPS (44380):** `BasicHttpBinding` sin seguridad solo atiende HTTP, y CoreWCF **no tiene** `BasicHttpsBinding`. Para servir también `https://localhost:44380` se registra un segundo endpoint con la misma ruta: `new BasicHttpBinding(CoreWCF.Channels.BasicHttpSecurityMode.Transport) { Name = …, Namespace = … }` (verificado en Kestrel con un servicio real). Regístralo **solo** si el host tiene una dirección HTTPS (p. ej. con una opción de configuración que active el perfil `IIS Express` mediante `environmentVariables`); si no, CoreWCF falla en la primera petición. Con ambos endpoints el WSDL declara dos puertos. Si el navegador rechaza el certificado: `dotnet dev-certs https --trust`. El emulador y el teléfono no aceptan ese certificado: para móvil usar HTTP.
- El `applicationhost.config` del proyecto vive en `.vs\<solución>\config\` (se crea al ejecutar con IIS Express y se regenera si se borra `.vs`).

### 4.3 Acceso desde otros dispositivos (emulador / teléfono)
Dentro del emulador de Android el PC es `10.0.2.2`; un teléfono físico usa la IP del PC (`ipconfig`), en la misma Wi‑Fi y con la red marcada como **privada**.

**Opción recomendada — perfil `http` (Kestrel):**
1. En `launchSettings.json`, perfil `http`: `"applicationUrl": "http://0.0.0.0:5267"` (o `dotnet run --urls http://0.0.0.0:5267`). Abrir el navegador a mano en `http://localhost:5267/…`.
2. Abrir el puerto en el firewall (abajo).

**Con `IIS Express`:** solo atiende `localhost` y devuelve `400 Invalid Hostname` a otros hosts. Con Visual Studio **cerrado**, en `.vs\<solución>\config\applicationhost.config`, dejar **ambos bindings en el mismo sitio** (añadir el comodín, no sustituir `localhost`, o VS creará un sitio nuevo `…(1)`):
```xml
<binding protocol="http" bindingInformation="*:57592:localhost" />
<binding protocol="http" bindingInformation="*:57592:*" />
```
Y permiso: ejecutar Visual Studio como administrador, o una vez como administrador `netsh http add urlacl url=http://*:57592/ user=Todos` (deshacer: `netsh http delete urlacl url=http://*:57592/`).

**Firewall (PowerShell como administrador):**
```powershell
New-NetFirewallRule -DisplayName "SOAP 5267" -Direction Inbound -Protocol TCP -LocalPort 5267 -Action Allow -Profile Private
Remove-NetFirewallRule -DisplayName "SOAP 5267"      # cerrar al terminar
```
**Cerrar el puerto cuando no se use**: el servicio no suele tener más protección que el token en memoria.

| Cliente | URL base |
|---|---|
| Mismo PC (consola, escritorio, web, MAUI Windows) | `http://localhost:<puerto>` |
| Emulador de Android | `http://10.0.2.2:<puerto>` |
| Teléfono físico | `http://<IP-del-PC>:<puerto>` |

Android bloquea HTTP sin cifrar: `android:usesCleartextTraffic="true"` y permisos `INTERNET`/`ACCESS_NETWORK_STATE` en el manifiesto.

## 5. Consumir el servicio desde clientes .NET
- Paquetes del cliente: `System.ServiceModel.Http` y `System.ServiceModel.Primitives` (`10.0.652802`, última estable al 2026-10-16).
- Con el servidor en marcha: *Agregar → Servicio conectado → WCF Web Service Reference* con la URL del WSDL (o `dotnet-svcutil`, que no viene instalado).
- **No dependas de la URL fija de `Reference.cs`:** crea el cliente con `BasicHttpBinding` y un `EndpointAddress` leído de **un solo lugar de configuración**. Fija timeouts (~10 s).
- Los errores de negocio llegan como respuesta con `estado`/`codigo` (HTTP 200), no como excepción: revisar siempre esos campos antes de usar el resultado.
- Cualquier cambio de firma (operaciones, parámetros, DTO) obliga a regenerar/actualizar los clientes.

## 6. Pruebas
xUnit **dentro del mismo proyecto**, en `ec/edu/monster/pruebas/`. Ya instalado en la plantilla (`xunit` 2.9.3, `xunit.runner.visualstudio` 4.0.1, `Microsoft.NET.Test.Sdk` 18.11.0, `Microsoft.Extensions.TimeProvider.Testing` 10.11.0, `Microsoft.AspNetCore.Mvc.Testing` 10.0.13); comandos de referencia:
```powershell
dotnet add package xunit
dotnet add package xunit.runner.visualstudio
dotnet add package Microsoft.NET.Test.Sdk
dotnet add package Microsoft.Extensions.TimeProvider.Testing
dotnet add package Microsoft.AspNetCore.Mvc.Testing
```
`<GenerateProgramFile>false</GenerateProgramFile>` ya está en el `.csproj` (evita duplicar el punto de entrada). Añadir `public partial class Program;` al final de `Program.cs` para `WebApplicationFactory<Program>`. Dos requisitos descubiertos al probarlo: (1) CoreWCF necesita direcciones base, que `TestServer` no tiene: usar una fábrica con `UseKestrel(0)` (disponible desde .NET 10); (2) como las pruebas están en el mismo proyecto, `WebApplicationFactory` no encuentra la raíz de contenido: el `.csproj` de la plantilla incluye el destino `EscribirManifiestoMvcTesting` que escribe `MvcTestingAppManifest.json` en la carpeta de salida (la clave debe ser el nombre completo del ensamblado). Ejecutar con `dotnet test`. Los paquetes de prueba viajan con el servidor; para publicar, excluir `ec/edu/monster/pruebas/**` de `Release`.

## 7. Problemas frecuentes
| Síntoma | Causa | Solución |
|---|---|---|
| 404 en la ruta del WSDL | Aún no hay CoreWCF/endpoint (plantilla vacía) o la ruta no coincide | §3 y revisar la ruta del `AddServiceEndpoint` y el `launchUrl` |
| `http://localhost:44380` falla | 44380 es el puerto **HTTPS** de IIS Express | Usar `https://localhost:44380` (con endpoint HTTPS) o el puerto HTTP |
| WSDL con `targetNamespace="http://tempuri.org/"` | Falta `Namespace` en `[ServiceContract]`/`[ServiceBehavior]` | Fijarlo en ambos |
| El WSDL anuncia `localhost` aunque se consulte por IP | Falta `UseRequestHeadersForMetadataAddressBehavior` | Registrarlo en `Program.cs` |
| `400 Invalid Hostname` desde IP o `10.0.2.2` | IIS Express solo atiende `localhost` | §4.3 (o usar el perfil `http` con `0.0.0.0`) |
| Conexión rechazada desde IP con el perfil `http` | Kestrel escucha solo en `localhost` | `applicationUrl` con `0.0.0.0` + firewall |
| Aparecen sitios `…(1)`, `…(2)` en `applicationhost.config` | Se reemplazó el binding `localhost` en lugar de añadir el comodín | Un solo sitio con ambos bindings; borrar duplicados |
| Puerto en uso | Otro proceso usa el puerto | `netstat -ano \| findstr :<puerto>` |
| Los clientes pierden la sesión | Se reinició la app o se cambió de perfil | Iniciar sesión de nuevo |
| `MSB3821 … zona restringida` | Archivos descargados con marca de la Web | `Get-ChildItem <carpeta> -Recurse -File -Force \| Unblock-File` |

## 8. Entorno de referencia (verificado el 2026-10-16)
Windows 11, Visual Studio Community 2026 (18.11), SDK .NET 10.0.301/10.0.402, CoreWCF 1.9.1. IIS Express presente; IIS completo (`W3SVC`) y Hosting Bundle **no** instalados (no necesarios para este flujo).
