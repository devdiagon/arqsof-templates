namespace ec.edu.monster.api;

/// <summary>
/// URL base de la API REST. Se recibe COMPLETA (host, puerto y prefijo) y no se asume su forma: cambiar de servidor
/// es cambiar solo esta URL. Debe terminar en "/" porque con <c>HttpClient.BaseAddress</c> una ruta relativa sin
/// barra inicial ("auth/login") solo se concatena bien si la base termina en "/".
/// </summary>
public static class ConfiguracionApi
{
    // CAMBIAR: URL base de la API, con prefijo y "/" final. Se puede sobrescribir sin recompilar con la variable de
    // entorno API_BASE_URL. Ejemplos:
    //   .NET (IIS Express, HTTP)    http://localhost:57592/api/
    //   .NET (Kestrel, perfil http) http://localhost:5267/api/
    //   Java (Payara)               http://localhost:8080/Contexto/api/
    public const string UrlBasePorDefecto = "http://localhost:57592/CAMBIAR_PREFIJO_API/";

    public const string VariableEntorno = "API_BASE_URL";

    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>Variable <c>API_BASE_URL</c> si existe; si no, <see cref="UrlBasePorDefecto"/>.</summary>
    public static string ObtenerUrlBase() => Resolver(Environment.GetEnvironmentVariable(VariableEntorno));

    internal static string Resolver(string? valorVariable) =>
        Normalizar(string.IsNullOrWhiteSpace(valorVariable) ? UrlBasePorDefecto : valorVariable);

    internal static string Normalizar(string url) => url.Trim().TrimEnd('/') + "/";
}
