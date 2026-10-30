using System.ServiceModel;

namespace ec.edu.monster.ws;

/// <summary>
/// Configuración del transporte de los proxies generados con dotnet-svcutil, <b>propia del móvil</b>: URL completa de cada
/// servicio, binding y tiempos límite. No conoce el dominio. No usa la URL que trae cada <c>Reference.cs</c>. La URL depende
/// de dónde se ejecuta la aplicación (Android no tiene <c>localhost</c> ni variables de entorno), por eso es la única pieza de
/// <c>ws</c> que se reescribe respecto a la de la consola (ARQUITECTURA_CLIENTE.md §4.9). Convención (docs/CONVENCION_SOAP.md):
/// <list type="bullet">
/// <item>R9: la URL de cada servicio se recibe COMPLETA por configuración y no se asume su forma ni que varios servicios
/// cuelguen de una misma base. Cambiar de servidor es cambiar solo estas constantes.</item>
/// <item>R10: el cliente no depende del nombre del puerto ni del binding del WSDL (CoreWCF y Metro los nombran distinto): se
/// crea el cliente generado con su propio binding y la URL configurada.</item>
/// </list>
/// Host por defecto: el emulador de Android ve el PC como <c>10.0.2.2</c>; un teléfono físico necesita la IP del PC
/// (<see cref="IpPc"/>); cualquier otra plataforma (Windows) usa <c>localhost</c>. Solo HTTP en desarrollo.
/// <code>
/// string url = ConfiguracionServicio.UrlConfigurada();
/// new XClient(ConfiguracionServicio.CrearBinding(url), new EndpointAddress(url));
/// </code>
/// </summary>
public static class ConfiguracionServicio
{
    /// <summary>
    /// Puerto HTTP del servidor. CAMBIAR. Servidor .NET con Kestrel (perfil <c>http</c>): 5267; con IIS Express: 57592;
    /// Java (Payara): 8080.
    /// </summary>
    public const int Puerto = 5267;

    /// <summary>
    /// Solo para el teléfono físico: IP del PC en la red local. CAMBIAR por la IP real (<c>ipconfig</c>) al probar con un
    /// teléfono: la plantilla trae un marcador de posición. El emulador y Windows no la usan.
    /// </summary>
    public const string IpPc = "127.0.0.1";

    /// <summary>
    /// Ruta del servicio, sin «/» inicial ni final. CAMBIAR. Con varios servicios, una constante por servicio (R9):
    /// <c>RutaConsulta</c>, <c>RutaOperacion</c>… Ejemplos: servidor .NET <c>RutaServicio</c>; Java <c>Contexto/Servicio</c>.
    /// </summary>
    public const string RutaServicio = "CAMBIAR_SERVICIO";

    /// <summary>Tiempo límite de apertura, envío, recepción y cierre.</summary>
    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>URL completa por defecto de <see cref="RutaServicio"/> en la plataforma en ejecución.</summary>
    public static string UrlPorDefecto() => UrlPorDefecto(RutaServicio);

    /// <summary>Variable de entorno <c>WS_URL</c>, si no <see cref="UrlPorDefecto()"/>. Se normaliza.</summary>
    public static string UrlConfigurada() => UrlConfigurada(Environment.GetEnvironmentVariable);

    /// <summary>Igual que la anterior, con el origen de las variables inyectado (las pruebas no tocan el entorno real).</summary>
    internal static string UrlConfigurada(Func<string, string?> variable)
    {
        string? url = variable("WS_URL");
        if (string.IsNullOrWhiteSpace(url))
        {
            url = UrlPorDefecto();
        }
        return NormalizarUrl(url);
    }

    /// <summary>
    /// Valor configurable por nombre, para cuando el servidor publica varios servicios. Variable de entorno
    /// <c>WS_URL_&lt;SERVICIO&gt;</c> (el nombre en mayúsculas) si existe; si no, la URL por defecto indicada
    /// (<c>UrlPorDefecto(RutaConsulta)</c>). Se normaliza. En Android no hay variables de entorno: solo rige en Windows.
    /// </summary>
    public static string UrlConfigurada(string servicio, string urlPorDefecto) =>
        UrlConfigurada(servicio, urlPorDefecto, Environment.GetEnvironmentVariable);

    internal static string UrlConfigurada(string servicio, string urlPorDefecto, Func<string, string?> variable)
    {
        string? url = variable("WS_URL_" + servicio.ToUpperInvariant());
        if (string.IsNullOrWhiteSpace(url))
        {
            url = urlPorDefecto;
        }
        return NormalizarUrl(url);
    }

    /// <summary>URL por defecto de un servicio en la plataforma en ejecución.</summary>
    public static string UrlPorDefecto(string ruta) =>
        UrlPorDefecto(ruta, DeviceInfo.Platform == DevicePlatform.Android, DeviceInfo.DeviceType == DeviceType.Virtual);

    /// <summary>
    /// Igual que la anterior, con la plataforma inyectada (las pruebas no dependen de MAUI): Android virtual →
    /// <c>10.0.2.2</c>; Android físico → <see cref="IpPc"/>; cualquier otra → <c>localhost</c>.
    /// </summary>
    internal static string UrlPorDefecto(string ruta, bool esAndroid, bool esVirtual)
    {
        string host = esAndroid ? (esVirtual ? "10.0.2.2" : IpPc) : "localhost";
        return $"http://{host}:{Puerto}/{ruta}";
    }

    internal static string NormalizarUrl(string url) => url.Trim().TrimEnd('/');

    /// <summary>
    /// El esquema de la URL decide el modo: <c>https</c> usa seguridad de transporte; <c>http</c>, ninguna.
    /// </summary>
    public static BasicHttpBinding CrearBinding(string url)
    {
        bool https = url.StartsWith("https://", StringComparison.OrdinalIgnoreCase);
        return new BasicHttpBinding(https ? BasicHttpSecurityMode.Transport : BasicHttpSecurityMode.None)
        {
            OpenTimeout = TiempoLimite,
            SendTimeout = TiempoLimite,
            ReceiveTimeout = TiempoLimite,
            CloseTimeout = TiempoLimite
        };
    }
}
