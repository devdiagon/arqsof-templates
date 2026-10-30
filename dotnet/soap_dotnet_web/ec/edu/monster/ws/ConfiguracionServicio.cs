using System.ServiceModel;

namespace ec.edu.monster.ws;

/// <summary>
/// Configuración genérica del transporte de los proxies generados con dotnet-svcutil: URL completa del servicio, binding y
/// tiempos límite. No conoce el dominio. No usa la URL que trae cada <c>Reference.cs</c>. Convención (docs/CONVENCION_SOAP.md):
/// <list type="bullet">
/// <item>R9: la URL del servicio se recibe COMPLETA por configuración y no se asume su forma ni que varios servicios cuelguen de
/// una misma base (la ruta de un servidor Java es /Contexto/Servicio; la de uno .NET, una ruta libre). Cambiar de servidor es
/// cambiar solo esta URL.</item>
/// <item>R10: el cliente no depende del nombre del puerto ni del binding del WSDL (CoreWCF y Metro los nombran distinto): se crea
/// el cliente generado con su propio binding y la URL configurada.</item>
/// </list>
/// <code>
/// string url = ConfiguracionServicio.UrlConfigurada();
/// new XClient(ConfiguracionServicio.CrearBinding(url), new EndpointAddress(url));
/// </code>
/// </summary>
public static class ConfiguracionServicio
{
    /// <summary>
    /// URL COMPLETA del servicio (sin "?wsdl", sin «/» final; con host, puerto y ruta tal como la entrega el servidor).
    /// CAMBIAR, o sobrescribir sin recompilar con la variable de entorno <c>WS_URL</c>. Ejemplos: servidor .NET con Kestrel
    /// http://localhost:5267/RutaServicio · con IIS Express http://localhost:57592/RutaServicio · Java http://localhost:8080/Contexto/Servicio
    /// </summary>
    public const string UrlPorDefecto = "http://localhost:5267/CAMBIAR_SERVICIO";

    /// <summary>Tiempo límite de apertura, envío, recepción y cierre.</summary>
    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>Variable de entorno <c>WS_URL</c>, si no <see cref="UrlPorDefecto"/>. Se normaliza.</summary>
    public static string UrlConfigurada() => UrlConfigurada(Environment.GetEnvironmentVariable);

    /// <summary>Igual que la anterior, con el origen de las variables inyectado (las pruebas no tocan el entorno real).</summary>
    internal static string UrlConfigurada(Func<string, string?> variable)
    {
        string? url = variable("WS_URL");
        if (string.IsNullOrWhiteSpace(url))
        {
            url = UrlPorDefecto;
        }
        return NormalizarUrl(url);
    }

    /// <summary>
    /// Valor configurable por nombre, para cuando el servidor publica varios servicios. Variable de entorno
    /// <c>WS_URL_&lt;SERVICIO&gt;</c> (el nombre en mayúsculas) si existe; si no, la URL por defecto indicada. Se normaliza.
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
