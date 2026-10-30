namespace ec.edu.monster.api;

/// <summary>
/// URL base completa de la API REST: host, puerto y prefijo <c>api</c>, con «/» final.
/// Las rutas de las operaciones se le añaden relativas y sin barra inicial. Es el único lugar donde vive.
/// </summary>
public static class ConfiguracionApi
{
    /// <summary>
    /// CAMBIAR: URL base completa del servidor (host, puerto y prefijo <c>api</c>, con «/» final). El valor de la
    /// plantilla es el perfil <c>http</c> de Kestrel del servidor .NET (5267); con IIS Express es 57592. Se puede
    /// sobrescribir sin recompilar con la variable de entorno <c>API_BASE_URL</c>.
    /// </summary>
    public const string UrlBasePorDefecto = "http://localhost:5267/api/";

    public const string VariableUrlBase = "API_BASE_URL";

    /// <summary>Tiempo límite de cada petición.</summary>
    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>Variable <c>API_BASE_URL</c> si existe; si no, <see cref="UrlBasePorDefecto"/>. Siempre normalizada.</summary>
    public static string UrlBase() => UrlBase(Environment.GetEnvironmentVariable);

    /// <summary>Igual que la anterior, con el origen de las variables inyectado (las pruebas no tocan el entorno real).</summary>
    internal static string UrlBase(Func<string, string?> variable)
    {
        string? url = variable(VariableUrlBase);
        if (string.IsNullOrWhiteSpace(url))
        {
            url = UrlBasePorDefecto;
        }
        return NormalizarUrl(url);
    }

    internal static string NormalizarUrl(string url) => url.Trim().TrimEnd('/') + "/";
}
